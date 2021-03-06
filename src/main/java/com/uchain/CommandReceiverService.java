package com.uchain;

import akka.actor.ActorRef;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.uchain.DTO.CommandSuffix;
import com.uchain.common.Serializable;
import com.uchain.common.Serializabler;
import com.uchain.core.Block;
import com.uchain.core.LevelDBBlockChain;
import com.uchain.core.Transaction;
import com.uchain.core.TransactionType;
import com.uchain.core.consensus.ForkBase;
import com.uchain.core.datastore.BlockBase;
import com.uchain.crypto.*;
import com.uchain.main.MainApp;
import com.uchain.network.message.BlockMessageImpl;
import lombok.val;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommandReceiverService {
    static public String getBlocks(String query, ActorRef nodeActor, ActorRef producerActor, LevelDBBlockChain chain){
        BlockBase blockBase = chain.getBlockBase();
        List<Map.Entry<byte[], byte[]>> entryList= blockBase.getBlockStore().getDb().scan();
        val blockNum = chain.getHeight();
        val blocks = new ArrayList<Block>(blockNum);
        try {
            for(int i=blockNum-1; i> 0; i--){
                blocks.add(chain.getBlock(i));
            }

            return Serializabler.JsonMapperTo(blocks);
        }
        catch (Throwable e){
            e.printStackTrace();
            return "";
        }

    }

    public static Block getBlock(String query, ActorRef nodeActor, ActorRef producerActor, LevelDBBlockChain chain){
//        try {
            ArrayList<CommandSuffix> commandSuffixes = parseCommandSuffixes(query);
            if(commandSuffixes.size() ==1){
                val suffixParam = commandSuffixes.get(0).getSuffixParam();
                val suffixValue = commandSuffixes.get(0).getSuffixValue();
                if(suffixParam.contains("h")) return chain.getBlock(Integer.valueOf(suffixValue));
                if(suffixParam.contains("id")) return chain.getBlock(UInt256.parse(suffixValue));
            }
            return null;
//        }
//        catch (IOException e){
//            e.printStackTrace();
//            return "";
//        }
    }

    public static String getAccount(String query, ActorRef nodeActor, ActorRef producerActor){
//        try {
        ArrayList<CommandSuffix> commandSuffixes = parseCommandSuffixes(query);
        if(commandSuffixes.size() ==1){
            val suffixParam = commandSuffixes.get(0).getSuffixParam();
            val suffixValue = commandSuffixes.get(0).getSuffixValue();
            return suffixValue;
        }
        return "";
//        }
//        catch (IOException e){
//            e.printStackTrace();
//            return "";
//        }
    }

    static public BinaryData sendRawTransaction(String query, ActorRef nodeActor, ActorRef producerActor, LevelDBBlockChain chain){

        ArrayList<CommandSuffix> commandSuffixes = parseCommandSuffixes(query);

        BinaryData privKeyBin = CryptoUtil.fromHexString(commandSuffixes.get(0).getSuffixValue());


        val privKey = PrivateKey.apply(privKeyBin);

        val toAddress = commandSuffixes.get(1).getSuffixValue();
        val assetId = commandSuffixes.get(2).getSuffixValue();
        val amount = BigDecimal.valueOf(Integer.valueOf(commandSuffixes.get(3).getSuffixValue()));
        val nonce = Integer.valueOf(commandSuffixes.get(4).getSuffixValue());

        val keyHash = PublicKeyHash.fromAddress(toAddress);

        val tx = new Transaction(TransactionType.Transfer, privKey.publicKey(), PublicKeyHash.fromAddress(toAddress),
                "", Fixed8.fromDecimal(amount), UInt256.fromBytes(CryptoUtil.binaryData2array(CryptoUtil.fromHexString(assetId))),
                (long)nonce, CryptoUtil.array2binaryData(BinaryData.empty),  CryptoUtil.array2binaryData(BinaryData.empty), 0x01, null);
        try {
            tx.sign(privKey);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        byte[] txBytes = Serializabler.toBytes(tx);
        Byte[] txBYTES = new Byte[txBytes.length];
        for (int i = 0; i < txBytes.length; i++){
            txBYTES[i] = txBytes[i];
        }
        List<Byte> txList = Arrays.asList(txBYTES);
        val txRawData = new BinaryData(txList);
        val rawTx = "{\"rawTx\":\""  + CryptoUtil.toHexString(txRawData)  + "\"}";
        return txRawData;
//        val blocks = new ArrayList<Block>(blockNum);
////            return Serializabler.JsonMapperTo(blocks);
//            return String.valueOf(blockNum);
//        }
//        catch (IOException e){
//            e.printStackTrace();
//            return "";
//        }

    }

    public static ArrayList<CommandSuffix> parseCommandSuffixes(String height){
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            JavaType javaType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, CommandSuffix.class);
            return mapper.readValue(height, javaType);
        }
        catch (IOException e){
            e.printStackTrace();
            return new ArrayList<CommandSuffix>();
        }
    }
}
