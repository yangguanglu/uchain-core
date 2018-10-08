package com.uchain.core.datastore;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: DateBase
 *
 * @Author: bridge.bu@chinapex.com 2018/9/28 13:36
 *
 * @Version: 1.0
 * *************************************************************/

import com.uchain.core.Account;
import com.uchain.core.Transaction;
import com.uchain.core.datastore.keyvalue.AccountValue;
import com.uchain.core.datastore.keyvalue.StringKey;
import com.uchain.core.datastore.keyvalue.UInt160Key;
import com.uchain.crypto.Fixed8;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;
import com.uchain.main.Settings;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DateBase {

    private static final Logger log = LoggerFactory.getLogger(DateBase.class);
    private Settings settings;
    private LevelDbStorage db;

    private AccountStore accountStore;
    private NameToAccountStore nameToAccountStore;

    public DateBase(Settings settings){
        String path = settings.getChainSettings().getChain_forkDir();
        this.db = ConnFacory.getInstance(path);
        this.settings = settings;
        init();
    }

    private  void init(){
        accountStore = new AccountStore(db, 10, DataStoreConstant.AccountPrefix,
                new UInt160Key(), new AccountValue());
        nameToAccountStore = new NameToAccountStore(db, 10,
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

    public Fixed8 getBalance(UInt160  address) {
        Account account = accountStore.get(address);
        return account.getBalance(UInt256.parse(address.toAddressString()));
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
