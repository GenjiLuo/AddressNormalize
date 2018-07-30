package com.jhzhang.address.normalizer.test.bean;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * @author Administrator
 */
public class AddressTest {
    /**
     * 每个元素数组的固定长度
     */
    private final static int LENGTH = 13;
    private static final Logger LOGGER = LogManager.getLogger(AddressTest.class.getSimpleName());
    /**
     * 错误类型
     */
    public String type;
    /**
     * 原始地址
     */
    public String raw;
    /**
     * 期望切分结果
     */
    public String[] excepts;
    /**
     * 实际切分结果
     */
    public String[] actuals;

    public AddressTest(String addressJson) {
        valueOf(addressJson);
    }

    public void setActuals(List<String> lists) {
        if (actuals == null) {
            actuals = new String[AddressTest.LENGTH];
            for (int i = 0; i < AddressTest.LENGTH; i++) {
                actuals[i] = lists.get(i);
            }
        }
    }

    private void valueOf(String str) {
        String[] lists = str.split("\t", 4);
        if (lists.length == 0x3) {
            type = lists[0];
            raw = lists[1];
            excepts = lists[2].replace("/", "").split(",", -1);
            if (excepts.length != AddressTest.LENGTH) {
                LOGGER.info("{} {} length {} isn't equal 13", type, raw, excepts.length);
                excepts = null;
            }
        }
    }

    /**
     * @return 获得预计结果切分元素的非空长度
     */
    public int getExceptLength() {
        int i = 0;
        if (excepts != null) {
            for (String str : excepts) {
                if (!str.isEmpty()) {
                    i++;
                }
            }
        }
        return i;
    }

    /**
     * @return 获得实际切分元素的非空长度
     */
    public int getActualLength() {
        int i = 0;
        if (actuals != null) {
            for (String str : actuals) {
                if (!str.isEmpty()) {
                    i++;
                }
            }
        }
        return i;
    }


    /**
     * 统计精确率:预期切分出来的数据中被成功切分出来的片段/实际总的切分片段
     *
     * @return
     */
    public int[] getPrecisionArray() {
        return getPrecisionArray(AddressTest.LENGTH);
    }

    /**
     * 统计精确率:预期切分出来的数据中被成功切分出来的片段/实际总的切分片段
     *
     * @param preIndex 统计preIndex前面的等级
     * @return
     */
    public int[] getPrecisionArray(int preIndex) {
        return getPrecisionArray(0, preIndex);
    }

    /**
     * 统计某些地址级别的景区率
     *
     * @param start
     * @param end
     * @return
     */
    public int[] getPrecisionArray(int start, int end) {
        if (start > end || start < 0 || end > LENGTH) {
            throw new RuntimeException(String.format("索引输入有误,start: {}, end: {}", start, end));
        }
        int count = 0;
        int sum = 0;
        if (actuals != null) {
            for (int i = start; i < end; i++) {
                // 正确的切分片段
                try {
                    if (excepts[i] != null && actuals[i] != null
                            && (actuals[i].equals(excepts[i]))) {
                        count++;
                    }
                } catch (Exception e) {
                    System.out.println(raw);
                    e.printStackTrace();
                }
                // 实际总的切分片段
                if (actuals[i] != null) {
                    sum++;
                }
            }
        }
        return new int[]{count, sum};
    }

    /**
     * 召回率(recall)：预期切分出来的数据中被成功切分出来的片段/预期切分出来总的地址片段
     *
     * @return
     */
    public int[] getRecallArray() {
        return getRecallArray(AddressTest.LENGTH);
    }

    /**
     * 召回率(recall)：预期切分出来的数据中被成功切分出来的片段/预期切分出来总的地址片段
     *
     * @param preIndex 统计前面的等级
     * @return
     */
    public int[] getRecallArray(int preIndex) {
        return getRecallArray(0, preIndex);
    }

    public int[] getRecallArray(int start, int end) {
        if (start > end || start < 0 || end > LENGTH) {
            throw new RuntimeException(String.format("索引输入有误,start: {}, end: {}", start, end));
        }
        int count = 0;
        int sum = 0;
        if (actuals != null) {
            for (int i = start; i < end; i++) {
                if (excepts[i] != null) {
                    if (excepts[i].equals(actuals[i])) {
                        count++;
                    }
                    sum++;
                }
            }
        }
        return new int[]{count, sum};
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append("|");
        for (String str : excepts) {
            sb.append(str).append("/");
        }
        if (actuals != null) {
            sb.append("|");
            for (String str : actuals) {
                sb.append(str).append("/");
            }
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return raw.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AddressTest) {
            AddressTest at = (AddressTest) obj;
            if (at.raw.equals(raw)) {
                return true;
            }
        }
        return false;
    }
}