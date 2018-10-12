package com.uchain.core.datastore;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: SessionManger
 *
 * @Author: bridge.bu@chinapex.com 2018/9/27 11:07
 *
 * @Version: 1.0
 * *************************************************************/

import com.google.common.collect.Lists;
import com.uchain.core.datastore.keyvalue.IntKey;
import com.uchain.storage.Batch;
import lombok.val;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SessionManger {

    private DB db;

    private byte[] prefix;

    private RollSession session;

    private List<RollSession> sessions = new ArrayList<>();

    private int _revision = 1;

    private Session defaultSession = new Session();

    private IntKey util = new IntKey();

    public SessionManger(DB db/*, RollSession session*/){
        this.db = db;
        this.prefix = new byte[]{(byte) StoreType.getStoreType(StoreType.Data),(byte)DataType.getDataType(DataType.Session)};
        /*this.session = new RollSession(db,prefix,_revision);
        this.sessions.add(this.session);*/
        init();
    }

    public int get_revision(){
        return _revision;
    }

    public List<Integer> getRevisions(){
        List res = new ArrayList();
        sessions.forEach(session1 -> {
            res.add(session1.get_revision());
        });
        return res;
    }

    private void init(){
        DBIterator iterator = db.iterator();
        try {
            iterator.seek(prefix);

            boolean eof = false;
            while (!eof && iterator.hasNext()) {
                eof = reloadSessions(iterator);
            }
        } finally {
            try {
                iterator.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        if(sessions.size()>0) {
            RollSession res = sessions.get(sessions.size() - 1);
            assert (res.get_revision() != _revision);
        }
    }

    private Boolean reloadSessions(DBIterator iterator){
        val kv = iterator.peekNext();
        byte[] key = kv.getKey();
        if(key[0]==prefix[0]){
            byte[] value = kv.getValue();
            if(key.length > prefix.length){
                int count = key.length-prefix.length;
                byte[] bs = new byte[count];
                System.arraycopy(key, prefix.length, bs, 0, count);
                BigInteger version = new BigInteger(bs);
                RollSession rsession = new RollSession(db,prefix,version.intValue());
                sessions.add(rsession);
                rsession.init(value);
            }else {
                _revision = Integer.valueOf(new BigInteger(value).toString());
            }
            iterator.next();
            return false;
        }else{
            return true;
        }
    }
    public Batch beginSet(byte[] k, byte[] v, Batch batch) throws Exception{
        Session session;
        if(sessions.size()==0){
            session = defaultSession;
        }else{
            session = sessions.get(sessions.size() -1);
        }
        return session.onSet(k,v,batch);

    }

    public Batch beginDelete(byte[] k,Batch batch){
        Session session;
        if(sessions.size()==0){
            session = defaultSession;
        }else{
            session = sessions.get(sessions.size() -1);
        }
        return session.onDelete(k,batch);
    }

    public void commit(){
        RollSession session = null;
        if(sessions.size()!=0) {
            session = sessions.get(0);
        }
        if(session == null) return;
        sessions.remove(0);
        session.close();
    }

    public void commit(int revision){
        List<RollSession> sessionsTemp = Lists.newArrayList();
        sessions.forEach(session1 -> {
            if(session1.get_revision()  <= revision){
                sessionsTemp.add(session1);
                session1.close();
            }
        });
        sessions.removeAll(sessionsTemp);
    }

    public void rollBack() throws IOException{
        if(sessions.size() <= 0){
            return;
        }
        RollSession session1 = sessions.get(sessions.size() - 1 );
        WriteBatch batch = db.createWriteBatch();
        try {
            session1.rollBack(batch);
            batch.put(prefix,BigInteger.valueOf(new Long(_revision - 1)).toByteArray());
            db.write(batch);
            sessions.remove(session1);
            _revision = _revision -1;
        }
        finally {
            batch.close();
        }
    }

    public RollSession newSession() throws IOException{
        RollSession sessionRoll = new RollSession(db, prefix, _revision);
        WriteBatch batch = db.createWriteBatch();
        try{
            sessionRoll.init(batch);
            batch.put(prefix,new BigInteger(String.valueOf(_revision +1)).toByteArray());
            db.write(batch);
        }
        finally {
            batch.close();
        }

        sessions.add(sessionRoll);
        _revision += 1;
        return sessionRoll;
    }
}
