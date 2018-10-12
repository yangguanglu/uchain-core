package com.uchain;

import com.uchain.core.consensus.SortedMultiMap2;
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

		SortedMultiMap2<Integer, Integer, UInt256> sortedMultiMap2 = new SortedMultiMap2<>(
				"reverse", "reverse");
		UInt256 ss0 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss1 = UInt256.fromBytes(Crypto.hash256(("test"+1).getBytes("UTF-8")));
        UInt256 ss2 = UInt256.fromBytes(Crypto.hash256(("test"+2).getBytes("UTF-8")));
        UInt256 ss3 = UInt256.fromBytes(Crypto.hash256(("test"+3).getBytes("UTF-8")));

		sortedMultiMap2.put(1, 1, ss0);
		sortedMultiMap2.put(2, 1, ss1);
        sortedMultiMap2.put(3, 1, ss1);
        sortedMultiMap2.put(4, 1, ss1);
        sortedMultiMap2.put(5, 1, ss1);
        sortedMultiMap2.put(6, 1, ss1);
        sortedMultiMap2.put(7, 1, ss1);
        sortedMultiMap2.put(8, 1, ss1);
       sortedMultiMap2.put(9, 1, ss2);
        sortedMultiMap2.put(10, 1, ss0);
        sortedMultiMap2.put(11, 1, ss3);
//        sortedMultiMap2.put(12, 1, ss0);

        System.out.println(sortedMultiMap2.head().first);
	}
}
