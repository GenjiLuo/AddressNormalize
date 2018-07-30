package com.jhzhang.address.normalizer.test.tools;

import com.jhzhang.address.normalizer.common.Level;

import java.util.*;

public class DebugUtil {
    /**
     * 打印数组["1","2","3"]
     *
     * @param objects
     */
    public static void printList(Object[] objects) {
        StringBuffer br = new StringBuffer();
        br.append("[");
        for (Object o : objects) {
            br.append("\"").append(o).append("\"").append(",");
        }
        br.setLength(br.length() - 1);
        br.append("]");
        System.out.println(br);
    }

    public static void printList(List<?> objects) {
        StringBuffer br = new StringBuffer();
        br.append("[");
        for (Object o : objects) {
            br.append("\"").append(o.toString()).append("\"").append(",");
        }
        br.setLength(br.length() - 1);
        br.append("]");
        System.out.println(br);
    }

    /**
     * 打印map
     *
     * @param map
     * @param isPretty
     */
    public static StringBuffer getHashMapStr(HashMap<?, Integer> map, boolean isPretty) {
        StringBuffer br = new StringBuffer();
        br.append("{");
        for (Map.Entry<?, Integer> entry : map.entrySet()) {
            br.append("\"").append((Object)(entry.getKey()).toString()).append("\"").append(":");
            br.append(entry.getValue()).append(",");
        }
        br.setLength(br.length() - 1);
        br.append("}");
        return br;
    }

    public static StringBuffer getHashMapStr(HashMap<Level, HashSet> map) {
        StringBuffer br = new StringBuffer();
        br.append("{");
        for (Map.Entry<Level, HashSet> entry : map.entrySet()) {
            br.append("\"").append(entry.getKey()).append("\"").append(":");
            br.append(entry.getValue()).append(",");
        }
        br.setLength(br.length() - 1);
        br.append("}");
        return br;
    }

    /**
     * 打印treeMap
     *
     * @param map
     * @param isPretty
     */
    public static void printTreeMap(TreeMap<String, HashMap<String, Integer>> map, boolean isPretty) {
        StringBuffer br = new StringBuffer();
        br.append("{");
        for (Map.Entry<String, HashMap<String, Integer>> entry : map.entrySet()) {
            br.append("\"").append(entry.getKey()).append("\"").append(":");
            br.append(getHashMapStr(entry.getValue(), isPretty)).append(",");
        }
        br.setLength(br.length() - 1);
        br.append("}");
        System.out.println(br);
    }

    public static void printTreeMap(TreeMap<String, HashMap<String, Integer>> map) {
        printTreeMap(map, true);
    }

    public static void printHashMap(HashMap<String, Integer> map){
        System.out.println(getHashMapStr(map,false));
    }

    public static void printList(int[] ints) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i : ints){
            sb.append(i).append(", ");
        }
        sb.setLength(sb.length()-2);
        sb.append("]");
        System.out.println(sb.toString());
    }
}
