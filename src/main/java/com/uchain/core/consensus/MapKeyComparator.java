package com.uchain.core.consensus;

import java.util.Comparator;

public class MapKeyComparator<K> implements Comparator<K>{
//	@Override
//	public int compare(K k1, K k2) {
//		if (k1 instanceof Integer) {
//			int value1 = ((Integer) k1).intValue();
//			int value2 = ((Integer) k2).intValue();
//			if(value1>=value2) {
//				return 1;
//			}else {
//				return -1;
//			}
//		}else if (k1 instanceof String) {
//            String str1 = (String) k1;
//            String str2 = (String) k2;
//            str1.compareTo(str2);
//		}else if (k1 instanceof Boolean) {
//		    Boolean b1 = ((Boolean) k1).booleanValue();
//		    Boolean b2 = ((Boolean) k2).booleanValue();
//		    return b1.compareTo(b2);
//		}
//		return 0;
//	}

    @Override
    public int compare(K o1, K o2) {
        if(o1 instanceof String && o2 instanceof String) {
            return ((String) o1).compareTo((String) o2);
        } else if(o1 instanceof Integer && o2 instanceof Integer) {
            return ((Integer) o1).compareTo((Integer) o2);
        }else if(o1 instanceof Boolean && o2 instanceof Boolean) {
            return ((Boolean) o1).compareTo((Boolean) o2);
        }
        return 0;
    }
}