package com.uchain;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: ForkBaseTest
 *
 * @Author: bridge.bu@chinapex.com 2018/10/11 14:17
 *
 * @Version: 1.0
 * *************************************************************/

import com.uchain.core.Block;
import com.uchain.core.BlockHeader;
import com.uchain.core.Transaction;
import com.uchain.core.consensus.ForkBase;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.PrivateKey;
import com.uchain.crypto.PublicKey;
import com.uchain.crypto.UInt256;
import com.uchain.main.Settings;
import com.uchain.main.Witness;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class ForkBaseTest {

    private PublicKey PubA = PublicKey.apply(new BinaryData("022ac01a1ea9275241615ea6369c85b41e2016abc47485ec616c3c583f1b92a5c8"));
    private PrivateKey PriA = PrivateKey.apply(new BinaryData("efc382ccc0358f468c2a80f3738211be98e5ae419fc0907cb2f51d3334001471"));
    private PublicKey PubB = PublicKey.apply(new BinaryData("0238eb90b322fac718ce10b21d451d00b7003a2a1de2a1d584a158d7b7ffee297b"));
    private PrivateKey PriB = PrivateKey.apply(new BinaryData("485cfb9f743d9997e316f5dca216b1c6adf12aa301c1d520e020269debbebbf0"));
    private PublicKey PubC = PublicKey.apply(new BinaryData("0234b9b7d2909231d143a6693082665837965438fc273fbc4c507996e41394c8c1"));
    private PrivateKey PriC = PrivateKey.apply(new BinaryData("5dfee6af4775e9635c67e1cea1ed617efb6d22ca85abfa97951771d47934aaa0"));
    private List<Witness> witnesses = new ArrayList<Witness>();

    public ForkBaseTest() {
        Witness A = new Witness();
        A.setName("A");
        A.setPubkey(PubA.hash160().toString());
        A.setPrivkey(PriA.toString());
        this.witnesses.add(A);
        Witness B = new Witness();
        B.setName("B");
        B.setPubkey(PubB.hash160().toString());
        B.setPrivkey(PriB.toString());
        this.witnesses.add(B);
    }

    private Block genesis = ForkBaseTest.genesisBlock();
    private static final List<String> dirs = new ArrayList<String>();
    private static final List<ForkBase>  dbs    = new ArrayList<ForkBase>();

    @AfterClass
    public static void cleanUp(){
        dbs.forEach(dbtmp->{dbtmp.close();});
        dirs.forEach(dirtmp->deleteDir(dirtmp));
    }

    private static void deleteDir(String dir){
        try {
            //递归删除
            //Directory(dir).deleteRecursively();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static Block genesisBlock() {
        PublicKey pub = PublicKey.apply(new BinaryData("03b4534b44d1da47e4b4a504a210401a583f860468dec766f507251a057594e682"));
        PrivateKey pri = PrivateKey.apply(new BinaryData("7a93d447bffe6d89e690f529a3a0bdff8ff6169172458e04849ef1d4eafd7f86"));

        BlockHeader genesisHeader = BlockHeader.build(
                0, Instant.now().toEpochMilli(),
                UInt256.Zero(), UInt256.Zero(), pub, pri);
        List<Transaction> queue = new ArrayList<Transaction>();
        return Block.build(genesisHeader, queue);
    }

    private static Block newBlock(PublicKey pub, PrivateKey pri, Block prevBlock) throws IOException {
        UInt256 root = SerializerTest.testHash256("test");
        long timeStamp = Instant.now().toEpochMilli();
        BlockHeader header = BlockHeader.build(
                prevBlock.height() + 1, timeStamp,
                root, prevBlock.id(), pub, pri);
        List<Transaction> queue = new ArrayList<Transaction>();
        return Block.build(header, queue);
    }

    public static ForkBase open(String dir, List<Witness> witnesses){
        Settings settings = new Settings("config");

        ForkBase forkBase = new ForkBase(settings);

        dbs.add(forkBase);
        dirs.add(dir);
        return forkBase;
    }

    @Test
    public void testHead() throws IOException{
        Block blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
        Block blk2a = ForkBaseTest.newBlock(PubA, PriA, blk1a);
        Block blk3a = ForkBaseTest.newBlock(PubA, PriA, blk2a);
        Block blk4a = ForkBaseTest.newBlock(PubA, PriA, blk3a);
        Block blk3b = ForkBaseTest.newBlock(PubB, PriB, blk2a);
        Block blk4b = ForkBaseTest.newBlock(PubB, PriB, blk3b);
        ForkBase forkBase = ForkBaseTest.open("forkBase_head", witnesses);
        System.out.println(forkBase.head());
        //由于forkBase是从config中读取，因此初始不是null
        //assert(forkBase.head()==null);
        forkBase.add(genesis);
        assert(forkBase.head().getBlock().equals(genesis));
        forkBase.add(blk1a);
        assert(forkBase.head().getBlock().equals(blk1a));
        forkBase.add(blk2a);
        assert(forkBase.head().getBlock().equals(blk2a));
        forkBase.add(blk3b);
        assert(forkBase.head().getBlock().equals(blk3b));
        forkBase.add(blk4b);
        assert(forkBase.head().getBlock().equals(blk4b));
//        assert(forkBase.get(blk1a.id()).isMaster());
    }

    @Test
    public void testGet()throws IOException{
        ForkBase forkBase = ForkBaseTest.open("forkBase_get", witnesses);
        assertBlock(forkBase,genesis);
        Block blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
        assertBlock(forkBase,blk1a);
        Block blk2a = ForkBaseTest.newBlock(PubA, PriA, blk1a);
        assertBlock(forkBase,blk2a);
        Block blk3a = ForkBaseTest.newBlock(PubA, PriA, blk2a);
        assertBlock(forkBase,blk3a,false, true, true, true);
    }

    public void assertBlock(ForkBase forkBase,Block block){
        assertBlock(forkBase,block,false,false,true,true);
    }

    public void assertBlock(ForkBase forkBase,Block block,Boolean beforeId,Boolean beforeHeight, Boolean afterId, Boolean afterHeight){
        assert((forkBase.get(block.id())==null) == beforeId);
        assert((forkBase.get(block.height())==null) == beforeHeight);
        forkBase.add(block);
        assert((forkBase.get(block.id())==null) == afterId);
        assert((forkBase.get(block.height())==null) == afterHeight);
    }

    @Test
    public void testGetNext() throws IOException{
        ForkBase forkBase = ForkBaseTest.open("forkBase_next", witnesses);
        assert(forkBase.getNext(genesis.id())==null);
        forkBase.add(genesis);
        assert(forkBase.getNext(genesis.id())==null);
        Block blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
        Block blk1b = ForkBaseTest.newBlock(PubA, PriA, genesis);
        Block blk2b = ForkBaseTest.newBlock(PubB, PriB, blk1b);
        forkBase.add(blk1a);
        assert(forkBase.getNext(genesis.id()).equals(blk1a.id()));
        forkBase.add(blk1b);
        assert(forkBase.getNext(genesis.id()).equals(blk1a.id()));
        forkBase.add(blk2b);
        assert(forkBase.getNext(genesis.id()).equals(blk1b.id()));
    }

    @Test
    public void testAdd()throws IOException{
        ForkBase forkBase = ForkBaseTest.open("forkBase_add", witnesses);
//        assert(forkBase.add(genesis));
//        assert(!forkBase.add(genesis));
        Block blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
//        assert(forkBase.add(blk1a))
        Block blk2a = ForkBaseTest.newBlock(PubA, PriA, blk1a);
        Block blk3a = ForkBaseTest.newBlock(PubA, PriA, blk2a);
//        assert(!forkBase.add(blk3a))
    }

    @Test
    public void testSwitch() throws IOException{
        Witness C = new Witness();
        C.setName("C");
        C.setPubkey(PubC.hash160().toString());
        C.setPrivkey(PriC.toString());
        this.witnesses.add(C);
        ForkBase forkBase = ForkBaseTest.open("forkBase_switch", witnesses);
        Block blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
        Block blk2a = ForkBaseTest.newBlock(PubA, PriA, blk1a);
        Block blk3a = ForkBaseTest.newBlock(PubA, PriA, blk2a);
        Block blk4a = ForkBaseTest.newBlock(PubA, PriA, blk3a);
        Block blk5a = ForkBaseTest.newBlock(PubA, PriA, blk4a);
        Block blk3b = ForkBaseTest.newBlock(PubB, PriB, blk2a);
        Block blk4b = ForkBaseTest.newBlock(PubB, PriB, blk3b);
        Block blk4c = ForkBaseTest.newBlock(PubC, PriC, blk3b);
        forkBase.add(genesis);
        forkBase.add(blk1a);
        forkBase.add(blk2a);
        forkBase.add(blk3b);
        forkBase.add(blk4b);
        assert(forkBase.get(blk1a.id()).isMaster());
        assert(forkBase.get(blk2a.id()).isMaster());
        assert(forkBase.get(blk3b.id()).isMaster());
        assert(forkBase.get(blk4b.id()).isMaster());
        forkBase.add(blk3a);
        forkBase.add(blk4a);
        assert(forkBase.get(blk1a.id()).isMaster());
        assert(forkBase.get(blk2a.id()).isMaster());
        assert(forkBase.get(blk3a.id()).isMaster());
        assert(forkBase.get(blk4a.id()).isMaster());
        forkBase.add(blk5a);
        assert(forkBase.get(blk3b.id()).isMaster());
        assert(forkBase.get(blk4b.id()).isMaster());
        assert(forkBase.get(blk3a.id()).isMaster());
        assert(forkBase.get(blk4a.id()).isMaster());
        assert(forkBase.get(blk5a.id()).isMaster());
        forkBase.add(blk4c);
        assert(forkBase.get(blk4b.id()).isMaster());
        assert(forkBase.get(blk3a.id()).isMaster());
        assert(forkBase.get(blk4a.id()).isMaster());
        assert(forkBase.get(blk5a.id()).isMaster());
        assert(forkBase.get(blk3b.id()).isMaster());
        assert(forkBase.get(blk4c.id()).isMaster());
    }

    @Test
    public void testRemoveFork()throws IOException{
        Witness C = new Witness();
        C.setName("C");
        C.setPubkey(PubC.hash160().toString());
        C.setPrivkey(PriC.toString());
        this.witnesses.add(C);
        ForkBase forkBase = ForkBaseTest.open("forkBase_removeFork", witnesses);
        Block blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
        Block blk2a = ForkBaseTest.newBlock(PubA, PriA, blk1a);
        Block blk3a = ForkBaseTest.newBlock(PubA, PriA, blk2a);
        Block blk4a = ForkBaseTest.newBlock(PubA, PriA, blk3a);
        Block blk5a = ForkBaseTest.newBlock(PubA, PriA, blk4a);
        Block blk3b = ForkBaseTest.newBlock(PubB, PriB, blk2a);
        Block blk4b = ForkBaseTest.newBlock(PubB, PriB, blk3b);
        Block blk3c = ForkBaseTest.newBlock(PubC, PriC, blk2a);
        forkBase.add(genesis);
        forkBase.add(blk1a);
        forkBase.add(blk2a);
        forkBase.add(blk3b);
        forkBase.add(blk4b);
        forkBase.add(blk3a);
        forkBase.add(blk4a);
        forkBase.add(blk5a);
//        assert(forkBase.removeFork(blk4a.id()));
        assert(forkBase.get(blk1a.id())!=null);
        assert(forkBase.get(blk2a.id())!=null);
        assert(forkBase.get(blk3a.id())!=null);
        assert(forkBase.get(blk3b.id())!=null);
        assert(forkBase.get(blk4b.id())!=null);
        assert(forkBase.get(blk4a.id())==null);
        assert(forkBase.get(blk5a.id())==null);
//        assert(forkBase.removeFork(blk2a.id));
        assert(forkBase.get(blk1a.id())!=null);
        assert(forkBase.get(blk2a.id())==null);
        assert(forkBase.get(blk3a.id())==null);
        assert(forkBase.get(blk3b.id())==null);
        assert(forkBase.get(blk4b.id())==null);
//        assert(!forkBase.removeFork(blk3c.id));
    }
    @Test
    public void testFork()throws IOException{
        ForkBase forkBase = ForkBaseTest.open("forkBase_fork", witnesses);
        Block blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
        Block blk2a = ForkBaseTest.newBlock(PubA, PriA, blk1a);
        Block blk3a = ForkBaseTest.newBlock(PubA, PriA, blk2a);
        Block blk4a = ForkBaseTest.newBlock(PubA, PriA, blk3a);
        forkBase.add(genesis);
        forkBase.add(blk1a);
        assert(forkBase.head().getBlock().id().equals(blk1a.id()));
        forkBase.add(blk2a);
        assert(forkBase.head().getBlock().id().equals(blk2a.id()));
        forkBase.add(blk3a);
        assert(forkBase.head().getBlock().id().equals(blk3a.id()));
        Block blk3b = ForkBaseTest.newBlock(PubB, PriB, blk2a);
        Block blk4b = ForkBaseTest.newBlock(PubB, PriB, blk3b);
        forkBase.add(blk3b);
        assert(forkBase.head().getBlock().id().equals(blk3b.id()));
        assert(forkBase.get(blk3a.id()).isMaster() == false);
        assert(forkBase.get(blk3b.id()).isMaster() == true);
        forkBase.add(blk4a);
        assert(forkBase.head().getBlock().id().equals(blk3b.id()));
        forkBase.add(blk4b);
        assert(forkBase.head().getBlock().id().equals(blk4b.id()));
    }

}


