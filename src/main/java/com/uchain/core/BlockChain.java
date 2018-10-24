package com.uchain.core;

import com.uchain.crypto.PrivateKey;
import com.uchain.crypto.PublicKey;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;

import java.util.Map;

public interface BlockChain extends Iterable<Block>{

    BlockHeader getLatestHeader();

    int getHeight();

    long getHeadTime();

    long headTimeSinceGenesis();

    BlockHeader getHeader(UInt256 id);

    BlockHeader getHeader(int index);
    
    UInt256 getNextBlockId(UInt256 id);

    Block getBlock(int height);

    Block getBlock(UInt256 id);

    Boolean containBlock(UInt256 id);

    Transaction getPendingTransaction(UInt256 txid);
    
    Block getBlockInForkBase(UInt256 id);

    void startProduceBlock(PublicKey producer);

    boolean produceBlockAddTransaction(Transaction tx);

    Block produceBlockFinalize(PublicKey producer, PrivateKey privateKey, Long timeStamp);

    Boolean isProducingBlock();

    Boolean addTransaction(Transaction tx);

    Boolean tryInsertBlock(Block block,Boolean doApply);

//    Transaction getTransaction(UInt256 id);
//
//    boolean containsTransaction(UInt256 id);

//    boolean verifyBlock(Block block);
//
//    boolean verifyTransaction(Transaction tx);

    Map<UInt256, Long> getBalance(UInt160 address);

    Account getAccount(UInt160 address);

    String getGenesisBlockChainId();

    void close();
}