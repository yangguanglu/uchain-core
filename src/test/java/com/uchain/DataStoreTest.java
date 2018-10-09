package com.uchain;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: DataStoreTest
 *
 * @Author: bridge.bu@chinapex.com 2018/9/26 10:07
 *
 * @Version: 1.0
 * *************************************************************/

import com.uchain.core.BlockHeader;
import com.uchain.core.datastore.HeaderStore;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.PrivateKey;
import com.uchain.crypto.PublicKey;
import com.uchain.crypto.UInt256;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;
import lombok.val;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataStoreTest {

    private static final List<String> dirs = new ArrayList<String>();
    private static final List<LevelDbStorage>  dbs = new ArrayList<LevelDbStorage>();

    public static LevelDbStorage openDB(String dir){
        LevelDbStorage db = null;//LevelDbStorage.open(dir);
        if (!dirs.contains(dir)) {
            dirs.add(dir);
        }
        dbs.add(db);
        return db;
    }

    public static void closeDB(LevelDbStorage db){
        db.close();
        dbs.remove(db);
    }

    //@AfterClass
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
    private static LevelDbStorage storage;

    private static BlockHeader createBlockHeader(){
        try {
            UInt256 prevBlock = SerializerTest.testHash256("prev");
            UInt256 merkleRoot = SerializerTest.testHash256("root");
            PublicKey producer = PublicKey.apply(new BinaryData("03b4534b44d1da47e4b4a504a210401a583f860468dec766f507251a057594e682")); // TODO: read from settings
            PrivateKey producerPrivKey = PrivateKey.apply(new BinaryData("7a93d447bffe6d89e690f529a3a0bdff8ff6169172458e04849ef1d4eafd7f86"));
            Long timeStamp = Instant.now().toEpochMilli();
            return new BlockHeader(0, timeStamp, merkleRoot, prevBlock, producer, new BinaryData("0000"),0x01,null);//null 代替Uint256
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    @Test
    public void testCommitRollBack(){
        try{
            LevelDbStorage db = DataStoreTest.openDB("test_RollBack");
            storage = ConnFacory.getInstance("\\.\\test_RollBack");
            HeaderStore store = new HeaderStore(storage, 10,null,null,null);
            BlockHeader blk1 = createBlockHeader();
            assert (blk1 != null);
            System.out.println("blk1===================="+blk1.id());
            db.getSessionManger().newSession();
            store.set(blk1.id(), blk1,null);
            assert(store.get(blk1.id()).equals(blk1));
            db.getSessionManger().rollBack();
            assert(store.get(blk1.id()) == null);
            BlockHeader blk2 = createBlockHeader();
            System.out.println("blk2===================="+blk2.id());
            store.set(blk2.id(), blk2,null);
            db.commit();
            assert(store.get(blk2.id()).equals(blk2));
            db.getSessionManger().rollBack();
            assert(store.get(blk2.id()).equals(blk2));
            db.getSessionManger().newSession();
            BlockHeader blk3 = createBlockHeader();
            System.out.println("blk3===================="+blk3.id());
            store.set(blk3.id(), blk3,null);
            assert(store.get(blk3.id()).equals(blk3));
            db.getSessionManger().rollBack();
            assert(store.get(blk2.id()).equals(blk2));
            assert(store.get(blk3.id()) == null);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
