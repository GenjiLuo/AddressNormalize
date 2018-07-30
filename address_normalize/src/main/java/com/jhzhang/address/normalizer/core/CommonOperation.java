package com.jhzhang.address.normalizer.core;

import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.core.support.Position;
import com.jhzhang.address.normalizer.core.support.Position;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 后九个等级处理过程中要用到的通用操作方法.
 * Created by Administrator on 2016/9/9.
 */
public class CommonOperation {

    /**
     * 找到列表中大于不连续位置的元素都回退.
     *
     * @param vector           地址向量
     * @param breakOffPosition 间断点
     */
    private static void rollBackFromBreakPoint(Vector vector, Position breakOffPosition) {
        if (breakOffPosition == null) {
            return;
        }
        List<Element> list = vector.indexOf(breakOffPosition.getHorizon());
        if (list.size() <= 0) {
            return;
        }
        //大于不连续位置的元素都回退
        for (int i = list.size() - 1; i >= breakOffPosition.getVertical(); i--) {
            Element rollBackElement = list.get(i);
            vector.rollBack(rollBackElement, new Position(breakOffPosition.getHorizon(), i));
        }
    }

    /**
     * 找到列表中offset 不连续的位置，大于不连续位置的元素都回退.
     *
     * @param vector 地址向量
     * @param level  对应的等级
     */
    public static void rollBackFromBreakPoint(Vector vector, Level level) {
        List<Element> list = vector.indexOf(level.ordinal());
        if (list.size() <= 0) {
            return;
        }
        //列表中不连续的位置
        Position breakOffPosition = vector.getBreakOffPosition(level);
        if (breakOffPosition == null) {
            return;
        }
        //大于不连续位置的元素都回退
        for (int i = list.size() - 1; i >= breakOffPosition.getVertical(); i--) {
            Element rollBackElement = list.get(i);
            vector.rollBack(rollBackElement, new Position(breakOffPosition.getHorizon(), i));
        }
    }

    /**
     * 修正offset不连续的地方.
     *
     * @param vector 地址元素
     * @param level  等级
     */
    public static void fixBreakPoint(Vector vector, Level level) {
        List<Element> list = vector.indexOf(level.ordinal());
        if (list.size() <= 0) {
            return;
        }
        //列表中不连续的位置
        Position breakOffPosition = vector.getBreakOffPosition(level);

        if (breakOffPosition == null) {
            return;
        }
        Element currElement = vector.indexOf(breakOffPosition);
        Element preElement = vector.getPreviousElement(breakOffPosition);

        if (preElement.getStart() > currElement.getStart()) {
            return;
        }
        Position startPosition = vector.getRollBackPosition(preElement);
        if (startPosition.getHorizon() <= Level.E.ordinal()) {
            return;
        }
        //修改：原始地址中的“路”可以在村前，也可以在村后，导致分词结果和原始地址中颠倒，所以可以允许不连续
        if (level.ordinal() == Level.E.ordinal()
                && breakOffPosition.getHorizon() == Level.E.ordinal()
                && startPosition.getHorizon() == Level.F.ordinal()) {
            return;
        }

        Position endPosition = getEndPosition(vector, breakOffPosition);
        //如果要补全的位置跨越多个等级，那么当前位置很有可能是从后面误分过来的，则应从间断点回退
        if (startPosition.getHorizon() != endPosition.getHorizon()) {
            rollBackFromBreakPoint(vector, breakOffPosition);
        } else {
            //如果要补全的位置没有跨越多个等级，则将空余的部分补全
            Element element = competeByOffset(vector, startPosition, endPosition, currElement);
            list.set(breakOffPosition.getVertical(), element);
            vector.setListOnIndex(list, level.ordinal());
        }

    }

    /**
     * 获取不连续点的终点位置.
     *
     * @param vector           地址向量
     * @param breakOffPosition 断点位置
     * @return 终点位置
     */
    private static Position getEndPosition(Vector vector,
                                           Position breakOffPosition
    ) {
        int i = 0;
        int j = 0;
        while (i < vector.size()) {
            List<Element> list = vector.indexOf(i);
            if (list.size() > 0) {
                while (j < list.size()) {
                    Element element = list.get(j);
                    Position rollBackPosition = vector.getRollBackPosition(element);
                    if (rollBackPosition.equals(breakOffPosition)) {
                        return new Position(i, j);
                    }
                    j++;
                }
            }
            j = 0;
            i++;
        }

        return new Position(i - 1, vector.indexOf(i - 1).size());
    }

    /**
     * 按关键字合并.
     *
     * @param vector 地址向量
     * @param level  处理的等级
     * @return 合并后的地址向量
     */
    public static Vector mergeByKeywords(Vector vector, Level level) {
        //不对H等级进行断点修复，因为H等级本身具有无序性
        /*if (!level.equals(Level.H) && !level.equals(Level.I)) {
            fixBreakPoint(vector, level);
        }*/

        if (!level.equals(Level.H)) {
            fixBreakPoint(vector, level);
        }

        List<Element> list = vector.indexOf(level.ordinal());
        List<Element> regList = new LinkedList<>();
        if (list.size() <= 0) {
            return vector;
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            Element suffixElement = list.get(i);
            //如果元素不为关键字 则回退
            if (suffixElement.getLevel().equals(Level.UNKNOWN)) {
                vector.rollBack(suffixElement, new Position(level.ordinal(), i));
                continue;
            }

            Element preElement;
            //将所有的UNKNOWN元素合并
            String name = suffixElement.getName();
            int offset = suffixElement.getStart();
            while (i > 0 && (preElement = list.get(i - 1)).getLevel().equals(Level.UNKNOWN)) {
                name = preElement.getName() + name;
                offset = preElement.getStart();
                i--;
            }

            //修改:如果元素name为空，则改元素无效，置空
            Element combine = null;
            if (name.trim().isEmpty())
                combine = new Element(name, "", level, offset, suffixElement.getEnd());
            else
                combine = new Element(name, suffixElement.getSuffix(), level, offset, suffixElement.getEnd());

            regList.add(0, combine);
        }
        vector.setListOnIndex(regList, level.ordinal());
        return vector;
    }

    /**
     * 补全偏移量缺失的部分.
     *
     * @param vector      地址向量
     * @param currElement 当前元素
     * @return 偏移量缺失的部分补全合并
     */
    private static Element competeByOffset(Vector vector,
                                           Position startPosition,
                                           Position endPosition,
                                           Element currElement) {
        //如果区间落在H等级上，则不合并，因为H等级本身具有无序性
        //如果区间落在I等级上且名称为“区” 则不合并

        /*if (endPosition.getHorizon() == Level.H.ordinal()
                || (endElement.getLevel().equals(Level.I) && endElement.getSuffix().equals("区"))) {
            return currElement;
        }*/
        if (endPosition.getHorizon() == Level.H.ordinal()) {
            return currElement;
        }
        List<Element> list = vector.indexOf(startPosition.getHorizon());
        StringBuilder name = new StringBuilder();
        int offset = list.get(startPosition.getVertical()).getStart();
        int num = 0;
        for (int i = startPosition.getVertical(); i <= endPosition.getVertical(); i++) {
            if (i < list.size() && list.get(i) != null) {
                vector.removeElement(new Position(startPosition.getHorizon(), i - num));
                name.append(list.get(i).getName()).append(list.get(i).getSuffix());
                num++;
            }
        }
        name.append(currElement.getName());
        return new Element(name.toString(),
                currElement.getSuffix(),
                currElement.getLevel(),
                offset, currElement.getEnd());

    }

    /**
     * 按照约束规则对对应等级上的列表进行合并.
     *
     * @param vector       地址向量
     * @param level        要处理的等级
     * @param pattern      约束模式
     * @param splitPattern 分离模式
     * @return 合并后生成的列表
     */
    public static Vector mergeByConstraint(Vector vector,
                                           Level level,
                                           Pattern pattern,
                                           Pattern splitPattern) {

        List<Element> list = vector.indexOf(level.ordinal());
        List<Element> regList = new LinkedList<>();
        if (list.size() <= 0) {
            return vector;
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            Element suffixElement = list.get(i);
            //如果元素不为关键字 则回退
            if (suffixElement.getLevel().equals(Level.UNKNOWN)) {
                if (level.equals(Level.M) && isMatchPattern(suffixElement, pattern)) {
                    suffixElement = new Element("", "", Level.M, -1, -1);
                    i = i + 1;
                } else {
                    vector.rollBack(suffixElement, new Position(level.ordinal(), i));
                    continue;
                }

            }
            if (i == 0) {
                break;
            }
            Element preElement = list.get(i - 1);
            //如果前驱地址元素能够匹配对应的模式，则将前驱地址元素与后继地址元素重新拆分后再组合
            if (isMatchPattern(preElement, pattern)) {
                List<Element> reSplitList =
                        reSplitAndCombine(preElement, suffixElement, splitPattern);
                for (Element element : reSplitList) {
                    regList.add(0, element);
                }
                int j = i - 2;
                while (j >= 0 && (preElement = list.get(j)).getLevel().equals(Level.UNKNOWN)) {
                    regList.add(0, preElement);
                    j--;
                }
                i = j + 1;
            } else {
                //如果前驱地址元素不能匹配对应的模式，则将后继地址元素与其前面所有相连的地址元素回退
                vector.rollBack(suffixElement, new Position(level.ordinal(), i));
                while (i > 0 && (preElement = list.get(--i)).getLevel().equals(Level.UNKNOWN)) {
                    vector.rollBack(preElement, new Position(level.ordinal(), i));
                }
            }
        }
        vector.setListOnIndex(regList, level.ordinal());
        return vector;
    }

    /**
     * 将前驱地址元素和后继地址元素按照指定的模式重新拆分再组合.
     *
     * @param preElement    前驱地址元素
     * @param suffixElement 后继地址元素
     * @param pattern       指定的模式
     * @return 组合结果列表
     */
    private static List<Element> reSplitAndCombine(Element preElement,
                                                   Element suffixElement,
                                                   Pattern pattern) {
        List<Element> list = new LinkedList<>();
        String name = preElement.getName();

        Matcher matcher = pattern.matcher(name);
        String str = "";
        while (matcher.find()) {
            str = matcher.group();
        }

        //将匹配到的模式合并到后继地址元素中去(新的分隔点)
        int offset = suffixElement.getStart() - str.length();
        Element newSuffixElement = new Element(str,
                suffixElement.getSuffix(),
                suffixElement.getLevel(),
                offset, suffixElement.getEnd());
        list.add(newSuffixElement);
        //将原前驱地址元素中对应的模式去掉
        String preName = replaceLastPattern(name, str, "");
        if (preName.length() > 0) {
            Element newPreElement = new Element(preName,
                    preElement.getSuffix(),
                    preElement.getLevel(),
                    preElement.getStart(), offset);
            list.add(newPreElement);
        }
        return list;
    }

    /**
     * 区掉一个字符串的末尾的指定模式.
     *
     * @param name   原字符串
     * @param orgStr 要替换的原模式
     * @param tagStr 要替换的目标模式
     * @return 替换后的字符串
     */
    private static String replaceLastPattern(String name, String orgStr, String tagStr) {
        int index = name.lastIndexOf(orgStr);
        name = name.substring(0, index);
        name = name + tagStr;
        return name;
    }

    /**
     * 判断一个地址元素的name是否符合指定的模式.
     *
     * @param element 要判断的地址元素
     * @param pattern 指定的模式
     * @return 是否符合指定的模式
     */
    public static boolean isMatchPattern(Element element, Pattern pattern) {
        String name = element.getName();
        return pattern.matcher(name).matches();
    }

    /**
     * 相同等级上按元素名合并
     *
     * @param list
     */
    public static void uniqElementByName(Vector vector, List<Element> list) {
        Set<String> set = new HashSet<>();
        if (list.size() > 1) {
            for (Element ele : list) {
                if (set.contains(ele.toString()))
                    vector.removeElement(ele);
                else
                    set.add(ele.toString());
            }
        }
    }


    /**
     * 将对应等级列表中多余的量移到指定位置.
     *
     * @param vector    要处理的地址向量
     * @param currLevel 当前等级
     * @return 地址向量
     */
    public static Vector moveToTagLevel(Vector vector, Level currLevel) {
        List<Element> list = vector.indexOf(currLevel.ordinal());
        if (list.size() <= 0) {
            return vector;
        }
        //lcren add
        uniqElementByName(vector, list);

        int index = getKeywordVertical(list);
        rollbackFromIndex(vector, currLevel, index);
        moveFromIndex(vector, currLevel, index);
        return vector;
    }

    /**
     * 从关键字点回退 主要是针对GJKLM中有多个时，只保留一个.
     *
     * @param vector 地址向量
     * @param level  当前等级
     * @param index  关键字的索引位置
     * @return 回退后的地址向量
     */
    private static Vector rollbackFromIndex(Vector vector, Level level, int index) {
        List<Element> list = vector.indexOf(level.ordinal());
        if (list.size() <= 0 || index >= list.size() - 1) {
            return vector;
        }
        for (int i = list.size() - 1; i > index; i--) {
            Element element = list.get(i);
            vector.rollBack(element, new Position(level.ordinal(), i));
        }
        return vector;
    }

    /**
     * 关键字点前面的部分挪到对应等级中去.
     *
     * @param vector    地址向量
     * @param currLevel 当前等级
     * @param index     关键字的索引位置
     * @return 地址向量
     */
    private static Vector moveFromIndex(Vector vector, Level currLevel, int index) {
        List<Element> list = vector.indexOf(currLevel.ordinal());
        if (list.size() <= 0 || index <= 0) {
            return vector;
        }
        String name = "";
        int offset = -1;
        int end = -1;
        for (int i = index - 1; i >= 0; i--) {
            Element element = list.get(i);
            vector.removeElement(new Position(currLevel.ordinal(), i));
            offset = element.getStart();
            name = element.getName() + element.getSuffix() + name;
            end = element.getEnd();
        }
        Level tagLevel = getTagLevel(vector, currLevel);
        Element combine = new Element(name, "", tagLevel, offset, end);
        combine = NumberConverter.removePunctuation(combine);
        if (combine.getName().length() > 0) {

            vector.mount(tagLevel.ordinal(), combine);
        }

        return vector;
    }

    /**
     * 根据当前等级 获取将多余信息移动到的目标等级
     *
     * @param vector    地址向量
     * @param currLevel 当前等级
     * @return 目标等级
     */
    private static Level getTagLevel(Vector vector, Level currLevel) {
        if (currLevel.equals(Level.G)) {
            if (vector.indexOf(Level.F.ordinal()).size() <= 0) {
                return Level.F;
            } else {
                return Level.E;
            }
        }
        /*if (currLevel.equals(Level.I)) {
            return Level.H;
        }*/
        List<Element> hList = vector.indexOf(Level.H.ordinal());
        if (hList.size() <= 0) {
            return Level.H;
        }
        
        /*List<Element> iList = vector.indexOf(Level.I.ordinal());
        if (iList.size() <= 0) {
            return Level.I;
        }*/
        //return Level.I;
        return Level.RUBBISH;
    }

    /**
     * 获取列表中第一个关键字地址元素的位置.
     *
     * @param list 要搜索的列表
     * @return 第一个关键字地址元素的位置
     */
    private static int getKeywordVertical(List<Element> list) {
        int i = 0;
        while (i < list.size()) {
            Element element = list.get(i);
            if (!element.getLevel().equals(Level.UNKNOWN)) {
                return i;
            }
            i++;
        }
        return i - 1;
    }
}
