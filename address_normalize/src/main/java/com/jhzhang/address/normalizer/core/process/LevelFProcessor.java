package com.jhzhang.address.normalizer.core.process;

import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.core.CommonOperation;
import com.jhzhang.address.normalizer.core.NumberConverter;
import com.jhzhang.address.normalizer.core.support.Position;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.core.CommonOperation;
import com.jhzhang.address.normalizer.core.support.Position;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 等级F的处理流程
 * Created by Administrator on 2016/9/9.
 */
public class LevelFProcessor implements Processor {
    private static Level LEVEL = Level.F;

    /**
     * 对该等级的整体处理 .
     *
     * @param vector 经简单合并后的地址向量
     * @return 处理后的结果
     */
    @Override
    public Vector process(Vector vector) {
        CommonOperation.fixBreakPoint(vector, LEVEL);
        //获取简单合并后的地址元素列表
        CommonOperation.mergeByKeywords(vector, LEVEL);
        fixFistElement(vector);
        //修正对应等级上的地址元素
        LevelFProcessor levelF = new LevelFProcessor();
        levelF.fix(vector, LEVEL);
        return vector;
    }

    private void fix(Vector vector, Level level) {
        List<Element> list = vector.indexOf(level.ordinal());
        if (list.size() < 0) {
            return;
        }
        List<Element> fixList = new LinkedList<>();
        int num = 0;
        for (int i = list.size() - 1; i >= 0; i--) {
            Element element = list.get(i);
            if (element.getName().length() <= 0) {
                Element preElement;
                if (i > 0 && (preElement = list.get(i - 1)).getName().length() <= 1) {
                    fixList.add(0, merge(preElement, element));
                    i--;
                } else {
                    vector.rollBack(element, new Position(level.ordinal(), i - num));
                    num++;
                }
            } else {
                fixList.add(0, element);
            }
        }
        vector.setListOnIndex(fixList, level.ordinal());
    }

    /**
     * 将名称长度小于等于1的地址元素与其后面的单个地址元素关键字合并.
     *
     * @param preElement 名称长度小于等于1的地址元素
     * @param sufElement 后面的单个地址元素关键
     * @return 合并结果
     */
    public Element merge(Element preElement, Element sufElement) {
        String name = preElement.getName() + preElement.getSuffix() + sufElement.getName();
        int offset = preElement.getStart();
        return new Element(name, sufElement.getSuffix(), sufElement.getLevel(), offset, sufElement.getEnd());
    }

    /**
     * 如果第一个元素的名称长度小于等于1 ，并且名称不为数字 取它前一个地址元素合并.
     *
     * @param vector 地址向量
     */
    private void fixFistElement(Vector vector) {
        Pattern pattern = Pattern.compile("\\d");
        List<Element> list = vector.indexOf(LEVEL.ordinal());
        if (list.size() <= 0) {
            return;
        }
        Element element = list.get(0);
        if (element.getName().length() <= 1
                && !pattern.matcher(element.getName()).matches()) {
            Position prePosition = vector.getPreviousPosition(new Position(LEVEL.ordinal(), 0));
            if (prePosition.getHorizon() <= Level.D.ordinal()) {
                return;
            }
            Element preElement = vector.indexOf(prePosition);
            vector.removeElement(preElement);
            vector.removeElement(element);
            Element combine = merge(preElement, element);
            list.set(0, combine);

        }
        vector.setListOnIndex(list, LEVEL.ordinal());
    }

    /**
     * 修正该等级上的地址元素列表.
     *
     * @param vector 等级上的地址元素列表
     */
    public void normalize(Vector vector) {
        List<Element> list = vector.indexOf(LEVEL.ordinal());
        List<Element> fixList = new LinkedList<>();
        for (Element element : list) {
            String suffix = element.getSuffix();
            if (suffix.equals("巷") || suffix.equals("国道")) {
                element = NumberConverter.cnToDigitInHundred(element);
            } else {
                element = NumberConverter.digitsToCn(element);
            }
            element = NumberConverter.removePunctuation(element);
            fixList.add(element);
        }
        vector.setListOnIndex(fixList, LEVEL.ordinal());
    }
}
