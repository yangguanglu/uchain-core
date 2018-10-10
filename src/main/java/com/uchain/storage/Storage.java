package com.uchain.storage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    Batch batchWrite();

    Map.Entry<byte[], byte[]> last() throws IOException;

	Integer revision();

	List<Integer> uncommitted();
}
