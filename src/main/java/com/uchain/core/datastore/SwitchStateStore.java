package com.uchain.core.datastore;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: SwitchStateStore
 *
 * @Author: bridge.bu@chinapex.com 2018/10/18 14:22
 *
 * @Version: 1.0
 * *************************************************************/

import com.uchain.core.consensus.SwitchState;
import com.uchain.core.datastore.keyvalue.Converter;
import com.uchain.storage.Batch;
import com.uchain.storage.LevelDbStorage;

public class SwitchStateStore extends StateStore<SwitchState> {

    private byte[] prefixBytes;
    private Converter valConverter;
    private LevelDbStorage db;

    public SwitchStateStore(LevelDbStorage db, byte[] prefixBytes, Converter valConverter) {
        super(db, prefixBytes, valConverter);
        this.db = db;
        this.prefixBytes = prefixBytes;
        this.valConverter = valConverter;
    }

    @Override
    public SwitchState get() {
        return super.get();
    }

    @Override
    public boolean set(SwitchState value, Batch batch) {
        return super.set(value, batch);
    }

    @Override
    public void delete(Batch batch) {
        super.delete(batch);
    }
}
