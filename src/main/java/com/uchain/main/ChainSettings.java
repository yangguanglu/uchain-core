package com.uchain.main;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ChainSettings {
    private BlockBaseSettings blockBaseSettings;
    private DataBaseSettings dataBaseSettings;
    private ForkBaseSettings forkBaseSettings;
    private String minerCoinFrom;
    private BigDecimal minerAward;
    private Long chain_genesis_timeStamp;
    private String chain_genesis_publicKey;
    private String chain_genesis_privateKey;
    private String coinToAddr;

    public ChainSettings(BlockBaseSettings blockBaseSettings, DataBaseSettings dataBaseSettings,
                         ForkBaseSettings forkBaseSettings, String minerCoinFrom, BigDecimal minerAward,
                         Long chain_genesis_timeStamp, String chain_genesis_publicKey,
                         String chain_genesis_privateKey,String coinToAddr) {
        this.blockBaseSettings = blockBaseSettings;
        this.dataBaseSettings = dataBaseSettings;
        this.forkBaseSettings = forkBaseSettings;
        this.minerCoinFrom = minerCoinFrom;
        this.minerAward = minerAward;
        this.chain_genesis_timeStamp = chain_genesis_timeStamp;
        this.chain_genesis_publicKey = chain_genesis_publicKey;
        this.chain_genesis_privateKey = chain_genesis_privateKey;
        this.coinToAddr = coinToAddr;
    }
}
