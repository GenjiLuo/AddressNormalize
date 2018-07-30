package com.jhzhang.address.normalizer.core.process;

import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;


/**
 * 等级H的处理过程.
 * Created by Administrator on 2016/9/13.
 */
public class LastProcessor implements Processor {
    private static Level H = Level.H;
    private static Level K = Level.K;

    public Vector process(Vector vector) {
        checkLevelH(vector);
        checkLevelK(vector);
        return vector;
    }

    /**
     * 校验H等级元素
     *
     * @param vector
     */
    private void checkLevelH(Vector vector) {
        String regex = "^[上下左右前后东西南北]+(面|边|方|门)?$";
        //此阶段只会有元素列表里只会存在一个元素
        if (!vector.indexOf(H.ordinal()).isEmpty()) {
            Element element = vector.indexOf(H.ordinal()).get(0);
            element.setName(element.getName().replaceAll(regex, ""));
        }


    }

    /**
     * 校验K等级元素
     *
     * @param vector
     */
    private void checkLevelK(Vector vector) {
        if (!vector.indexOf(K.ordinal()).isEmpty()) {
            Element element = vector.indexOf(K.ordinal()).get(0);
            if (element.getName().startsWith("-") || element.getName().endsWith("-")) {
                element.setName(element.getName().replaceAll("-", ""));
            }
        }

    }

}
