package com.uchain.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.CryptoUtil;
import com.uchain.crypto.Fixed8;
import com.uchain.crypto.UInt256;
import com.uchain.util.Utils;
import lombok.val;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class Serializabler {

	static ObjectMapper mapper;

	static{
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	public static String  JsonMapperTo(Object object) throws IOException{
		String json = mapper.writeValueAsString(object);
		return json;
	}

	public static <T> T JsonMapperFrom(String content, Class<T> valueType) throws IOException{
		T object = mapper.readValue(content, valueType);
		return object;
	}

	public static byte[] toBytes(Serializable obj) {
		val bs = new ByteArrayOutputStream();
		val os = new DataOutputStream(bs);
		obj.serialize(os);
		return bs.toByteArray();
	}

	public static DataInputStream toInstance(byte[] bytes, Object object) throws IOException {
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		return new DataInputStream(is);
	}

	public static List<DataInputStream> toInstanceList(byte[] bytes) throws IOException {
		List<DataInputStream> list = new ArrayList<>();
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		for (int i = 1; i < is.readInt(); i++) {
			list.add(new DataInputStream(is));
		}
		return list;
	}

	public static void writeByteArray(DataOutputStream os, byte[] bytes) throws IOException {
        os.writeByte(bytes.length);
		os.write(bytes);
	}

	public static void writeString(DataOutputStream os, String str) throws UnsupportedEncodingException, IOException {
		writeByteArray(os, str.getBytes("UTF-8"));
	}

	public static void  write(DataOutputStream os, Serializable value) {
		value.serialize(os);
	}

	public static <T extends Serializable> void  writeSeq(DataOutputStream os, List<T> t){
		try {
			os.writeInt(t.size());
			t.forEach(v -> {
				v.serialize(os);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeMap(DataOutputStream os, Map<UInt256, Fixed8> map) throws IOException {
		os.writeInt(map.size());
		map.forEach((key, value) -> {
			key.serialize(os);
			value.serialize(os);
		});
	}
	
	public static byte[] readByteArray(DataInputStream is) throws IOException {
		int length = Utils.readVarInt(is).intValue();
		byte[] data = new byte[(int) length];

		Arrays.fill(data, (byte)0);
		is.read(data, 0, data.length);
		return data;
	}

	public static String readString(DataInputStream is) throws UnsupportedEncodingException, IOException {
		return new String(readByteArray(is), "UTF-8");
	}

//	public static long readVarInt(DataInputStream inputStream) throws IOException{
//		val value = inputStream.read();
//		if(value < 0xfd) return value;
//		if(value == 0xfd) return uint16(inputStream, ByteOrder.LITTLE_ENDIAN);
//		if(value == 0xfe) return uint32(inputStream, ByteOrder.LITTLE_ENDIAN);
//		if(value == 0xff) return uint64(inputStream, ByteOrder.LITTLE_ENDIAN);
//		else return 0;
//	}
//
//	static int uint16(DataInputStream inputStream, ByteOrder order)throws IOException{
//
//		byte[] bin = new byte[2];
//		inputStream.read(bin);
//		return uint16(CryptoUtil.array2binaryData(bin), order);
//	}
//
//	static int uint16(BinaryData input, ByteOrder order)throws IOException{
//		val buffer = ByteBuffer.wrap(CryptoUtil.binaryData2array(input)).order(order);
//		return buffer.getShort() & 0xFFFF;
//	}
//
//	static long uint32(DataInputStream inputStream, ByteOrder order)throws IOException{
//		byte[] bin = new byte[4];
//		inputStream.read(bin);
//		return uint32(CryptoUtil.array2binaryData(bin), order);
//	}
//
//	static long uint32(BinaryData input, ByteOrder order)throws IOException{
//		val buffer = ByteBuffer.wrap(CryptoUtil.binaryData2array(input)).order(order);
//		return buffer.getInt() & 0xFFFFFFFFL;
//	}
//
//	static long uint64(DataInputStream inputStream, ByteOrder order)throws IOException{
//		byte[] bin = new byte[8];
//		inputStream.read(bin);
//		return uint32(CryptoUtil.array2binaryData(bin), order);
//	}
//
//	static long uint64(BinaryData input, ByteOrder order)throws IOException{
//		val buffer = ByteBuffer.wrap(CryptoUtil.binaryData2array(input)).order(order);
//		return buffer.getLong();
//	}

//	public static<K,V> Map<K,V> readMap(DataInputStream is) throws IOException,ClassNotFoundException{
//		byte[] data = new byte[is.readInt()];
//		Arrays.fill(data, (byte)0);
//		is.read(data, 0, data.length);
//		ByteArrayInputStream byteInt=new ByteArrayInputStream(data);
//		ObjectInputStream objInt=/*new ObjectInputStream(byteInt)*/null;
//
//		if(byteInt.available() != 0){
//			objInt = new ObjectInputStream(byteInt);
//			return (Map<K,V>) objInt.readObject();
//		}
//		else return new HashMap<>();
//	}

	public static Map<byte[], byte[]> readMap(DataInputStream is) throws Exception{
		Map<byte[], byte[]> byteMap = new HashMap<>();
		val value = Utils.readVarInt(is);
		for(int i = 1; i<= value; i++){
			byteMap.put(readByteArray(is), readByteArray(is));
		}
		return byteMap;
	}

}
