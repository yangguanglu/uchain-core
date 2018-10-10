package com.uchain.main;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChainSettings {
    private BlockBaseSettings blockBaseSettings;
    private DataBaseSettings dataBaseSettings;
    private ForkBaseSettings forkBaseSettings;
    private String chain_miner;
    private Long chain_genesis_timeStamp;
    private String chain_genesis_publicKey;
    private String chain_genesis_privateKey;

    public ChainSettings(BlockBaseSettings blockBaseSettings, DataBaseSettings dataBaseSettings, ForkBaseSettings forkBaseSettings, String chain_miner, Long chain_genesis_timeStamp, String chain_genesis_publicKey, String chain_genesis_privateKey) {
        this.blockBaseSettings = blockBaseSettings;
        this.dataBaseSettings = dataBaseSettings;
        this.forkBaseSettings = forkBaseSettings;
        this.chain_miner = chain_miner;
        this.chain_genesis_timeStamp = chain_genesis_timeStamp;
        this.chain_genesis_publicKey = chain_genesis_publicKey;
        this.chain_genesis_privateKey = chain_genesis_privateKey;
    }
}
