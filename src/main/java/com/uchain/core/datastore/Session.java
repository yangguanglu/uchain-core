package com.uchain.core.datastore;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: Session
 *
 * @Author: bridge.bu@chinapex.com 2018/9/26 14:45
 *
 * @Version: 1.0
 * *************************************************************/

import com.uchain.storage.LevelDbStorage;
import org.iq80.leveldb.WriteBatch;


public class Session {
    private LevelDbStorage db;

    public Session(){
    }
    public Session(LevelDbStorage db){
        this.db = db;
    }

    public WriteBatch onSet(byte[] key, byte[] v, WriteBatch batch) throws Exception{
        WriteBatch newBatch = originOrNew(batch);
        newBatch.put(key,v);
        return newBatch;
    }

    protected WriteBatch originOrNew(WriteBatch batch){
        return batch == null?db.getBatchWrite():batch;
    }

    public WriteBatch onDelete(byte[] key,WriteBatch batch){
        WriteBatch newBatch = originOrNew(batch);
        newBatch.delete(key);
        return newBatch;
    }

    public static byte[] byteMergerAll(byte[]... values) {
        int length_byte = 0;
        for (int i = 0; i < values.length; i++) {
            length_byte += values[i].length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < values.length; i++) {
            byte[] b = values[i];
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }
}
