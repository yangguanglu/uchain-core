package com.uchain.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.uchain.core.consensus.ForkBase;
import com.uchain.core.consensus.ForkItem;
import com.uchain.core.consensus.ThreeTuple;
import com.uchain.core.datastore.*;
import com.uchain.core.datastore.keyvalue.ProducerStatus;
import com.uchain.crypto.*;
import com.uchain.main.Settings;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class LevelDBBlockChain implements BlockChain{
	private static final Logger log = LoggerFactory.getLogger(LevelDBBlockChain.class);
    private Settings settings;
    private ForkBase forkBase;
    
    private PublicKey genesisProducer;
    private PrivateKey genesisProducerPrivKey;
    
    private HeaderStore headerStore;
    private HeightStore heightStore;
    private TransactionStore txStore;
    private AccountStore accountStore;
    private BlkTxMappingStore blkTxMappingStore;
    private HeadBlockDataStore headBlkStore;
    private NameToAccountStore nameToAccountStore;
    private ProducerStateStore prodStateStore;
    private PublicKey minerCoinFrom;
    private Fixed8 minerAward;
    private UInt160 genesisMinerAddress;
    private Transaction genesisTx;
    private BlockHeader genesisBlockHeader;
    private Block genesisBlock;
    private BlockHeader latestHeader;
    private ProducerStatus latestProdState;
    private BlockBase blockBase;
    private DataBase dataBase;
    private List<Transaction> pendingTxs = Lists.newArrayList();
    private List<Transaction> unapplyTxs = Lists.newArrayList();

    LevelDBBlockChain(Settings settings){
    	this.settings = settings;
        genesisProducer = PublicKey.apply(new BinaryData(settings.getChainSettings().getChain_genesis_publicKey())); // TODO: read from settings
        genesisProducerPrivKey = new PrivateKey(Scalar.apply(new BinaryData(settings.getChainSettings().getChain_genesis_privateKey())));

        blockBase = new BlockBase(settings.getChainSettings().getBlockBaseSettings());
        dataBase = new DataBase(settings.getChainSettings().getDataBaseSettings());
        forkBase = new ForkBase(settings);

        // TODO: folkBase is todo
        // TODO: zero is not a valid pub key, need to work out other method
        minerCoinFrom = PublicKey.apply(new BinaryData(settings.getChainSettings().getChain_miner()));   // 33 bytes pub key
        minerAward = Fixed8.Ten;

        genesisMinerAddress = UInt160.parse("f54a5851e9372b87810a8e60cdd2e7cfd80b6e31");
        genesisTx = new Transaction(TransactionType.Miner, minerCoinFrom,
                genesisMinerAddress, "", minerAward, UInt256.Zero(), 0L,
                CryptoUtil.array2binaryData(BinaryData.empty),CryptoUtil.array2binaryData(BinaryData.empty),0x01,null);

        genesisBlockHeader =  BlockHeader.build(0, settings.getChainSettings().getChain_genesis_timeStamp(),
                UInt256.Zero(), UInt256.Zero(), genesisProducer, genesisProducerPrivKey);

        genesisBlock = new Block(genesisBlockHeader,Transaction.transactionToArrayList(genesisTx));

        latestHeader= genesisBlockHeader;

        List<Transaction> pendingTxs = Lists.newArrayList();  // TODO: save to DB?
        List<Transaction> unapplyTxs = Lists.newArrayList();  // TODO: save to DB?

        populate();

    }

    private void populate(){
        if (blockBase.head() == null) {
            blockBase.add(genesisBlock);
        }

        if (forkBase.head() == null) {
            ThreeTuple<Block,List<ForkItem>, List<ForkItem>> threeTuple = forkBase.add(genesisBlock);
            confirmedOrSwitch(threeTuple);
        }

        assert(forkBase.head().getBlock().height() >= blockBase.head().getIndex());

        if (forkBase.head() != null)
            latestHeader = forkBase.head().getBlock().getHeader();
        else
            latestHeader = blockBase.head();
    }

    @Override
    public String getGenesisBlockChainId(){
        return genesisBlockHeader.id() + "";
    }

    @Override
    public BlockChainIterator iterator () {
        return new BlockChainIterator(this);
    } 
    
    @Override
    public int getHeight(){
        if(forkBase.head()!=null){
            return forkBase.head().getBlock().height();
        }else{
            return genesisBlockHeader.getIndex();
        }
    }

    @Override
    public long getHeadTime(){
        if(forkBase.head()!=null){
            return forkBase.head().getBlock().timeStamp();
        }else{
            return 0;
        }
    }
        
    @Override
    public BlockHeader getLatestHeader(){
    	ForkItem forkHead = forkBase.head();
    	if(forkHead != null) {
    		return forkHead.getBlock().getHeader();
    	}else {
    		return genesisBlockHeader;
    	}
    }
    
    @Override
    public long headTimeSinceGenesis(){
        return getLatestHeader().getTimeStamp() - genesisBlockHeader.getTimeStamp();
    }

    @Override
    public long getDistance(){
        val state = prodStateStore.get();
        assert (state != null);
        return state.getDistance();
    }
    
    @Override
    public BlockHeader getHeader(UInt256 id){
        if(forkBase.get(id)!=null){
            return forkBase.get(id).getBlock().getHeader();
        }else {
            return blockBase.getBlock(id).getHeader();
        }
    }

    @Override
    public BlockHeader getHeader(int height){
        if(forkBase.get(height)!=null){
            return forkBase.get(height).getBlock().getHeader();
        }else {
            return blockBase.getBlock(height).getHeader();
        }
    }
    
    @Override
    public UInt256 getNextBlockId(UInt256 id) {
    	UInt256 target = null;
    	Block block = getBlock(id);
    	if(block != null) {
    		Block nextBlock = getBlock(block.height() + 1);
    		if(nextBlock != null) {
    			target = nextBlock.id();
    		}
    	}
    	if(target == null) {
    		target = forkBase.getNext(id);
    	}
    	return target;
    }
    
    
    @Override
    public Block getBlock(UInt256 id){
        if(forkBase.get(id)!=null){
            return forkBase.get(id).getBlock();
        }else {
            return blockBase.getBlock(id);
        }
    }
    
    @Override
    public Block getBlock(int height){
        if(forkBase.get(height)!=null){
            return forkBase.get(height).getBlock();
        }else {
            return blockBase.getBlock(height);
        }
    }

    @Override
    public Block getBlockInForkBase(UInt256 id) {
    	ForkItem forkItem = forkBase.get(id);
    	if(forkItem == null) {
    		return null;
    	}else {
    		return getBlock(forkItem.getBlock().id());
    	}
    }

    @Override
    public void startProduceBlock(PublicKey producer){
        assert(pendingTxs.isEmpty());
        ForkItem forkHead = forkBase.head();
//        UInt160 to = UInt160.fromBytes(Crypto.hash160(CryptoUtil.listTobyte(new BinaryData("0345ffbf8dc9d8ff15785e2c228ac48d98d29b834c2e98fb8cfe6e71474d7f6322").getData())));
        Transaction minerTx = new Transaction(TransactionType.Miner, minerCoinFrom,
                producer.pubKeyHash(), "", minerAward, UInt256.Zero(), forkHead.getBlock().height()+1L,
               new BinaryData(new ArrayList<>()), new BinaryData(new ArrayList<>()),0x01,null);
        try {
            dataBase.startSession();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Boolean applied = applyTransaction(minerTx);
        assert(applied);
        pendingTxs.add(minerTx);

    }

    @Override
    public Boolean isProducingBlock(){
        return !pendingTxs.isEmpty();
    }


    @Override
    public boolean produceBlockAddTransaction(Transaction tx){
        assert(!pendingTxs.isEmpty());
        if (applyTransaction(tx)) {
            pendingTxs.add(tx);
            return true;
        }
        else
            return false;
    }

    @Override
    public Block produceBlockFinalize(PublicKey producer,PrivateKey privateKey,Long timeStamp){
        assert(!pendingTxs.isEmpty());
        ForkItem forkHead = forkBase.head();
        UInt256 merkleRoot = MerkleTree.root(pendingTxs.stream().map(v -> v.id()).collect(Collectors.toList()));
        BlockHeader header = BlockHeader.build(
                forkHead.getBlock().height() + 1, timeStamp, merkleRoot,
                forkHead.getBlock().id(), producer, privateKey);
        Block block = Block.build(header, pendingTxs);
        pendingTxs.clear();
        if (tryInsertBlock(block, false)) {
            return block;
        } else {
            return null;
        }
    }


    @Override
    public Boolean tryInsertBlock(Block block,Boolean doApply){
        boolean inserted = false;
        if (!pendingTxs.isEmpty()) {
            pendingTxs.forEach(tx ->{
                unapplyTxs.add(tx);
            });
            pendingTxs.clear();
            try {
                dataBase.rollBack();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (forkBase.head().getBlock().id().equals(block.prev())) {
            if (doApply == false) {
                ThreeTuple<Block,List<ForkItem>, List<ForkItem>> threeTuple = forkBase.add(block);
                log.info("threeTuplethreeTuplethreeTuplethreeTuple="+threeTuple);
                confirmedOrSwitch(threeTuple);
                inserted = true;
                latestHeader = block.getHeader();
            }
            else if (applyBlock(block)) {
                ThreeTuple<Block,List<ForkItem>, List<ForkItem>> threeTuple = forkBase.add(block);
                confirmedOrSwitch(threeTuple);
                inserted = true;
                latestHeader = block.getHeader();
            }
            else {
                log.info("block "+block.height()+" "+block.id()+" apply error");
                //forkBase.removeFork(block.id)
            }
        }
        else {
            log.info("received block added to minor fork chain. block "+block.height()+" "+block.id());
            ThreeTuple<Block,List<ForkItem>, List<ForkItem>> threeTuple = forkBase.add(block);
            confirmedOrSwitch(threeTuple);
            inserted = true;
        }
        return inserted;
    }

    private Boolean applyBlock(Block block){
        boolean applied = false;
        if (verifyBlock(block)) {
            try {
                dataBase.startSession();
            } catch (IOException e) {
                e.printStackTrace();
            }
            block.getTransactions().forEach(tx ->{
                applyTransaction(tx);
            });
            applied = true;
        }
        return applied;
    }


    private Boolean applyTransaction(Transaction tx){
        Account fromAccount;
        Map<UInt256, Fixed8> balances = Maps.newHashMap();
        if(dataBase.getAccount(tx.fromPubKeyHash())!=null){
            fromAccount = dataBase.getAccount(tx.fromPubKeyHash());
        }else {
            fromAccount = new Account(true, "", balances, 0L,0x01,null);
        }
        Account toAccount;
        if(dataBase.getAccount(tx.getToPubKeyHash())!=null){
            toAccount = dataBase.getAccount(tx.getToPubKeyHash());
        }else {
            toAccount = new Account(true, "", balances, 0L,0x01,null);
        }
        Map<UInt256, Fixed8> fromBalance = updateBalancesAccount(fromAccount.getBalances(),tx,"mus");
        Map<UInt256, Fixed8> toBalance = updateBalancesAccount(toAccount.getBalances(),tx,"add");
        dataBase.setAccount(tx.fromPubKeyHash(), new Account(true, fromAccount.getName(), fromBalance, fromAccount.getNextNonce() + 1,0x01,null),
                tx.getToPubKeyHash(), new Account(true, toAccount.getName(), toBalance, toAccount.getNextNonce(),0x01,null));

        return true;
    }


    private Map<UInt256, Fixed8> updateBalancesAccount(Map<UInt256, Fixed8> balancesInAccount,
                                                       Transaction tx,String flag) {
        Map<UInt256, Fixed8> updateBalances = new HashMap<>();
        balancesInAccount.forEach((assetId, balance) -> {
            if (tx.getAssetId().equals(assetId)) {
                Fixed8 balanceTemp;
                if("add".equals(flag)){
                    balanceTemp = balance.add(tx.getAmount());
                    updateBalances.put(assetId, balanceTemp);
                }else{
                    balanceTemp = balance.mus(tx.getAmount());
                    if(balanceTemp.getValue()>0L){
                        updateBalances.put(assetId, balanceTemp);
                    }
                }

            } else
                updateBalances.put(assetId, balance);
        });

        return updateBalances;
    }


    private Boolean verifyBlock(Block block){
        if (!verifyHeader(block.getHeader()))
            return false;
        else if (!verifyTxs(block.getTransactions()))
            return false;
        else if (!verifyRegisterNames(block.getTransactions()))
            return false;
        else
            return true;
    }

    private Boolean verifyTxs(List<Transaction> txs){
        boolean isValid = true;
        int txsNum = txs.size();
        for (int i = 0; i < txsNum; i++) {
            if(!verifyTransaction(txs.get(i))) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    private Boolean verifyTransaction(Transaction tx){
        if (tx.getTxType() == TransactionType.Miner) {
            return true;
        } else {
            boolean isValid = tx.verifySignature();
            return isValid && checkAmount();
        }
    }



    private Boolean verifyHeader(BlockHeader header) {
        return header.verifySig();
    }





    private boolean verifyRegisterNames(List<Transaction> transactions){
        boolean isValid = true;
        Set<String> newNames = new HashSet();
        Set<UInt160> registers = new HashSet();
        int txNum = transactions.size();
        for (int i = 0; i < txNum; i++) {
            Transaction tx = transactions.get(i);
            if(tx.getTxType() == TransactionType.RegisterName){
                String name = "";
                try {
                    name = new String(CryptoUtil.binaryData2array(tx.getData()), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if(name.length()!=10){// TODO: read "10" from config file
                    isValid = false;
                }
                if(newNames.contains(name)){
                    isValid = false;
                }
                if(registers.contains(tx.fromPubKeyHash())){
                    isValid = false;
                }
                newNames.add(name);
                registers.add(tx.fromPubKeyHash());
            }
        }
        Iterator newNamesIt = newNames.iterator();
        while (newNamesIt.hasNext()) {
            if(dataBase.nameExists((String)newNamesIt.next())){
                isValid = false;
                break;
            }
        }

        Iterator registersIt = registers.iterator();
        while (newNamesIt.hasNext()) {
            if(dataBase.registerExists((UInt160)registersIt.next())){
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    boolean checkAmount(){
        return true;
    }
    
    @Override
    public Map<UInt256, Long> getBalance(UInt160 address){
        Map<UInt256, Long> resultMap = Maps.newHashMap();
        Map<UInt256, Fixed8> map = dataBase.getBalance(address);
        map.forEach((k,v) -> resultMap.put(k,v.getValue()));
        return resultMap;
    }

    @Override
    public Account getAccount(UInt160 address) {
    	return dataBase.getAccount(address);
    }


    private void onConfirmed(Block block){
        if (block.height() > 0) {
            log.info("confirm block "+block.height()+" ("+block.id()+")");
            dataBase.commit(block.height());
            blockBase.add(block);
        }
    }

    private void printChain(String title,List<ForkItem> fork){
        StringBuilder str = new StringBuilder();
        fork.forEach(item -> {
            str.append(item.getBlock().id().toString().substring(0,6));
            str.append("->");
        });
        str.toString().substring(0,str.length()-2);
        log.info(title+": "+str);
    }

    private void onSwitch(List<ForkItem> from,List<ForkItem> to){
        printChain("old chain", from);
        printChain("new chain", to);
        from.forEach(forkItem -> {
            try {
                dataBase.rollBack();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        to.forEach(forkItem -> {
            applyBlock(forkItem.getBlock());
        });
    }

    private void confirmedOrSwitch(ThreeTuple<Block,List<ForkItem>, List<ForkItem>> threeTuple){
        if(threeTuple!=null){
            if(threeTuple.first!=null){
                onConfirmed(threeTuple.first);
            }
            if(threeTuple.second!=null){
                onSwitch(threeTuple.second,threeTuple.third);
            }
        }
    }
}
