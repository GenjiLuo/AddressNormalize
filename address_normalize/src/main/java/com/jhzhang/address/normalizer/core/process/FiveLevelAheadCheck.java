package com.jhzhang.address.normalizer.core.process;

import com.jhzhang.address.normalizer.cache.IQuery;
import com.jhzhang.address.normalizer.cache.exception.InvalidAddressException;
import com.jhzhang.address.normalizer.cache.exception.NotFindAddressException;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.core.support.Position;
import com.jhzhang.address.normalizer.cache.IQuery;
import com.jhzhang.address.normalizer.cache.exception.InvalidAddressException;
import com.jhzhang.address.normalizer.cache.exception.NotFindAddressException;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.core.support.Position;

import java.util.HashSet;
import java.util.List;

/**
 * 将地址转换为地址向量.
 */
public class FiveLevelAheadCheck implements Processor {

    /**
     * 第五个等级在枚举类中对应的序号.
     */
    private static final int MAX_NORMALIZED_LEVEL = Level.E.ordinal();
    private final IQuery query;

    public FiveLevelAheadCheck(IQuery query) {
        this.query = query;
    }

    /**
     * 前四级地址元素处理过程.
     *
     * @param vector 经过简单合并后的地址向量
     */
    @Override
    public Vector process(Vector vector) throws InvalidAddressException {
        HashSet<AddressNode> candidateSet = vector.findAdministrationCondidate(vector);

        //如果生成的前五级候选地址为空
        if (candidateSet == null || candidateSet.size() <= 0) {
            // 缺少行政单元
            throw new InvalidAddressException("01 don't contain administration");
        }
        List<Element> lists = vector.getMaxScoreList(candidateSet);

        fixPreviousReferenceElements(lists, vector);
//        fixAllElementOverMaxNormalizeLevel(vector);
        return vector;
    }

    /**
     * 以接口的形式修复参考元素之前的所有元素.
     *
     * @param reliableLists 接口返回的结果
     * @param vector        要修正的地址向量
     */
    private boolean fixPreviousReferenceElements(List<Element> reliableLists, Vector vector) {
        for (int i = 0; i < reliableLists.size(); i++) {
            for (int j = vector.indexOf(i).size() - 1; j >= 0; j--) {
                Element ele = vector.indexOf(i).get(j);
                if (ele.equals(reliableLists.get(i))) {
                    continue;
                }
                if (vector.isMountedAfter(ele, new Position(i + 1, -1))) {
                    vector.removeElement(ele);
                } else {
                    vector.rollBack(ele, new Position(i, j));
                }
            }
        }
        for (Element element : reliableLists) {
            vector.mount(element.getLevel().ordinal(), element);
        }

        rollBackAllReferenceAfterElements(vector, reliableLists);
        return false;
    }

    /**
     * 回退所有参考元素后面，误匹配的元素
     *
     * @param vector
     * @param reliableLists 作为参照的地址元素
     */
    private void rollBackAllReferenceAfterElements(Vector vector, List<Element> reliableLists) {
        for (int i = reliableLists.size(); i <= MAX_NORMALIZED_LEVEL; i++) {
            List<Element> list = vector.indexOf(i);
            for (int j = list.size() - 1; j >= 0; j--) {
                Element ele = list.get(j);
                // 如果元素后缀不为空，且是自带的后缀，判断是否是因为行政区变所导致
                if (!ele.getName().isEmpty() && !ele.getSuffix().isEmpty()
                        && Vector.notAddSuffix(ele)) {
                    // 去除元素的后缀
                    Element tmp = new Element(ele.getName(), "", Level.values()[i], ele.getStart(), ele.getEnd());
                    Element query = getMatchedReliableElement(reliableLists, tmp);
                    if (query != null) {
                        reliableLists.add(query);
                        vector.removeElement(ele);
                        vector.mount(i, query);
                        continue;
                    }
                }
                if (vector.isMountedAfter(ele, new Position(i, j)) || vector.isMountedBefore(ele, new Position(i, j))) {
                    vector.removeElement(ele);
                } else {
                    // 回滚其中元素
                    Position rollPosition = vector.getRollBackPosition(ele);
                    if (rollPosition.getHorizon() <= i) {
                        vector.removeElement(ele);
                    } else {
                        vector.rollBack(ele, new Position(i, j));
                    }
                }
            }
        }
    }

    public Element getMatchedReliableElement(List<Element> reliableList, Element element) {
        if (reliableList.size() <= 0) {
            return null;
        }
        Element reference = reliableList.get(reliableList.size() - 1);
        if (element.getLevel().ordinal() <= reference.getLevel().ordinal()) {
            return null;
        }
        List<AddressNode> queryResult;
        try {
            queryResult = query.getSpecificAddressNodeList(reference, element);
        } catch (NotFindAddressException e) {
            return null;
        }
        for (AddressNode node : queryResult) {
            AddressNode pNode = node.getParentAddressNode();
            if (isAddressNodeEquals(pNode, reliableList)) {
                return new Element(node.getKeyString(), node.getSuffix(), node.getLevel(), element.getStart(), element.getEnd());
            }
        }
        return null;
    }

    public boolean isAddressNodeEquals(AddressNode node, List<Element> reliableList) {
        for (int i = reliableList.size() - 1; i >= 0; --i) {
            Element element = reliableList.get(i);
            if (!element.getName().contains(node.getKeyString()) ||
                    !element.getLevel().equals(node.getLevel())) {
                return false;
            }
            node = node.getParentAddressNode();
        }
        return true;
    }


    /**
     * 修复最大归一化元素后，所有小于最大归一化的地址元素等级。
     *
     * @param vector
     */
    public void fixAllElementOverMaxNormalizeLevel(Vector vector) {
        Position start = new Position(MAX_NORMALIZED_LEVEL + 1, -1);
        Position next1 = vector.getNextPosition(start);
        Element ele1 = vector.indexOf(next1);
        if (next1 == null || ele1 == null) {
            return;
        }
        Level level = ele1.getLevel();
        while (ele1 != null && level.ordinal() <= MAX_NORMALIZED_LEVEL) {
            vector.removeElement(ele1);
            // 如果挂载的位置没有挂载其它元素
            if (vector.indexOf(ele1.getLevel().ordinal()).size() == 0) {
                // D,E 级别的地址元素，可能会因为地址库中元素不存在而出现
                if (level.equals(Level.E)) {
//                        || level.equals(Level.D)) {
                    vector.mount(level.ordinal(), ele1);
                }
            }
            next1 = vector.getNextPosition(next1);
            if (next1 == null) {
                return;
            }
            ele1 = vector.indexOf(next1);
            level = ele1.getLevel();
        }
    }
}

