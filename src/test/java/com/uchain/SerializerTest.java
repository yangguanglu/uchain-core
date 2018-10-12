package com.uchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;
import com.uchain.crypto.Fixed8;
import org.junit.Test;

import com.uchain.common.Serializable;
import com.uchain.common.Serializabler;
import com.uchain.core.Account;
import com.uchain.crypto.Crypto;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;

import lombok.val;

public class SerializerTest<A extends Serializable> {


    static boolean eqComparer(Serializable x, Serializable y){
        return x.equals(y);
    }

    @Test
    public void test() throws IOException{
        Account value = null;
        val bos = new ByteArrayOutputStream();
        val os = new DataOutputStream(bos);
        Serializabler.write(os, value);
        val ba = bos.toByteArray();
        val bis = new ByteArrayInputStream(ba);
        val is = new DataInputStream(bis);
        val accountDeserializer = Account.deserialize(is);

    }

    public static UInt256 testHash256(String str) throws IOException {
        return UInt256.fromBytes(Crypto.hash256(str.getBytes("UTF-8")));
    }

    public static  UInt256 testHash256() throws IOException{
        String  str = "test";
        return UInt256.fromBytes(Crypto.hash256(str.getBytes("UTF-8")));

    }

    public static UInt160 testHash160(String str) throws IOException {
        return UInt160.fromBytes(Crypto.hash160(str.getBytes("UTF-8")));
    }

    public static UInt160 testHash160() throws IOException {
        String str = "test";
        return UInt160.fromBytes(Crypto.hash160(str.getBytes("UTF-8")));
    }

    @Test
    public void Test256() throws IOException{
        UInt256 value = testHash256();
        System.out.println("before:==>"+value);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(bos);
        Serializabler.write(os, value);

        val ba = bos.toByteArray();
        val bis = new ByteArrayInputStream(ba);
        val is = new DataInputStream(bis);
        UInt256 test256 = UInt256.deserialize(is);
        System.out.println("after:==>"+test256);

        assert (value.equals(test256));

    }

    @Test
    public void Test160() throws IOException{
        UInt160 value = testHash160();
        System.out.println("before:==>"+value);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(bos);
        Serializabler.write(os, value);

        val ba = bos.toByteArray();
        val bis = new ByteArrayInputStream(ba);
        val is = new DataInputStream(bis);
        UInt160 test160 = UInt160.deserialize(is);
        System.out.println("after:==>"+test160);

        assert (value.equals(test160));

    }

    @Test
    public void TestMap()throws Exception{
        Map<UInt256, Fixed8> value = Maps.newLinkedHashMap();
        value.put(testHash256(),new Fixed8(1000000000));
        value.put(testHash256("Hello"),new Fixed8(2000000000));
        System.out.println("before:==>"+value);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(bos);
        Serializabler.writeMap(os, value);

        val ba = bos.toByteArray();
        val bis = new ByteArrayInputStream(ba);
        val is = new DataInputStream(bis);
        Map<UInt256, Fixed8> testMap = Serializabler.readMap(is,true);
        System.out.println("after:==>"+testMap);

        for(Map.Entry obj:value.entrySet()){
            assert (testMap.get(obj.getKey()).equals(obj.getValue()));
        }
    }

}
