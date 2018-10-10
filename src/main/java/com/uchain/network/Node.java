package com.uchain.network;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.common.collect.Lists;
import com.uchain.core.Block;
import com.uchain.core.BlockChain;
import com.uchain.core.producer.BlockAcceptedMessage;
import com.uchain.crypto.UInt256;
import com.uchain.network.message.BlockMessageImpl.*;
import com.uchain.network.message.*;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class Node extends AbstractActor{
	Logger log = LoggerFactory.getLogger(Node.class);
	private BlockChain chain;
	private ActorRef peerHandlerManager;
	private ActorRef producer;

	public Node(BlockChain chain, ActorRef peerHandlerManager,ActorRef producer) {
		this.chain = chain;
		this.peerHandlerManager = peerHandlerManager;
        this.producer = producer;
	}
	public static Props props(BlockChain chain, ActorRef peerHandlerManager,ActorRef producer) {
		return Props.create(Node.class, chain, peerHandlerManager,producer);
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(VersionMessage.class, msg -> {
			if (msg.getHeight() < chain.getHeight()) {
		          //sender() ! GetBlockMessage(height).pack
		    }
		}).match(BlockMessage.class, msg -> {
			log.info("received block "+msg.getBlock().height()+" ("+msg.getBlock().id()+")");
			if(chain.tryInsertBlock(msg.getBlock(),true)) {
				log.info("insert block "+msg.getBlock().height()+" ("+msg.getBlock().id()+") success");
                producer.tell(new BlockAcceptedMessage(msg.getBlock()),getSelf());
				peerHandlerManager.tell(new InventoryMessage(new InventoryPayload(InventoryType.Block, Arrays.asList(msg.getBlock().id()))), getSelf());
			}else {
				log.error("failed insert block "+msg.getBlock().height()+", ("+msg.getBlock().id()+") to db");
				if(msg.getBlock().height() > chain.getLatestHeader().getIndex()) {
					log.info("send GetBlocksMessage");
					getSender().tell(new GetBlocksMessage(new GetBlocksPayload(Arrays.asList(chain.getLatestHeader().id()), UInt256.Zero())).pack(), getSelf());
				}
			}
		}).match(GetBlocksMessage.class, msg -> {
			log.info("received GetBlocksMessage");
			List<UInt256> hashs = Lists.newArrayList();
			UInt256 hash = msg.getBlockHashs().getHashStart().get(0);
			if(hash.equals(UInt256.Zero())) {
				getSender().tell(new InventoryMessage(new InventoryPayload(InventoryType.Block, Arrays.asList(chain.getLatestHeader().id()))).pack(), getSelf());
			}else {
				hashs.add(hash);
				UInt256 next = chain.getNextBlockId(hash);
				while (next != null) {
					hashs.add(next);
					next = chain.getNextBlockId(next);
				}

				log.info("send InventoryMessage");
				getSender().tell(new InventoryMessage(new InventoryPayload(InventoryType.Block, hashs)).pack(), getSelf());
			}
		}).match(BlocksMessage.class, msg -> {
            log.info("received "+msg.getBlocksPayload().getBlocks().size()+" blocks");
            msg.getBlocksPayload().getBlocks().forEach(block -> {
                if (chain.tryInsertBlock(block, true)){
                    log.info("insert block "+block.height()+" ($"+block.id()+") success");
                    producer.tell(new BlockAcceptedMessage(block),getSelf());
                }else{
                    log.error("failed insert block "+block.height()+" ($"+block.id()+") to db");
                }
            });
            // try to get more blocks if have any
            getSender().tell(new GetBlocksMessage(new GetBlocksPayload(Arrays.asList(chain.getLatestHeader().id()), UInt256.Zero())).pack(), getSelf());
        }).match(InventoryMessage.class, msg -> {
			log.info("received Inventory");
	        if (msg.getInv().getInvType() == InventoryType.Block) {
	        	List<UInt256> newBlocks = Lists.newArrayList();
	        	msg.getInv().getHashs().forEach(h -> {
	        		if(chain.getBlock(h) == null) {
	        			if(chain.getBlockInForkBase(h) == null) {
	        				newBlocks.add(h);
	        			}
	        		}
	        	});
	        	if(newBlocks.size() > 0) {
	        		log.info("send GetDataMessage "+newBlocks);
	        		getSender().tell(new GetDataMessage(new Inventory(InventoryType.Block, newBlocks)).pack(), getSelf());
	        	}
	        }
		}).match(GetDataMessage.class, msg -> {
	        log.info("received GetDataMessage");
	        if(msg.getInv().getInvType() == InventoryType.Block) {
	        	int sendMax = 10;
	        	int sentNum = 0;
                List<Block> blocks = Lists.newArrayList();
                int hashNum = msg.getInv().getHashs().size();
                for (int i = 0; i < hashNum; i++) {
                    UInt256 h = msg.getInv().getHashs().get(i);
                    Block block = chain.getBlock(h);
                    if(block == null) {
                        block = chain.getBlockInForkBase(h);
                    }
                    if(block != null) {
                        if (sentNum < sendMax) {
                            blocks.add(block);
                            sentNum += 1;
                        }
                    }else {
                        log.error("received GetDataMessage but block not found");
                    }
                }
                if (blocks.size() > 0) {
                    getSender().tell(new BlocksMessage(new BlocksPayload(blocks)).pack(),getSelf());
                }
	        }
		}).build();
	}
}
