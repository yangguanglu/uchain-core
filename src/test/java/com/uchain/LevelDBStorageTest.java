package com.uchain;

import com.google.common.collect.Maps;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

public class LevelDBStorageTest {

	private static LevelDbStorage storage;

	@BeforeClass
	public static void setUp(){
		storage = ConnFacory.getInstance("test_net/fork");
	}

	@AfterClass
	public static void tearDown(){
		storage.close();
	}

	@Test
	public void testSet(){
		try {
			assert (storage.set("testSet".getBytes(), "testSetValue".getBytes(), storage.batchWrite()));
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void testGet(){
		try {
			val key = "testGet".getBytes();
			val valueString = "testGetValue".getBytes();
			assert (storage.set(key, valueString, storage.batchWrite()));
			val value = storage.get(key);
			assert (value != null);
			assert (new String(value).equals(new String(valueString)));
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void testUpdate(){
		try {
			val key = "testUpdate".getBytes();
			val valueString = "testUpdateValue";
			val newValueString = "testUpdateValueNew";
			assert (storage.set(key, valueString.getBytes(), storage.batchWrite()));
			val value = storage.get(key);
			assert (value != null);
			assert (new String(value).equals(valueString));
			assert (storage.set(key, newValueString.getBytes(), storage.batchWrite()));
			val newValue = storage.get(key);
			assert (newValue != null);
			assert (new String(newValue).equals(newValueString));
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void testGetKeyNotExists(){
		val value = storage.get("testNotExistKey".getBytes());
		assert (value == null);
	}

	@Test
	public void testDelete(){
		val key = "testDelete".getBytes();
		val value = "testDeleteValue".getBytes();
		assert (storage.set(key, value, storage.batchWrite()));
		assert (storage.get(key) != null);
		storage.delete(key, storage.batchWrite());
		assert (storage.get(key) == null);
	}
	
	@Test
	public void  testScan(){
		Map<String, String> linkedHashMap = Maps.newLinkedHashMap();
	    for (int i = 1; i <= 10; i++) {
	    	val key = "key"+i;
	    	val value = "value"+i;
	    	linkedHashMap.put(key, value);
	    	if (storage.get(key.getBytes()) == null) {
		        assert(storage.set(key.getBytes(), value.getBytes(), storage.batchWrite()));
		    }
		}
	  }
}
