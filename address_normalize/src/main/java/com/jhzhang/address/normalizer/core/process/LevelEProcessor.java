package com.jhzhang.address.normalizer.core.process;

import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.core.CommonOperation;
import com.jhzhang.address.normalizer.core.NumberConverter;
import com.jhzhang.address.normalizer.core.support.Position;
import com.jhzhang.address.normalizer.core.support.Position;

import java.util.LinkedList;
import java.util.List;

/**
 * 等级E的处理流程
 * Created by Administrator on 2016/9/9.
 */
public class LevelEProcessor implements Processor {
    private static Level LEVEL = Level.E;

    @Override
    public Vector process(Vector vector) {
        CommonOperation.fixBreakPoint(vector, LEVEL);
        //按关键字合并
        CommonOperation.mergeByKeywords(vector, LEVEL);
        fix(vector, LEVEL);
        //修正对应等级上的地址元素
        normalize(vector);
        return vector;
    }

    /**
     * 修正 主要是将单个的地址元素关键字向前合并或向后回退.
     *
     * @param vector 地址元素
     */
    public void fix(Vector vector, Level level) {
        List<Element> list = vector.indexOf(level.ordinal());
        if (list.size() > 0) {
            return;
        }
        List<Element> fixList = new LinkedList<>();
        Position start = new Position(level.ordinal() + 1, -1);
        Position next = vector.getNextPosition(start);
        Element current;
        boolean flag = false;
        while (next != null) {
            current = vector.indexOf(next);
            fixList.add(current);
            if (current.getLevel().equals(level)) {
                flag = true;
                break;
            }
            next = vector.getNextPosition(next);
        }
        if (flag) {
            for (Element ele : fixList) {
                vector.removeElement(ele);
            }
            vector.setListOnIndex(fixList, level.ordinal());
        }
    }

    /**
     * 修正该等级上的地址元素列表.
     *
     * @param vector 地址向量
     */
    private void normalize(Vector vector) {
        List<Element> list = vector.indexOf(LEVEL.ordinal());
        List<Element> fixList = new LinkedList<>();
        for (Element element : list) {
            //数字归一化
            element = NumberConverter.digitsToCn(element);
            element = NumberConverter.removePunctuation(element);
            fixList.add(element);
        }
        vector.setListOnIndex(fixList, LEVEL.ordinal());
    }

}
