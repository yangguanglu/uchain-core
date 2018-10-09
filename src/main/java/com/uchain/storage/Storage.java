package com.uchain.storage;

import java.util.List;

public interface Storage<Key, Value> {
    boolean containsKey(Key key);

    Value get(Key key);

	boolean set(Key key, Value value,Batch batch);

	boolean delete(Key key, Batch batch);

//    boolean batchWrite();

    void newSession();

    void commit(Integer revision);

    void commit();

    void rollBack();

	void close();

	Integer revision();

	List<Integer> uncommitted();
}
