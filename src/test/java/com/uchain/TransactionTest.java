package com.uchain;

import com.uchain.common.Serializabler;
import com.uchain.core.Transaction;
import com.uchain.core.TransactionType;
import com.uchain.core.consensus.SortedMultiMap2;
import com.uchain.core.consensus.SortedMultiMap2Iterator;
import com.uchain.core.consensus.ThreeTuple;
import com.uchain.crypto.*;
import lombok.val;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;

public class TransactionTest {
	@Test
	public void testSerialize() throws IOException {
		PrivateKey privKey = PrivateKey
				.apply(new BinaryData("d39d51a8d40336b0c73af180308fe0e4ee357e45a59e8afeebf6895ddf78aa2f"));

//		Transaction tx = new Transaction(TransactionType.Transfer,
//				PublicKey.apply(new BinaryData("0345ffbf8dc9d8ff15785e2c228ac48d98d29b834c2e98fb8cfe6e71474d7f6322")),
//				PublicKeyHash.fromAddress("APGMmPKLYdtTNhiEkDGU6De8gNCk3bTsME9"), "bob", Fixed8.Ten, UInt256.Zero(),
//				1L, new BinaryData(new ArrayList<>()), new BinaryData(new ArrayList<>()));
		UInt160 to = UInt160.fromBytes(Crypto.hash160(CryptoUtil.listTobyte(new BinaryData("0345ffbf8dc9d8ff15785e2c228ac48d98d29b834c2e98fb8cfe6e71474d7f6322").getData())));
        PublicKey minerCoinFrom = PublicKey.apply(new BinaryData("0345ffbf8dc9d8ff15785e2c228ac48d98d29b834c2e98fb8cfe6e71474d7f6322"));
        PublicKey producer = PublicKey.apply(new BinaryData("03b4534b44d1da47e4b4a504a210401a583f860468dec766f507251a057594e682"));
//        Transaction tx = new Transaction(TransactionType.Miner, minerCoinFrom,
//				to, "", Fixed8.Ten, UInt256.Zero(), 1L,
//                new BinaryData(new ArrayList<>()), new BinaryData(new ArrayList<>()));
		     val tx = new Transaction(TransactionType.Miner, minerCoinFrom,
				to, "", Fixed8.Ten, UInt256.Zero(),  1L,
				new BinaryData(new ArrayList<>()), new BinaryData(new ArrayList<>()),0x01,null);

		tx.sign(privKey);
		assert (tx.verifySignature() == true);

		val bos = new ByteArrayOutputStream();
		val os = new DataOutputStream(bos);
		Serializabler.write(os, tx);
		val ba = bos.toByteArray();
		val bis = new ByteArrayInputStream(ba);
		val is = new DataInputStream(bis);
		val transactionDeserializer = Transaction.deserialize(is);
		
		assert(tx.getTxType() == transactionDeserializer.getTxType());
		assert(tx.getFrom().toBin().getData().equals(transactionDeserializer.getFrom().toBin().getData()));
		assert(tx.getToPubKeyHash().toString().equals(transactionDeserializer.getToPubKeyHash().toString()));
		assert(tx.getToName().equals(transactionDeserializer.getToName()));
		assert(tx.getAmount().eq(transactionDeserializer.getAmount()));
		assert(tx.getAssetId().toString().equals(transactionDeserializer.getAssetId().toString()));
		assert(tx.getNonce()==transactionDeserializer.getNonce());
		assert(tx.getData().getData().equals(transactionDeserializer.getData().getData()));
		assert(tx.getSignature().getData().equals(transactionDeserializer.getSignature().getData()));
		assert(tx.getVersion() == transactionDeserializer.getVersion());


		SortedMultiMap2<Integer, Integer, UInt256> sortedMultiMap2 = new SortedMultiMap2<>(
				"asc", "reverse");
		UInt256 ss0 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss1 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss2 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss4 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss5 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss6 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));

		sortedMultiMap2.put(0, 0, ss0);
		sortedMultiMap2.remove(0, 0);
		sortedMultiMap2.put(0, 1, ss0);
		sortedMultiMap2.head();
		sortedMultiMap2.put(1, 0, ss1);
//		System.out.println("1111111");
		sortedMultiMap2.head();
		sortedMultiMap2.remove(1, 0);
		sortedMultiMap2.put(1, 1, ss1);
		//sortedMultiMap2.put(2, false, null)
		sortedMultiMap2.head();
		sortedMultiMap2.remove(0, 1);
		System.out.println(sortedMultiMap2.size());
//		sortedMultiMap2.remove(2, false);
//		sortedMultiMap2.put(2, true, ss2);
//		System.out.println(sortedMultiMap2.size());
//		System.out.println(sortedMultiMap2.get(2,true));
		SortedMultiMap2Iterator<Integer, Integer, UInt256> a = sortedMultiMap2.iterator();
		while (a.hasNext()){
			ThreeTuple<Integer, Integer, UInt256> b = a.next();
			System.out.println(b.first+"  "+b.second);
		}
	}
}
