package com.uchain.storage;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: LowLevelDB
 *
 * @Author: bridge.bu@chinapex.com 2018/10/22 16:42
 *
 * @Version: 1.0
 * *************************************************************/

public interface LowLevelDB {
    byte[] get(byte[] key);

    void set(byte[] key,byte[] value);

    void delete(byte[] key);

    LowLevelDBIterator iterator();

//    def batchWrite(action: LowLevelWriteBatch => Unit)
}
