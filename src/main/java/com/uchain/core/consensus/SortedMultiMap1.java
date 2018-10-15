package com.uchain.core.consensus;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SortedMultiMap1<K, V> {
	private Map<K, List<V>> container = null;

	public SortedMultiMap1(String sortType) {
		if ("reverse".equals(sortType)) {
			this.container = new TreeMap(new MapKeyComparatorReverse<K>());
		} else {
			this.container = new TreeMap(new MapKeyComparator<K>());
		}
	}

	public int size() {
		int sizeBig = 0;
		for (Map.Entry<K, List<V>> entry : container.entrySet()) {
			sizeBig += entry.getValue().size();
		}
		return sizeBig;
	}

	public boolean contains(K k) {
		return container.containsKey(k);
	}

	public List<V> get(K k) {
//		if (k instanceof Integer) {
//			String tempk = String.valueOf(k);
//			return container.get(tempk);
//		}else if(k instanceof Boolean){
//			Boolean tempk = Boolean.valueOf((Boolean) k);
//			return container.get(tempk);
//		}else {
			return container.get(k);
//		}
	}

	public void put(K k, V v) {
//		if (k instanceof Integer){
//			String tempk = String.valueOf(k);
//			if (!container.containsKey(tempk)) {
//				List<V> list = Lists.newArrayList();
//				list.add(v);
//				container.put((K) tempk, list);
//			} else {
//				container.get(tempk).add(v);
//			}
//		}else if (k instanceof Boolean){
//			Boolean tempk = Boolean.valueOf((Boolean) k);
//			if (!container.containsKey(tempk)) {
//				List<V> list = Lists.newArrayList();
//				list.add(v);
//				container.put((K) tempk, list);
//			} else {
//				container.get(tempk).add(v);
//			}
//		}else {
			if (!container.containsKey(k)) {
//				List<V> list = Lists.newArrayList();
//				list.add(v);
				container.put(k, Lists.newArrayList());
			}
				container.get(k).add(v);

//		}
	}

	public List<V> remove(K k) {
        return container.remove(k);

	}

	public TwoTuple<K, V> head() {
		return iterator().next();
	}

	public SortedMultiMap1Iterator<K, V> iterator() {
		return new SortedMultiMap1Iterator(container);
	}
}
