package com.uchain.core.producer;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.uchain.core.Block;
import com.uchain.core.BlockChain;
import com.uchain.core.Transaction;
import com.uchain.core.producer.ProduceStateImpl.*;
import com.uchain.crypto.*;
import com.uchain.main.ConsensusSettings;
import com.uchain.main.Witness;
import com.uchain.network.message.BlockMessageImpl;
import com.uchain.network.message.InventoryPayload;
import com.uchain.network.message.InventoryType;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Producer extends AbstractActor {
    private static final Logger log = LoggerFactory.getLogger(Producer.class);
	private ConsensusSettings settings;
	private ActorRef peerManager;
	private BlockChain chain;

	public Producer(ConsensusSettings settings, BlockChain chain, ActorRef peerManager) {
		this.settings = settings;
		this.peerManager = peerManager;
		this.chain = chain;
	}

	public static Props props(ConsensusSettings settings, BlockChain chain, ActorRef peerManager) {
		return Props.create(Producer.class, settings, chain, peerManager);
	}

	private Map<UInt256, Transaction> txPool = new HashMap();
	private boolean canProduce = false;
    private ProduceTask task;
    @Override
	public void preStart() {
		task = new ProduceTask(this, peerManager, false);
		getContext().system().scheduler().scheduleOnce(Duration.ZERO, task, getContext().system().dispatcher());
	}

	/**
	 * 同步最新的区块完后开始加入生产区块
	 * @return
	 */
	 public ProduceState produce() {
		 try {
			 Long now = Instant.now().toEpochMilli(); //精确到毫秒
			 if(canProduce) {
				 return tryProduce(now);
			 }else {
				 Long next = nextBlockTime(1);
				 if (next >= now) {
					 canProduce = true;
					 return tryProduce(now);
				 }else {
					 return new NotSynced(next, now);
				 }
			 }
		 }catch(Exception e) {
			 return new Failed(e);
		 }
	 }

	 /**
	  * 满足时间，且此刻轮训到当前节点，开始生产区块
	  * @param now
	  * @return
	  */
	private ProduceState tryProduce(Long now) {
		Long next = nextBlockTime(1);
		tryStartProduce(now);
		if (now + settings.getAcceptableTimeError() < next) {
			return new NotYet(next,now);
		}else {
            if (nextProduceTime(now, next) > next) {
                //println(s"some blocks skipped")
            }
            Witness witness = getWitness(nextProduceTime(now, next));
            if (witness.getPrivkey()==null || "".equals(witness.getPrivkey())) {
                return new NotMyTurn(witness.getName(), PublicKey.apply(new BinaryData(witness.getPubkey())));
            }else {
                Block block = chain.produceBlockFinalize(PublicKey.apply(new BinaryData(witness.getPubkey())), PrivateKey.apply(new BinaryData(witness.getPrivkey())), nextProduceTime(now, next));
                if (block!=null) {
                    getSelf().tell(new BlockAcceptedMessage(block),ActorRef.noSender());
                }
                return new Success(block, witness.getName(), now);
            }

		}
	}

	private void tryStartProduce(Long now){
		if (chain.isProducingBlock() == false) {
			Witness witness = getWitness(nextProduceTime(now, nextBlockTime(1)));
			if (!(witness.getPrivkey()==null)) {
				log.info("startProduceBlock");
				chain.startProduceBlock(PublicKey.apply(new BinaryData(witness.getPubkey())));
			}
		}
	}

    private void removeTransactionsInBlock(Block block) {
        block.getTransactions().forEach(tx->{
            if(txPool.containsKey(tx.id())){
                txPool.remove(tx.id());
            }
        });
    }
	
	/**
	 * 根据上一个区块时间，获取下一个区块期望时间
	 * @param nextN
	 * @return
	 */
	private Long nextBlockTime(int nextN) {
		if(nextN == 0) nextN =1;
		long headTime = chain.getLatestHeader().getTimeStamp();
		long slot = headTime / settings.getProduceInterval(); //ProduceInterval 生成区块间隔时间
		slot += nextN;
		return slot * settings.getProduceInterval();
	}

	/**
	 * 下一个区块生产时间
	 * @param now
	 * @param next
	 * @return
	 */
	private Long nextProduceTime(Long now,Long next) {
		if (now <= next) {
			return next;
		}else {
			long slot = now / settings.getProduceInterval();
			return slot * settings.getProduceInterval();
		}
	}

	/**
	 * 获取给定时间点的生产者
	 * @param timeMs time from 1970 in ms
	 * @return
	 */
	private Witness getWitness(Long timeMs) {
		long slot = timeMs / settings.getProduceInterval();
		long index = slot % (settings.getWitnessList().size() * settings.getProducerRepetitions());
		index /= settings.getProducerRepetitions();
		return settings.getWitnessList().get((int)index); //获取
	}

	/**
	 * 洗牌
	 * @param nowSec
	 * @param witnesses
	 * @return
	 */
	private Witness[] updateWitnessSchedule(Long nowSec, Witness[] witnesses) {
		Witness[] newWitness = witnesses;
		BigInteger nowHi = new BigInteger(nowSec.toString()).shiftLeft(32); // this << n
		BigInteger param = new BigInteger("2685821657736338717");
		int witnessNum = newWitness.length;
		for (int i = 0; i < witnessNum; i++) {
			BigInteger ii = BigInteger.valueOf(i);
			BigInteger k = ii.multiply(param).add(nowHi);
			k = k.xor(k.shiftRight(12));
			k = k.xor(k.shiftLeft(25));
			k = k.xor(k.shiftRight(27));
			k = k.multiply(param);

			int jmax = newWitness.length - i;
			int j = k.remainder(BigInteger.valueOf(jmax)).add(ii).intValue();

			Witness a = newWitness[i];
			Witness b = newWitness[j];
			newWitness[i] = b;
			newWitness[j] = a;
		}
		return newWitness;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(BlockAcceptedMessage.class, msg -> {
            tryStartProduce(Instant.now().toEpochMilli());
		}).match(ProducerStopMessage.class, msg -> {
            log.info("stopping producer task");
            task.cancel();
            getContext().stop(getSelf());
        }).match(SendRawTransaction.class, msg -> {
			BinaryData rawTx = msg.getRawTx();
			DataInputStream is = new DataInputStream(new ByteArrayInputStream(CryptoUtil.binaryData2array(rawTx)));
			val tx = Transaction.deserialize(is);
			if(tx.verifySignature()){
				List<UInt256> txList = new ArrayList<>();
				txList.add(tx.id());
				peerManager.tell(new BlockMessageImpl.InventoryMessage(new InventoryPayload(InventoryType.Tx ,txList)).pack(), getSelf());
				if(chain.addTransaction(tx)){
					getSender().tell(true, getSender());
				}
				else getSender().tell(true, getSender());
			}
			else getSender().tell(false, getSender());
		}).match(Boolean.class, msg ->{
			getSender().tell(msg, getSender());
		}).build();
	}


}
