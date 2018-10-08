package com.uchain.core.datastore;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: ByteArrayKey
 *
 * @Author: bridge.bu@chinapex.com 2018/9/28 15:37
 *
 * @Version: 1.0
 * *************************************************************/

import com.uchain.common.Serializabler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ByteArrayKey extends Serializabler {

    private byte[] bytes;
    public ByteArrayKey(byte[] bytes){
        this.bytes = bytes;
    }

    public byte[] getBytes(){
        return this.bytes;
    }
    public boolean equals(Object obj){
        if( obj == this){
            return true;
        }
        if (obj instanceof byte[]){
           if(this.bytes == obj){
               return true;
           }
        }
        return false;
    }

    public int hashCode(){
        return Arrays.hashCode(bytes);
    }

    public void serialize(DataOutputStream os) throws IOException {
        os.writeBytes(bytes.toString());
    }

}
