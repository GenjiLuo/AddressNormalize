package com.jhzhang.address.normalizer.util;

import com.jhzhang.address.normalizer.cache.AddressQuery;
import com.jhzhang.address.normalizer.cache.exception.AddressParseException;
import com.jhzhang.address.normalizer.cache.exception.InvalidAddressException;
import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.core.process.*;
import com.jhzhang.address.normalizer.cache.AddressQuery;
import com.jhzhang.address.normalizer.cache.exception.AddressParseException;
import com.jhzhang.address.normalizer.cache.exception.InvalidAddressException;

import java.util.LinkedList;
import java.util.List;

/**
 * 地址向量操作.
 */
public class VectorUtil {

    private static final int ELEMENT_LENG = 4;

    /**
     * 将Element包装成字符串.
     *
     * @return 包装后的结果
     */
    private static String transform(Element element) {
        String name = element.getName();
        String suffix = element.getSuffix();
        int offset = element.getStart();
        Level level = element.getLevel();
        return name + "/" + suffix + "/" + level + "/" + offset;
    }

    /**
     * 将一个列表包装成字符串.
     *
     * @param list 要序列化的的列表
     * @return 包装后的结果
     */
    private static String listToString(List<Element> list) {
        StringBuilder sb = new StringBuilder();
        if (list.size() <= 0) {
            return sb.toString();
        }
        for (int i = 0; i < list.size() - 1; i++) {
            Element element = list.get(i);
            sb.append(transform(element)).append(",");
        }
        sb.append(transform(list.get(list.size() - 1)));
        return sb.toString();
    }

    /**
     * 将vector序列化为字符串.
     *
     * @param vector 需要序列化的地址向量
     * @return 序列化之后的字符串
     */
    public static String transform(Vector vector) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.size() - 1; i++) {
            sb.append(listToString(vector.indexOf(i))).append("\t");
        }
        sb.append(listToString(vector.indexOf(vector.size() - 1)));
        return sb.toString();
    }

    /**
     * 将字符串反序列化为地址元素.
     *
     * @param str 需要反序列化的字符串
     * @return 反序列化结果
     */
    private static Element stringToElement(String str) throws IllegalArgumentException {
        if (str.isEmpty()) {
            return Element.EMPTY;
        }
        String[] strArr = str.split("/", -1);
        if (strArr.length != ELEMENT_LENG) {
            return null;
        }
        // TODO 没有完全的修改Elment参数
        return new Element(strArr[0],
                strArr[1],
                Level.valueOf(strArr[2]),
                Integer.parseInt(strArr[3]), -1);

    }

    /**
     * 将字符串反序列化为地址元素列表.
     *
     * @param str 需要反序列化的字符串
     * @return 反序列化结果
     * @throws AddressParseException 抛出反序列化失败异常
     */
    private static List<Element> stringToList(String str) throws AddressParseException {
        List<Element> list = new LinkedList<>();
        String[] strArr = str.split(",", -1);
        for (String elStr : strArr) {
            try {
                Element element = stringToElement(elStr);
                if (element != null) {
                    if (!element.equals(Element.EMPTY)) {
                        list.add(element);
                    }
                } else {
                    throw new AddressParseException("Element切分字段数目不正确，反序列化失败");
                }
            } catch (IllegalArgumentException e) {
                throw new AddressParseException("Level参数不正确，Element反序列化失败");

            }

        }
        return list;
    }

    /**
     * 移除列表中的重复元素片段
     *
     * @param segElement
     * @return
     */
    public static List<Element> removeDuplicateElement(List<Element> segElement) {
        if (segElement.size() <= 1) {
            return segElement;
        }
        int preIndex = 0;
        // 前一个变量
        Element preElement;
        // 后一个变量
        Element curElement;
        for (int i = 1, length = segElement.size(); i < length; i++) {
            preElement = segElement.get(preIndex);
            curElement = segElement.get(i);
            // 只要是位置、等级、后缀不相同，就表示不同元素
            if (curElement.getStart() != preElement.getStart()
                    || curElement.getEnd() != preElement.getEnd()
                    || !curElement.getLevel().equals(preElement.getLevel())
//                    || !curElement.getSuffix().equals(preElement.getSuffix())
                    ) {
                segElement.set(++preIndex, curElement);
            }
        }
        return segElement.subList(0, ++preIndex);
    }

    /**
     * 将字符串反序列化为地址向量.
     *
     * @param line 需要反序列化的字符串
     * @return 反序列化结果
     * @throws AddressParseException 抛出反序列化失败异常
     */
    public static Vector parse(String line) throws AddressParseException {
        Vector vector = new Vector();
        String[] strArr = line.split("\\t", -1);
        if (strArr.length != vector.size()) {
            throw new AddressParseException("Vector切分字段数目不正确，反序列化失败");
        }
        for (int i = 0; i < strArr.length; i++) {
            List<Element> list = stringToList(strArr[i]);
            vector.setListOnIndex(list, i);
        }
        return vector;
    }

    /**
     * 将一个字符串归一化为地址向量的整个过程.
     *
     * @param address 需要归一化的地址字符串
     * @return 归一化结果
     * @throws InvalidAddressException 抛出无效地址异常
     */
    public static Vector addressNormalizer(String address) throws InvalidAddressException {

        Vector vector = Vector.valueOf(address);

        new FiveLevelAheadCheck(AddressQuery.getInstance()).process(vector);
        new LevelEProcessor().process(vector);
        new LevelFProcessor().process(vector);
        new LevelGProcessor().process(vector);
        new LevelHProcessor().process(vector);
        // new LevelIProcessor().process(vector);
        new LevelJProcessor().process(vector);
        new LevelKProcessor().process(vector);
        new LevelLProcessor().process(vector);
        new LevelRubbishProcessor().process(vector);
        new LevelMProcessor().process(vector);
        new LastProcessor().process(vector);

        return vector;
    }

}
