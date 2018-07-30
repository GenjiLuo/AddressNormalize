package com.jhzhang.address.normalizer.test;

import com.jhzhang.address.normalizer.test.Counter.MutableInteger;
import com.jhzhang.address.normalizer.test.bean.AddressTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.NumberFormat;
import java.util.List;

/**
 * @author jhzhang
 */
public class CountUtils {
    static final Logger LOGGER = LogManager.getLogger(CountUtils.class.getSimpleName());
    /**
     * 每个元素数组的固定长度
     */
    private final static int LENGTH = 13;

    /**
     * 精确率
     *
     * @param lists 输入的归一化数据
     * @return 返回统计不同错误类型的数值统计
     */
    public static long[] precisionRate(List<AddressTest> lists) {
        return precisionRate(lists, LENGTH);
    }

    /**
     * 返回某一界别以前的地址
     *
     * @param lists
     * @param preIndex 起始级别
     * @return
     */
    public static long[] precisionRate(List<AddressTest> lists, int preIndex) {
        return precisionRate(lists, 0, preIndex);
    }

    /**
     * 返回某一界别之间的地址精确率
     *
     * @param lists
     * @param start 起始级别
     * @param end   终止级别
     * @return
     */
    public static long[] precisionRate(List<AddressTest> lists, int start, int end) {
        MutableInteger count = new MutableInteger(0);
        MutableInteger sum = new MutableInteger(0);
        // 当前数据类型
        for (AddressTest atb : lists) {
            if (atb.actuals != null) {
                // 判断两个地址是否相等
                int[] res = atb.getPrecisionArray(start, end);
                count.increase(res[0]);
                sum.increase(res[1]);
            }
        }
        return new long[]{count.getVal(), sum.getVal()};
    }

    /**
     * 召回率
     *
     * @param lists 输入的归一化数据
     * @return 返回统计不同错误类型的数值统计
     */
    public static long[] recallRate(List<AddressTest> lists) {
        return recallRate(lists, LENGTH);
    }

    public static long[] recallRate(List<AddressTest> lists, int preIndex) {
        return recallRate(lists, 0, preIndex);
    }

    /**
     * 某一个级别之间的召回率
     *
     * @param lists 输入列表
     * @param start 起始的地址级别
     * @param end   借宿的地址级别
     * @return
     */
    public static long[] recallRate(List<AddressTest> lists, int start, int end) {
        MutableInteger count = new MutableInteger(0);
        MutableInteger sum = new MutableInteger(0);
        // 当前数据类型
        for (AddressTest atb : lists) {
            if (atb.excepts != null) {
                // 判断两个地址是否相等
                int[] res = atb.getRecallArray(start, end);
                count.increase(res[0]);
                sum.increase(res[1]);
            }
        }
        return new long[]{count.getVal(), sum.getVal()};
    }

    public static String[] countPresionAndRecallF1(List<AddressTest> atLists) {
        return countPresionAndRecallF1(atLists, 0, LENGTH);
    }

    public static String[] countPresionAndRecallF1(List<AddressTest> atLists, int start, int end) {
        return countPresionAndRecallF1(atLists, start, end, true);
    }

    /**
     * 返回 [级别等级，精确率，召回率，F值]
     *
     * @param atLists
     * @param start
     * @param end
     * @param isLogPrint
     * @return
     */
    public static String[] countPresionAndRecallF1(List<AddressTest> atLists, int start, int end, boolean isLogPrint) {
        // 级别名称
        String formatStr0 = "";
        if (end - start == 1) {
            formatStr0 = String.format("第%d级", start);
        } else if (start == 0) {
            formatStr0 = String.format("前%d级", end);
        } else {
            formatStr0 = String.format("%d-%d级", start, end);
        }
        long[] preRate = precisionRate(atLists, start, end);
        double pRate = Double.valueOf(preRate[0]) / Double.valueOf(preRate[1]);
        // 设置分数后的两位小数
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMaximumFractionDigits(2);
        String formatStr1 = nf.format(pRate);
        long[] recRate = recallRate(atLists, start, end);
        double rRate = Double.valueOf(recRate[0]) / Double.valueOf(recRate[1]);
        String formatStr2 = nf.format(rRate);
        Double F = (pRate * rRate * 2) / (pRate + rRate);
        String formatStr3 = nf.format(F);
        if (isLogPrint) {
            LOGGER.info("{}-{}级别，精确率：{}/{}={}", start, end, preRate[0], preRate[1], formatStr1);
            LOGGER.info("{}-{}级别，召回率：{}/{}={}", start, end, recRate[0], recRate[1], formatStr2);
            LOGGER.info("{}-{}级别，F值: {}", start, end, formatStr3);
        }
        return new String[]{formatStr0, formatStr1, formatStr2, formatStr3};
    }


    /**
     * 计算每个级别的精确率，召回率，F值
     */
    public static void countEeveryPRF(List<AddressTest> atLists) {
        for (int i = 0; i < LENGTH; i++) {
            String[] rate = countPresionAndRecallF1(atLists, i, i + 1, false);
            LOGGER.info("{}，精确率：{}, 召回率：{}, F值: {}", rate[0], rate[1], rate[2], rate[3]);
        }
    }
}



