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
import com.uchain.main.ForkBaseSettings;
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

    public ForkBase open(String dir, List<Witness> witnesses){
        /*def forkStr(title: String, fork: Seq[ForkItem]): String = {
                s"  $title: ${fork.map(blk => s"${blk.block.height}(${blk.block.id.toString.substring(0, 6)})").mkString(" <- ")}"
    }*/

        ForkBaseSettings settings = new ForkBaseSettings(dir, false, 0);

        ForkBase forkBase = new ForkBase(null);
        dbs.add(forkBase);
        dirs.add(dir);
        return forkBase;
    }

    @Test
    public void testHead(){

    }
}
