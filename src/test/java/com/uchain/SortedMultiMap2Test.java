package com.uchain;

import com.uchain.core.consensus.SortedMultiMap2;
import com.uchain.core.consensus.SortedMultiMap2Iterator;
import com.uchain.core.consensus.ThreeTuple;
import com.uchain.crypto.Crypto;
import com.uchain.crypto.UInt256;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class SortedMultiMap2Test {
	@Test
	public void testReverse() throws UnsupportedEncodingException {
//		SortedMultiMap2<String, String, UInt256> sortedMultiMap2 = new SortedMultiMap2<String, String, UInt256>(
//				"asc", "reverse");
//		UInt256 ss = UInt256.fromBytes(Crypto.hash256(("test").getBytes("UTF-8")));
//		sortedMultiMap2.put("2", "2", ss);
////		sortedMultiMap2.put(8, 8, ss);
//		sortedMultiMap2.put("2", "3", ss);
////		sortedMultiMap2.put(7, 7, ss);
//
//		ThreeTuple<String, String, UInt256> threeTuple = sortedMultiMap2.head();
//		System.out.println(threeTuple.first+"  "+threeTuple.second);
//		sortedMultiMap2.remove("2","3");
//		ThreeTuple<String, String, UInt256> threeTuple1 = sortedMultiMap2.head();
//		System.out.println(threeTuple1.first+"  "+threeTuple1.second);
//		ThreeTuple<String, String, UInt256> threeTuple2 = sortedMultiMap2.head();
//		System.out.println(threeTuple2.first+"  "+threeTuple2.second);
//		SortedMultiMap2Iterator<String, String, UInt256> sortedMultiMap2Iterator = sortedMultiMap2.iterator();
//		while(sortedMultiMap2Iterator.hasNext()){
//			ThreeTuple<String, String, UInt256> three1 = sortedMultiMap2Iterator.next();
//			Object object = three1.first;
//			if(object instanceof Integer){
//				System.out.println("true");
//			}else if(object instanceof String){
//				String aa = (String)object;
//				Integer.parseInt(aa);
//				System.out.println(aa);
//			}
//		}
//		sortedMultiMap2.remove("2","2");

		SortedMultiMap2<Integer, Boolean, UInt256> sortedMultiMap2 = new SortedMultiMap2<>(
				"asc", "reverse");
		UInt256 ss0 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss1 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss2 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss4 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss5 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss6 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));

//		sortedMultiMap2.put(0, false, ss0);
//		sortedMultiMap2.remove(0, false);
		sortedMultiMap2.put(0, true, ss0);
//		sortedMultiMap2.head();
//		sortedMultiMap2.put(1, false, ss1);
//		System.out.println("1111111");
//		sortedMultiMap2.head();
//		sortedMultiMap2.remove(1, false);
		sortedMultiMap2.put(1, true, ss1);
		//sortedMultiMap2.put(2, false, null)
//		sortedMultiMap2.head();
		sortedMultiMap2.remove(0, true);
//		System.out.println(sortedMultiMap2.size());
//		sortedMultiMap2.remove(2, false);
//		sortedMultiMap2.put(2, true, ss2);
//		System.out.println(sortedMultiMap2.size());
//		System.out.println(sortedMultiMap2.get(2,true));
		SortedMultiMap2Iterator<Integer, Boolean, UInt256> a = sortedMultiMap2.iterator();
		while (a.hasNext()){
			ThreeTuple<Integer, Boolean, UInt256> b = a.next();
			System.out.println(b.first+"  "+b.second);
		}
//		sortedMultiMap2.head();
	}
}
