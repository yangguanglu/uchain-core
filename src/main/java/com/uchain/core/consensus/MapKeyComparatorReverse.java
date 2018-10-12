package com.uchain.core.consensus;

import java.util.Comparator;

class MapKeyComparatorReverse<K> implements Comparator<K>{
	@Override
	public int compare(K k1, K k2) {
		if (k1 instanceof Integer) {
			int value1 = ((Integer) k1).intValue(); 
			int value2 = ((Integer) k2).intValue(); 
			if(value1>=value2) {
				return -1;
			}else {
				return 1;
			}
		}else if (k1 instanceof String) {
            int value1 = Integer.parseInt((String) k1);
            int value2 = Integer.parseInt((String) k2);
            if(value1>=value2) {
                return -1;
            }else {
                return 1;
            }
		}else if (k1 instanceof Boolean) {
			Boolean b1 = ((Boolean) k1).booleanValue();
			Boolean b2 = ((Boolean) k2).booleanValue();
		    return b2.compareTo(b1);
		}
		return 0;
	}
}