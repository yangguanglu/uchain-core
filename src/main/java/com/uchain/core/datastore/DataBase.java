package com.uchain.core.datastore;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: DataBase
 *
 * @Author: bridge.bu@chinapex.com 2018/9/28 13:36
 *
 * @Version: 1.0
 * *************************************************************/

import com.uchain.core.Account;
import com.uchain.core.datastore.keyvalue.AccountValue;
import com.uchain.core.datastore.keyvalue.StringKey;
import com.uchain.core.datastore.keyvalue.UInt160Key;
import com.uchain.crypto.Fixed8;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;
import com.uchain.main.DataBaseSetting;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class DataBase {

    private static final Logger log = LoggerFactory.getLogger(DataBase.class);
    private DataBaseSetting settings;
    private LevelDbStorage db;

    private AccountStore accountStore;
    private NameToAccountStore nameToAccountStore;

    public DataBase(DataBaseSetting settings){
        this.db = ConnFacory.getInstance(settings.getDir());
        this.settings = settings;
        init();
    }

    private  void init(){
        accountStore = new AccountStore(db, settings.getCacheSize(), DataStoreConstant.AccountPrefix,
                new UInt160Key(), new AccountValue());
        nameToAccountStore = new NameToAccountStore(db, settings.getCacheSize(),
                DataStoreConstant.NameToAccountIndexPrefix,new StringKey(),new UInt160Key());
    }

    public Boolean nameExists(String name){
        return nameToAccountStore.contains(name);
    }

    public Boolean registerExists(UInt160 register){
        return accountStore.contains(register);
    }

    public Account getAccount(UInt160 address){
        return accountStore.get(address);
    }

    public Boolean setAccount(UInt160 fromUInt160,Account fromAccount,UInt160 toUInt160,Account toAccount){
        return true;
    }


    public Map<UInt256, Fixed8> getBalance(UInt160  address) {
        Account account = accountStore.get(address);
        return account.getBalances();
    }

    public void startSession() throws IOException {
        db.getSessionManger().newSession();
    }

    public void rollBack()throws IOException{
        db.getSessionManger().rollBack();
    }

    public void commit( int revision){
        db.getSessionManger().commit(revision);
    }

    public void commit(){
        db.commit();
    }

}
