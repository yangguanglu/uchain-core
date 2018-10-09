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

import com.uchain.core.datastore.keyvalue.IntKey;
import com.uchain.storage.Batch;
import com.uchain.storage.LevelDbStorage;
import lombok.val;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SessionManger {

    private LevelDbStorage db;

    private byte[] prefix;

    private RollSession session;

    private List<RollSession> sessions;

    private int level = 1;

    private Session defaultSession = new Session();

    private IntKey util = new IntKey();

    public SessionManger(LevelDbStorage db/*, RollSession session*/){
        this.db = db;
        this.prefix = Session.byteMergerAll(util.toBytes(StoreType.getStoreType(StoreType.Data)), util.toBytes(DataType.getDataType(DataType.Session)));
        /*this.session = new RollSession(db,prefix,level);
        this.sessions.add(this.session);*/
        init();
    }

    public int getLevel(){
        return level;
    }

    public List getLevels(){
        List res = new ArrayList();
        sessions.forEach(session1 -> {
            res.add(session1.getLevel());
        });
        return res;
    }

    private void init(){
        DBIterator iterator = db.db.iterator();
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

        Session res=null;
        for(RollSession ss:sessions){
            if(ss.getLevel() == level -1) {
                res = ss;
                break;
            }
        }
        assert(res != null);
    }

    private Boolean reloadSessions(DBIterator iterator){
        val kv = iterator.peekNext();
        byte[] key = kv.getKey();
        if(key.toString().startsWith(prefix.toString())){
            byte[] value = kv.getValue();
            if(key.length > prefix.length){
                BigInteger version = new BigInteger(key.toString().substring(prefix.length));
                RollSession rsession = new RollSession(db,prefix,new Integer(version.toString()));
                sessions.add(rsession);
                rsession.init(value);
            }else {
                level = Integer.valueOf(new BigInteger(value).toString());
            }
            iterator.next();
            return false;
        }else{
            return true;
        }
    }
    public Batch beginSet(byte[] k, byte[] v, Batch batch) throws Exception{
        Session session = sessions.get(sessions.size());
        if(null == session){
            session = defaultSession;
        }

        return session.onSet(k,v,batch);

    }

    public Batch beginDelete(byte[] k,Batch batch){
        Session session = sessions.get(sessions.size());
        if(null == session){
            session = defaultSession;
        }

        return session.onDelete(k,batch);
    }
    //關閉會話
    public void commit(){
        sessions.forEach(session1 -> {
            sessions.remove(0);
            session1.close();
        });
    }
    //關閉當前版本以前的會話
    public void commit(int revision){
        sessions.forEach(session1 -> {
            if(level >= session1.getLevel()){
                sessions.remove(session1);
                session1.close();
            }
        });

    }

    public void rollBack() throws IOException{
        RollSession session1 = sessions.get(sessions.size() - 1 );
        WriteBatch batch = db.createBatchWrite();
        session1.rollBack(batch);
        batch.put(prefix,util.toBytes(level-1));
    }

    public RollSession newSession() throws IOException{
        RollSession sessionRoll = new RollSession(db, prefix, level);
        WriteBatch batch = db.createBatchWrite();
        batch.put(prefix,new BigInteger(String.valueOf(level+1)).toByteArray());
        sessionRoll.init(batch);

        sessions.add(sessionRoll);
        level += 1;
        return sessionRoll;
    }
}
