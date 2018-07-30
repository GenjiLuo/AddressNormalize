/*
 * Created on 2004-8-26
 *
 */
package com.jhzhang.address.normalizer;


import com.jhzhang.address.normalizer.prob.AdjList;
import com.jhzhang.address.normalizer.prob.bean.AddTypes;
import com.jhzhang.address.normalizer.prob.bean.AddressSpan;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.conf.ContextStatAddress;
import com.jhzhang.address.normalizer.prob.conf.UnknowGrammar;
import com.jhzhang.address.normalizer.prob.dictree.DicAddress;
import com.jhzhang.address.normalizer.prob.dictree.MatchRet;
import com.jhzhang.address.normalizer.prob.structure.AddTokenInf;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;
import com.jhzhang.address.normalizer.prob.AdjList;
import com.jhzhang.address.normalizer.prob.bean.AddTypes;
import com.jhzhang.address.normalizer.prob.bean.AddressSpan;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.conf.ContextStatAddress;
import com.jhzhang.address.normalizer.prob.conf.UnknowGrammar;
import com.jhzhang.address.normalizer.prob.dictree.DicAddress;
import com.jhzhang.address.normalizer.prob.dictree.MatchRet;
import com.jhzhang.address.normalizer.prob.structure.AddTokenInf;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 分词和标注
 *
 * @author jhzhang
 */
@SuppressWarnings("ALL")
public class AddressTagger {
    /**
     * 转移状态
     */
    public static ContextStatAddress contextStatAddress = ContextStatAddress.getInstance();
    /**
     * 初始化词典
     */
    public static DicAddress dictAddress = DicAddress.getInstance();
    /**
     * 未知词合并规则
     */
    public static UnknowGrammar grammar = UnknowGrammar.getInstance();
    static double minValue = Double.NEGATIVE_INFINITY / 2;
    private static volatile AddressTagger segment = null;

    public static AddressTagger getInstance() {
        if (segment == null) {
            synchronized (AddressTagger.class) {
                if (segment == null) {
                    segment = new AddressTagger();
                }
                return segment;
            }
        } else {
            return segment;
        }
    }

    /**
     * 计算节点i的最佳前驱节点
     *
     * @param g 切分词图
     * @param i 节点编号
     */
    public static void getPrev(AdjList g, int i, AddTokenInf[] prevNode, double[] prob) {
        Iterator<AddTokenInf> it = g.getPrev(i);
        double maxProb = minValue;
        AddTokenInf maxID = null;

        // 向左查找所有候选词，得到前驱词集合，从中挑选最佳前趋词
        while (it.hasNext()) {
            AddTokenInf itr = it.next();
            double currentProb = prob[itr.start] + itr.logProb;
            if (currentProb > maxProb) {
                maxID = itr;
                maxProb = currentProb;
            }
        }
        prob[i] = maxProb;
        prevNode[i] = maxID;
    }

    /**
     * 获得最有可能匹配的DocToken链表
     *
     * @param g
     * @return maxProb maxProb中的所有元素都是最大概率匹配上的序列
     */
    public static ArrayList<AddressToken> maxProb(AdjList g) {
        AddTokenInf[] prevNodeArray = new AddTokenInf[g.verticesNum];
        double[] probArray = new double[g.verticesNum];
        for (int index = 1; index < g.verticesNum; index++) {
            getPrev(g, index, prevNodeArray, probArray);
        }
        ArrayList<AddTokenInf> ret = new ArrayList<>(g.verticesNum);
        // 从右向左取词候选词
        for (int i = (g.verticesNum - 1); i > 0; i = prevNodeArray[i].start) {
            ret.add(prevNodeArray[i]);
        }

        Collections.reverse(ret);
        mergeUnknow(ret);
        AddressType[] bestTag = hmm(ret);

        ArrayList<AddressToken> list = new ArrayList<AddressToken>();
        for (int i = 0; i < ret.size(); i++) {
            AddTokenInf tokenInf = ret.get(i);
            AddressToken addressToken = AddressToken.valueOf(tokenInf, bestTag[i]);
            list.add(addressToken);
        }
        return list;
    }


    /**
     * @param addressStr
     * @return AdjListDoc 表示一个列表图
     */
    public static AdjList getAdjList(String addressStr) {
        if (addressStr == null || addressStr.length() == 0) {
            return null;
        }

        AtomicInteger atomCount = new AtomicInteger(addressStr.length());

        // 初始化在Dictionary中词组成的图
        AdjList g = new AdjList(atomCount.get() + 1);
        // 在这里开始进行分词
        for (int offset = 0; offset < atomCount.get(); offset++) {
            ArrayList<MatchRet> matchRet = dictAddress.matchAll(
                    addressStr, offset);
            // 匹配上
            if (matchRet.size() > 0) {
                for (MatchRet ret : matchRet) {
                    String termText = addressStr.substring(offset, ret.end);
                    double logProb = Math.log(ret.posInf.totalCost()) - Math.log(dictAddress.n);
                    AddTokenInf tokenInf = new AddTokenInf(offset,
                            ret.end, termText, ret.posInf, logProb);
                    g.addEdge(tokenInf);
                }
            } else {
                // 没匹配上
                double logProb = Math.log(1) - Math.log(dictAddress.n);
                g.addEdge(new AddTokenInf(offset, offset + 1, addressStr
                        .substring(offset, offset + 1), null, logProb));
            }
        }
        return g;
    }

    /**
     * 消除歧义方法
     *
     * @param ret 要消除歧义的集合
     * @return 估计的标注类型序列
     */
    public static AddressType[] hmm(ArrayList<AddTokenInf> ret) {
        //增加开始阶段和结束阶段
        AddTypes startType = new AddTypes();
        startType.put(new AddTypes.AddressTypeInf(AddressType.Start, 1, 0));
        ret.add(0, new AddTokenInf(-1, 0, "Start", startType, 0));

        AddTypes endType = new AddTypes();
        endType.put(new AddTypes.AddressTypeInf(AddressType.End, 100, 100));
        ret.add(new AddTokenInf(-1, 0, "End", endType, 0));

        int stageLength = ret.size();
        //累积概率
        int[][] prob = new int[stageLength][];
        for (int i = 0; i < stageLength; ++i) {
            prob[i] = new int[AddressType.values().length];
            for (int j = 0; j < AddressType.values().length; ++j) {
                prob[i][j] = Integer.MIN_VALUE;
            }
        }

        //最佳前驱，也就是前一个标注是什么
        AddressType[][] bestPre = new AddressType[stageLength][];
        for (int i = 0; i < stageLength; ++i) {
            bestPre[i] = new AddressType[AddressType.values().length];
        }

        prob[0][AddressType.Start.ordinal()] = 1;

        for (int stage = 1; stage < stageLength; stage++) {
            AddTokenInf nexInf = ret.get(stage);
            if (nexInf.dataType == null) {
                continue;
            }
            Iterator<AddTypes.AddressTypeInf> nextIt = nexInf.dataType.iterator();
            while (nextIt.hasNext()) {
                AddTypes.AddressTypeInf nextTypeInf = nextIt.next();

                AddTokenInf preInf = ret.get(stage - 1);
                if (preInf.dataType == null) {
                    continue;
                }

                Iterator<AddTypes.AddressTypeInf> preIt = preInf.dataType.iterator();

                while (preIt.hasNext()) {
                    AddTypes.AddressTypeInf preTypeInf = preIt.next();
                    // 上一个结点到下一个结点的转移概率
                    int trans = contextStatAddress.getContextPossibility(preTypeInf.pos,
                            nextTypeInf.pos);
                    int currentprob = prob[stage - 1][preTypeInf.pos.ordinal()];
                    currentprob = currentprob + trans + nextTypeInf.weight;
                    if (prob[stage][nextTypeInf.pos.ordinal()] <= currentprob) {
                        prob[stage][nextTypeInf.pos.ordinal()] = currentprob;
                        bestPre[stage][nextTypeInf.pos.ordinal()] = preTypeInf.pos;
                    }
                }
            }
        }

        AddressType endTag = AddressType.End;

        AddressType[] bestTag = new AddressType[stageLength];
        for (int i = stageLength - 1; i > 1; i--) {
            bestTag[i - 1] = bestPre[i][endTag.ordinal()];
            endTag = bestTag[i - 1];
        }
        AddressType[] resultTag = new AddressType[stageLength - 2];
        System.arraycopy(bestTag, 1, resultTag, 0, resultTag.length);

        ret.remove(stageLength - 1);
        ret.remove(0);
        return resultTag;
    }

    public static void mergeUnknow(ArrayList<AddTokenInf> tokens) {
        // 合并未知词
        for (int i = 0; i < tokens.size(); ++i) {
            AddTokenInf token = tokens.get(i);
            if (token.dataType != null) {
                continue;
            }
            StringBuilder unknowText = new StringBuilder();
            int start = token.start;
            while (true) {
                unknowText.append(token.termText);
                tokens.remove(i);
                if (i >= tokens.size()) { //已经到结束位置了
                    int end = token.end;

                    AddTypes item = new AddTypes();
                    item.put(new AddTypes.AddressTypeInf(AddressType.Unknow, 10, 0));
                    AddTokenInf unKnowTokenInf = new AddTokenInf(start,
                            end, unknowText.toString(), item, 0);
                    tokens.add(i, unKnowTokenInf);
                    break;
                }
                token = tokens.get(i);
                //已经到了已知词的位置
                if (token.dataType != null) {
                    int end = token.start;
                    AddTypes item = new AddTypes();
                    item.put(new AddTypes.AddressTypeInf(AddressType.Unknow, 10, 0));
                    AddTokenInf unKnowTokenInf = new AddTokenInf(start,
                            end, unknowText.toString(), item, 0);
                    tokens.add(i, unKnowTokenInf);
                    break;
                }
            }
        }
    }

    /**
     * 分词函数，将给定的一个名称分成若干个有意义的部分
     *
     * @param addressStr
     * @return 一个ArrayList，其中每个元素是一个词
     */
    public static ArrayList<AddressToken> basicTag(String addressStr) {
        AdjList g = getAdjList(addressStr);
        ArrayList<AddressToken> tokens = maxProb(g);
        return tokens;
    }

    /**
     * 分词
     *
     * @param addressStr
     * @return
     */
    public static ArrayList<AddressToken> tag(String addressStr) {
        // 词性标注
        AdjList g = getAdjList(addressStr);
        // 查找最大的匹配值
        ArrayList<AddressToken> tokens = maxProb(g);
        // 增加开始和结束节点
        AddressToken startToken = new AddressToken(-1, 0, "Start", AddressType.Start);
        tokens.add(0, startToken);
        AddressToken endToken = new AddressToken(g.verticesNum - 1, g.verticesNum, "End", AddressType.End);
        tokens.add(endToken);

        // 未登录词识别
        int offset = 0;
        while (true) {
            ArrayList<AddressSpan> lhs = grammar.matchLong(tokens, offset);
            if (lhs != null) {
                UnknowGrammar.replace(tokens, offset, lhs);
                offset = 0;
            } else {
                ++offset;
                if (offset >= tokens.size()) {
                    break;
                }
            }
        }
        return tokens;
    }
}