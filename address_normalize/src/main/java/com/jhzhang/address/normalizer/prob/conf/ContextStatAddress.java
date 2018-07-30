package com.jhzhang.address.normalizer.prob.conf;

import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.common.ContextStatTool;
import com.jhzhang.address.normalizer.prob.common.ContextStatTool;

/**
 * Address Context State
 *
 * @author Administrator
 */
public class ContextStatAddress {

    /**
     * 转移概率
     */
    private static int[][] transProbs;

    private static ContextStatAddress cs = new ContextStatAddress();

    private ContextStatAddress() {
        int matrixLength = AddressType.values().length;
        transProbs = new int[matrixLength][];

        for (int i = 0; i < AddressType.values().length; ++i) {
            transProbs[i] = new int[matrixLength];
        }
    }

    public static ContextStatAddress getInstance() {
        if (cs == null) {
            cs = new ContextStatAddress();
            ContextStatTool.init(cs);
        }
        return cs;
    }

    public static int[][] getTransProbs() {
        return transProbs;
    }

    public void addTrob(AddressType prev, AddressType cur, int val) {
        transProbs[prev.ordinal()][cur.ordinal()] = val;
    }

    /**
     * get context possibility
     *
     * @param prev the previous POS
     * @param cur  the current POS
     * @return the context possibility between two POSs
     */
    public int getContextPossibility(AddressType prev, AddressType cur) {
        return transProbs[prev.ordinal()][cur.ordinal()];
    }
}
