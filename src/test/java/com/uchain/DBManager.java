package com.uchain;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: DBManager
 *
 * @Author: bridge.bu@chinapex.com 2018/10/16 9:40
 *
 * @Version: 1.0
 * *************************************************************/

import com.uchain.storage.LevelDbStorage;
import org.apache.commons.collections.map.HashedMap;

import java.io.File;
import java.util.Map;

public class DBManager {
    private static final Map<String, LevelDbStorage> dbs = new HashedMap();
    private static String testClass;
    private final static Map<String, DBManager> dbManagers = new HashedMap();

    public DBManager(String testClass){
        this.testClass = testClass;
    }

    public static LevelDbStorage openDB(String dir){
        if (!dbs.containsKey(dir)) {
            LevelDbStorage db = LevelDbStorage.open(dir);
            dbs.put(dir, db);
        }
        return dbs.get(dir);
    }

    public static void cleanUp() {
        for(Map.Entry db : dbs.entrySet()){
            ((LevelDbStorage)db.getValue()).close();
        }
        deleteDir(testClass);
    }

    private static void deleteDir(String dir){
        try {
            //递归删除
            File scFileDir = new File(dir);
            File TrxFiles[] = scFileDir.listFiles();
            for(File curFile:TrxFiles ){
                curFile.delete();
            }
            //删除空目录
            scFileDir.delete();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static LevelDbStorage open(String testClass, String dir) {
        if (!dbManagers.containsKey(testClass)) {
            DBManager dbMgr = new DBManager(dir);
            dbManagers.put(testClass, dbMgr);
        }
        return dbManagers.get(testClass).openDB(dir);
    }

    public static void close(String testClass, String dir ) {
        DBManager dbMgr = dbManagers.get(testClass);

        if (dbMgr.dbs.containsKey(dir)) {
            dbMgr.dbs.get(dir).close();
            dbMgr.dbs.remove(dir);
        }
    }

    public static void clearUp(String testClass) {
        DBManager dbMgr = dbManagers.get(testClass);
        dbMgr.cleanUp();
        dbManagers.remove(testClass);
    }
}
