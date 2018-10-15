package com.uchain;import com.uchain.core.consensus.MapKeyComparator;import java.util.Iterator;import java.util.Map;import java.util.TreeMap;public class SortDemo {    public static void main(String[] args) {        System.out.println("---------------- 自定义 排序结果-----------------");        createDefinitionSortTreeMap();    }    public static void createDefaultSortTreeMap() {        TreeMap<String, String> map = new TreeMap<String, String>();        init(map);        print(map);    }    public static void createDefinitionSortTreeMap() {////        TreeMap<String, String> map = new TreeMap<String, String>(new Comparator<String>() {//            @Override//            public int compare(String o1, String o2) {//                return o2.compareTo(o1);//            }////        });        TreeMap<String, String> map = new TreeMap(new MapKeyComparator<String>());        init(map);        System.out.println("aaaaaaa"+map.size());        System.out.println(map.containsKey("c"));        //map.remove("c");        print(map);    }    public static void init(Map<String, String> map) {        map.put("c", "1");        map.put("a", "2");        map.put("bb", "3");        map.put("b", "4");    }    public static void print(Map<String, String> map) {        System.out.println("bbbbbb"+map.size());        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();        while (it.hasNext()) {            Map.Entry<String, String> entry = it.next();            System.out.println(entry.getKey() + " : " + entry.getValue());        }    }}