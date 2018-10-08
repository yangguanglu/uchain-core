package com.uchain.core.consensus;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SortedMultiMap2<K1,K2,V> implements Iterable<Object>{
	private Map<K1,SortedMultiMap1<K2,V>> container;
	private String sortedMultiMap1SortType;
	public SortedMultiMap2(String sortType,String sortedMultiMap1SortType) {
		if ("reverse".equals(sortType)) {
			this.container = new TreeMap(new MapKeyComparatorReverse<K1>());
		} else {
			this.container = new TreeMap(new MapKeyComparator<K1>());
		}
		this.sortedMultiMap1SortType = sortedMultiMap1SortType;
	}
	
	public int size() {
		int sizeBig = 0;
		for (Map.Entry<K1, SortedMultiMap1<K2,V>> entry : container.entrySet()) {
			sizeBig = sizeBig + entry.getValue().size();
		}
		return sizeBig;
	}
	
	public boolean contains(K1 k1,K2 k2) {
		return container.containsKey(k1) && container.get(k1).contains(k2);
	}
	
	public List<V> get(K1 k1,K2 k2) {
		if (k1 instanceof Integer){
			String tempk1 = String.valueOf(k1);
			return container.get(tempk1).get(k2);
		}else{
			return container.get(k1).get(k2);
		}
	}

	public void put(K1 k1,K2 k2,V v) {
		if (k1 instanceof Integer){
			String tempk1 = String.valueOf(k1);
			if (k2 instanceof Integer){
				String tempk2 = String.valueOf(k2);
				if (!container.containsKey(tempk1)) {
					SortedMultiMap1<K2, V> sortedMultiMap1 = new SortedMultiMap1<>(sortedMultiMap1SortType);
					sortedMultiMap1.put((K2)tempk2, v);
					container.put((K1)tempk1, sortedMultiMap1);
				} else {
					container.get(tempk1).put((K2) tempk2, v);
				}
			}else if(k2 instanceof Boolean){
				Boolean tempk2 = Boolean.valueOf((Boolean)k2);
				if (!container.containsKey(tempk1)) {
					SortedMultiMap1<K2, V> sortedMultiMap1 = new SortedMultiMap1<>(sortedMultiMap1SortType);
					sortedMultiMap1.put((K2)tempk2, v);
					container.put((K1)tempk1, sortedMultiMap1);
				} else {
					container.get(tempk1).put((K2) tempk2, v);
				}
			}
		}else {
			if (!container.containsKey(k1)) {
				SortedMultiMap1<K2, V> sortedMultiMap1 = new SortedMultiMap1<>(sortedMultiMap1SortType);
				sortedMultiMap1.put(k2, v);
				container.put(k1, sortedMultiMap1);
			} else {
				container.get(k1).put(k2, v);
			}
		}
	}
	
	public List<V> remove(K1 k1,K2 k2) {
		String tempk1 = String.valueOf(k1);
		if(container.containsKey(tempk1)) {
			List<V> list = container.get(tempk1).remove(k2);
			if(list != null && list.size() > 0) {
				container.remove(tempk1);
			}
			return list;
		}else {
			return null;
		}
	}

	public ThreeTuple<K1,K2,V> head() {
		return iterator().next();
	}
    public SortedMultiMap2Iterator<K1,K2,V> iterator() {
    	return new SortedMultiMap2Iterator<>(container);
    }
}
