package com.jhzhang.address.normalizer.core.process;

import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.core.CommonOperation;
import com.jhzhang.address.normalizer.core.NumberConverter;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 等级I的处理过程.
 * Created by Administrator on 2016/9/13.
 */
public class LevelIProcessor implements Processor {

    //添加：零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十甲乙丙丁戊己庚辛壬癸
    private static final Pattern PATTERN_FOR_FIX = Pattern.compile("[a-zA-Z]+[0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十甲乙丙丁戊己庚辛壬癸]*");
    private static final Pattern END_BY_PATTERN = Pattern.compile(".*([a-zA-Z]+[0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十甲乙丙丁戊己庚辛壬癸]*)$");
    //修改掉I级别
    //private static Level LEVEL = Level.I;
    private static Level LEVEL = Level.RUBBISH;

    @Override
    public Vector process(Vector vector) {
        //获取简单合并后的地址元素列表
        CommonOperation.mergeByKeywords(vector, LEVEL);
        List<Element> list = vector.indexOf(LEVEL.ordinal());
        //修正对应等级上的地址元素
        List<Element> normalizeList = normalize(list);
        vector.setListOnIndex(normalizeList, LEVEL.ordinal());

        fix(vector);

        return vector;
    }

    /**
     * 做修正 如果“座”关键字前面为字母，则将字母分离出来，前面的部分移动到等级H中.
     *
     * @param vector 地址向量
     */
    private void fix(Vector vector) {
        List<Element> list = vector.indexOf(LEVEL.ordinal());
        List<Element> fixList = new LinkedList<>();
        for (Element element : list) {
            if (element.getSuffix().equals("座")
                    && CommonOperation.isMatchPattern(element, END_BY_PATTERN)) {
                List<Element> reSplit = reSplit(element, PATTERN_FOR_FIX);
                fixList.addAll(reSplit);
            } else {
                fixList.add(element);
            }
        }
        vector.setListOnIndex(fixList, LEVEL.ordinal());


    }

    /**
     * 拆分后再重新组合.
     *
     * @param element 要拆分的地址元素
     * @param pattern 拆分的模式
     * @return 重新组合的列表
     */
    private List<Element> reSplit(Element element, Pattern pattern) {
        List<Element> list = new LinkedList<>();
        String name = element.getName();

        Matcher matcher = pattern.matcher(name);
        String str = "";
        while (matcher.find()) {
            str = matcher.group();
        }
        String preName = name.replaceFirst(str, "");
        if (preName.length() > 0) {
            Element newPreElement = new Element(preName, "", Level.H, element.getStart(), element.getEnd());
            list.add(newPreElement);
        }
        Element newSufElement = new Element(str,
                element.getSuffix(),
                element.getLevel(),
                element.getStart() + preName.length(), element.getEnd());
        list.add(newSufElement);
        return list;
    }

    /**
     * 归一化.
     *
     * @param list 要归一化的列表
     * @return 归一化后的列表
     */
    public List<Element> normalize(List<Element> list) {
        List<Element> normalizeList = new LinkedList<>();
        for (Element element : list) {
            String name = element.getName();
            //数字转中文
            name = NumberConverter.digitToCnNumber(name);
            //小写转大写
            name = name.toUpperCase();
            Element fixElement = new Element(name,
                    element.getSuffix(),
                    element.getLevel(),
                    element.getStart(), element.getEnd());
            normalizeList.add(fixElement);
        }
        return normalizeList;
    }
}
