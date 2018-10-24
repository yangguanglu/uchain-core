package com.uchain.storage;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: LowLevelWriteBatch
 *
 * @Author: bridge.bu@chinapex.com 2018/10/22 16:41
 *
 * @Version: 1.0
 * *************************************************************/

public interface LowLevelWriteBatch {
    void set(byte[] key, byte[] value);

    void delete(byte[] key);

    void close();
}
