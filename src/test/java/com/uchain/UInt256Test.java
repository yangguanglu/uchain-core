package com.uchain;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: UInt256Test
 *
 * @Author: bridge.bu@chinapex.com 2018/9/21 16:19
 *
 * @Version: 1.0
 * *************************************************************/

import com.uchain.core.Account;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;
import lombok.val;
import org.junit.Test;
import scala.Array;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

public class UInt256Test {
    @Test(expected = IllegalArgumentException.class)
    public void testCtorNull(){
        new UInt256(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCtorWrongSize1(){
            byte[] arr = new byte[31];
            Arrays.fill(arr,(byte)0);
            new UInt256(arr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCtorWrongSize2(){
            byte[] arr = new byte[33];
            Arrays.fill(arr,(byte)0);
            new UInt256(arr);
    }

    @Test
    public void testEquals(){
        assert(!UInt256.Zero().equals(null));
        assert(UInt256.Zero().equals(UInt256.Zero()));
        try {
            val a = SerializerTest.testHash256();
            val b = SerializerTest.testHash256();
            val c = SerializerTest.testHash160();
            val d = SerializerTest.testHash160("Test");
            assert (a.equals(a));
            assert (a.equals(b));
            assert (!a.equals(c));
            assert (!a.equals(d));
            assert (!a.equals(null));
        }catch (Exception e){
            System.out.println("Catch Exception!");
        }
    }

    @Test
    public void testCompare(){
        assert(UInt256.Zero().compare(UInt256.Zero()) == 0);
        try {
            val a = SerializerTest.testHash256();
            val b = SerializerTest.testHash256();
            val c = SerializerTest.testHash256("Test");
            assert (a.compare(b) == 0);
            assert (a.compare(c) < 0);
            assert (c.compare(a) > 0);
        }catch (Exception e){
            System.out.println("Catch Exception!");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompareNull(){
        UInt256.Zero().compare(null);
    }

    @Test
    public void testSerialize() throws IOException{
        val bis = new ByteArrayInputStream(new byte[]{1,2});
        DataInputStream is = new DataInputStream(bis);
        val o = new SerializerTest(UInt256.deserialize(is),is);
        o.test(SerializerTest.testHash256());
        o.test(UInt256.Zero());
    }

}
