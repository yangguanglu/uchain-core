package com.uchain;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.uchain.DTO.CommandSuffix;
import com.uchain.common.Serializabler;
import com.uchain.core.Block;
import com.uchain.core.LevelDBBlockChain;
import com.uchain.core.consensus.ForkBase;
import com.uchain.main.MainApp;
import com.uchain.network.message.BlockMessageImpl;
import lombok.val;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandReceiverService {
    static public String getBlocks(String query, ActorRef nodeActor, ActorRef producerActor, LevelDBBlockChain chain){
//        try {
        ForkBase forkBase = chain.getForkBase();
        List<Map.Entry<byte[], byte[]>> entryList= forkBase.getDb().scan();
        int forkBaseNum = entryList.size();
        System.out.println(forkBaseNum);
        return String.valueOf(forkBaseNum);
//        val blocks = new ArrayList<Block>(blockNum);
//        for(int i=0; i< blockNum; i++){
//            blocks.add(chain.getBlock(i));
//        }
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
