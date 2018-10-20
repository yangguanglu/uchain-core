package com.uchain.main;

import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

@Getter
@Setter
public class Settings {
    public Settings(String config) {
        Properties properties = new Properties();
        BufferedReader bufferedReader = null;
        try{
            bufferedReader =new BufferedReader(
                    new FileReader(config));
            properties.load(bufferedReader);
        }catch (Exception e){
            e.printStackTrace();
        }
//        ResourceBundle resource = ResourceBundle.getBundle(config);
        this.nodeName = properties.getProperty("nodeName");
        this.bindAddress = properties.getProperty("bindAddress");
        this.knownPeers = properties.getProperty("knownPeers");
        this.agentName = properties.getProperty("agentName");
        this.maxPacketSize = properties.getProperty("maxPacketSize");
        this.localOnly = properties.getProperty("localOnly");
        this.appVersion = properties.getProperty("appVersion");
        this.maxConnections = properties.getProperty("maxConnections");
        this.connectionTimeout = properties.getProperty("connectionTimeout");
        this.upnpEnabled = properties.getProperty("upnpEnabled");
        this.handshakeTimeout = properties.getProperty("handshakeTimeout");
        this.controllerTimeout = properties.getProperty("controllerTimeout");

        this.server = properties.getProperty("server");
        this.updateEvery = properties.getProperty("updateEvery");
        this.timeout = properties.getProperty("timeout");


        BlockBaseSettings blockBaseSetting = new BlockBaseSettings(properties.getProperty("chain_blockBase_dir"),Boolean.valueOf(properties.getProperty("chain_blockBase_cacheEnabled")),
                Integer.parseInt(properties.getProperty("chain_blockBase_cacheSize")));
        DataBaseSettings dataBaseSetting = new DataBaseSettings(properties.getProperty("chain_dataBase_dir"),Boolean.valueOf(properties.getProperty("chain_dataBase_cacheEnabled")),
                Integer.parseInt(properties.getProperty("chain_dataBase_cacheSize")));
        ForkBaseSettings forkBaseSettings = new ForkBaseSettings(properties.getProperty("chain_forkBase_dir"),Boolean.valueOf(properties.getProperty("chain_forkBase_cacheEnabled")),
                Integer.parseInt(properties.getProperty("chain_forkBase_cacheSize")));

        this.chainSettings = new ChainSettings(blockBaseSetting, dataBaseSetting,forkBaseSettings,
                properties.getProperty("chain_miner"), Long.valueOf(properties.getProperty("chain_genesis_timeStamp")),
                properties.getProperty("chain_genesis_publicKey"), properties.getProperty("chain_genesis_privateKey"));

        String initialWitness = properties.getProperty("initialWitness");
        int produceInterval = Integer.parseInt(properties.getProperty("produceInterval"));
        int acceptableTimeError = Integer.parseInt(properties.getProperty("acceptableTimeError").trim());
        int producerRepetitions = Integer.parseInt(properties.getProperty("producerRepetitions").trim());
        this.consensusSettings = new ConsensusSettings(getWitness(initialWitness),produceInterval,acceptableTimeError,producerRepetitions);
        this.rpcServerSetting = new RPCServerSetting(properties.getProperty("rpcServerHost"), properties.getProperty("rpcServerPort"));
    }

    private String nodeName;
    private String bindAddress;
    private String knownPeers;
    private String agentName;
    private String maxPacketSize;
    private String localOnly;
    private String appVersion;
    private String maxConnections;
    private String connectionTimeout;
    private String upnpEnabled;
    private String handshakeTimeout;
    private String controllerTimeout;

    private String server;
    private String updateEvery;
    private String timeout;

    private ChainSettings chainSettings;
    private ConsensusSettings consensusSettings;
    private RPCServerSetting rpcServerSetting;

    private static List<Witness> getWitness(String json) {
        List<Witness> list = new ArrayList();
        try {
            JSONArray jsonObject = JSONArray.fromObject(json);
            for (Iterator<?> iterator = jsonObject.iterator(); iterator.hasNext();) {
                JSONObject job = (JSONObject) iterator.next();
                Iterator<?> it = job.keys();
                Witness witness = new Witness();
                int i = 0;
                while (it.hasNext()) {
                    if (i == 0) {
                        witness.setName((String) job.get(it.next()));
                    } else if (i == 1) {
                        witness.setPubkey((String) job.get(it.next()));
                    } else if (i == 2) {
                        witness.setPrivkey((String) job.get(it.next()));
                    }
                    i++;
                }
                list.add(witness);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
