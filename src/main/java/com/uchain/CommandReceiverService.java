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
//        try {
        BlockBase blockBase = chain.getBlockBase();
        List<Map.Entry<byte[], byte[]>> entryList= blockBase.getBlockStore().getDb().scan();
        int forkBaseNum = entryList.size();
        System.out.println(forkBaseNum);

        val blockNum = chain.getHeight();

//        val blocksNumConfirm = chain.getBlockBase();

        val blocks = new ArrayList<Block>(blockNum);
        try {
            JSONArray jsonArray = new JSONArray();
            for(int i=blockNum-1; i> 0; i--){
                blocks.add(chain.getBlock(i));
            }

//            for(Block block : blocks){
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put()
//            }

//            String jsonString = JSON.toJSONString(blocks, SerializerFeature.DisableCircularReferenceDetect);
//            return jsonString;
            return Serializabler.JsonMapperTo(blocks);
        }
        catch (Throwable e){
            e.printStackTrace();
            return "";
        }

//        val blocks = new ArrayList<Block>(blockNum);
////            return Serializabler.JsonMapperTo(blocks);
//            return String.valueOf(blockNum);
//        }
//        catch (IOException e){
//            e.printStackTrace();
//            return "";
//        }

    }

    public static String getBlock(String query, ActorRef nodeActor, ActorRef producerActor){
//        try {
            ArrayList<CommandSuffix> commandSuffixes = parseCommandSuffixes(query);
            if(commandSuffixes.size() ==1){
                val suffixParam = commandSuffixes.get(0).getSuffixParam();
                val suffixValue = commandSuffixes.get(0).getSuffixValue();
                if(suffixParam.matches("(?:-h|-height)")){
                    int heightValue = Integer.valueOf(suffixValue);
                    return "";
                }
            }
            return "";
//        }
//        catch (IOException e){
//            e.printStackTrace();
//            return "";
//        }
    }

    static public String sendRawTransaction(String query, ActorRef nodeActor, ActorRef producerActor, LevelDBBlockChain chain){

        ArrayList<CommandSuffix> commandSuffixes = parseCommandSuffixes(query);
        BinaryData privKeyBin = CryptoUtil.fromHexString(commandSuffixes.get(0).getSuffixValue());


        val privKey = PrivateKey.apply(privKeyBin);

        val toAddress = commandSuffixes.get(1).getSuffixValue();
        val assetId = commandSuffixes.get(2).getSuffixValue();
        val amount = BigDecimal.valueOf(Integer.valueOf(commandSuffixes.get(3).getSuffixValue()));
        val nonce = Integer.valueOf(commandSuffixes.get(4).getSuffixValue());

        val tx = new Transaction(TransactionType.Transfer, privKey.publicKey(), PublicKeyHash.fromAddress(toAddress),
                "", Fixed8.fromDecimal(amount), UInt256.fromBytes(CryptoUtil.binaryData2array(CryptoUtil.fromHexString(assetId))),
                (long)nonce, CryptoUtil.array2binaryData(BinaryData.empty),  CryptoUtil.array2binaryData(BinaryData.empty), 0x01, null);

        tx.sign(privKey);

        byte[] txBytes = Serializabler.toBytes(tx);
        Byte[] txBYTES = new Byte[txBytes.length];
        for (int i = 0; i < txBytes.length; i++){
            txBYTES[i] = txBytes[i];
        }
        List<Byte> txList = Arrays.asList(txBYTES);
        val txRawData = new BinaryData(txList);
        val rawTx = "{\"rawTx\":\""  + CryptoUtil.toHexString(txRawData)  + "\"}";
        return rawTx;
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
