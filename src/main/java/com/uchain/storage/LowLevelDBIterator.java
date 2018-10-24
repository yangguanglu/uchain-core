package com.uchain.storage;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: LowLevelDBIterator
 *
 * @Author: bridge.bu@chinapex.com 2018/10/22 16:38
 *
 * @Version: 1.0
 * *************************************************************/

import java.util.Map;

public interface LowLevelDBIterator {
    void seek(byte[] prefix);

    Map<byte[],byte[]> next();

    Boolean hasNext();
}
