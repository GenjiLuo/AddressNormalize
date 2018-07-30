package com.jhzhang.address.normalizer.cache;

import com.jhzhang.address.normalizer.cache.exception.NotFindAddressException;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.cache.structure.AddressTree;
import com.jhzhang.address.normalizer.cache.tool.ParseAddressXml;
import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.collection.AhoCorasickDoubleArrayTrie;
import com.jhzhang.address.normalizer.common.collection.CommonAhoCorasickSegmentUtil;
import com.jhzhang.address.normalizer.common.collection.ResultTerm;
import com.jhzhang.address.normalizer.cache.exception.NotFindAddressException;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.cache.structure.AddressTree;
import com.jhzhang.address.normalizer.cache.tool.ParseAddressXml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;


/**
 * 封装实现地址查询的方法，为实现地址查询服务.
 */
class AddressMatcher {
    private AddressTree addressTree;
    private AhoCorasickDoubleArrayTrie<List<AddressNode>> tokenTrie;

    public AddressMatcher() {
        addressTree = new AddressTree();
        tokenTrie = new AhoCorasickDoubleArrayTrie<>();
    }

    public void init(String addressDataPath, String addressLevelPath) {
        try {
            init(new FileInputStream(new File(addressDataPath)), new FileInputStream(new File(addressLevelPath)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void init(InputStream addressDataStream, InputStream addressLevelStream) {
        //读取地名库
        addressTree = ParseAddressXml.createAddressTree(addressDataStream);

        //初始化等级后缀库
        ParseAddressXml.initLevelWithSuffix(addressLevelStream);

        //初始化自动机，将包含地名关键字和地名对应地址节点的map当做参数传给自动机
        TreeMap<String, List<AddressNode>> keyWithNode = addressTree.getAllNodes();
        tokenTrie.build(keyWithNode);
    }

    /**
     * 根据输入的确定的本级节点和上级节点，输出包含本级节点的所有上级节点.
     *
     * @param parentAddressNode 确定的上级节点
     * @param childAddressNode  确定的本级节点
     * @return 包含本级节点的所有上级节点的列表
     */
    public List<AddressNode> getSpecificAddressNodeList(AddressNode parentAddressNode,
                                                        AddressNode childAddressNode) throws NotFindAddressException {
        //对节点判空，如果节点为null，则抛出异常
        if (parentAddressNode == null || childAddressNode == null) {
            throw new NotFindAddressException("AddressNode can not be null.");
        }

        //新建一个list存放本级和上级还有中间的地址节点
        List<AddressNode> nodesList;

        //获取元素节点的等级差
        int levelDifference = getLevelDifference(parentAddressNode.getLevel(),
                childAddressNode.getLevel());
        if (levelDifference < 0 || levelDifference > 13) {
            throw new NotFindAddressException(parentAddressNode.getKeyString() + " or "
                    + childAddressNode.getKeyString() + " has wrong level.");
        }

        //创建节点，暂存childAddressNode的上级
        AddressNode ancestorAddressNode = childAddressNode.getParentAddressNode();
        for (int i = 1; i < levelDifference; i++) {
            ancestorAddressNode = ancestorAddressNode.getParentAddressNode();
        }

        //如果childAddressNode的相应等级的父节点和parentAddressNode相同，则输出此地址，否则返回空列表
        if (parentAddressNode.equals(ancestorAddressNode)) {
            nodesList = getAllParentAddressNode(childAddressNode);
        } else {
            nodesList = Collections.emptyList();
        }

        Collections.reverse(nodesList);
        return nodesList;
    }

    /**
     * 获得两个地址节点的等级差.
     *
     * @param parentLevel 给定的上级地址节点的等级
     * @param childLevel  给定的本级地址节点的等级
     * @return 等级差
     */
    private int getLevelDifference(Level parentLevel, Level childLevel) {
        //得到枚举量的序数
        int parentLevelOrdinal = parentLevel.ordinal();
        int childLevelOrdinal = childLevel.ordinal();

        return childLevelOrdinal - parentLevelOrdinal;
    }

    /**
     * 根据输入的待确定的字符串，返回切词后的结果.
     *
     * @param text 待确定的字符串
     * @return 每一个切词对应的地址节点
     */
    public List<Element> keywordsMatch(String text) {
        List<Element> nodesList = new ArrayList<>();
        LinkedList<ResultTerm<List<AddressNode>>> segResultList = CommonAhoCorasickSegmentUtil
                .segment(text, tokenTrie);
        for (ResultTerm<List<AddressNode>> result : segResultList) {
            if (result.label != null) {
                for (int i = 0; i < result.label.size(); i++) {
                    Element element = nodeTransToElement(result.label.get(i), result.offset, result.end);
                    nodesList.add(element);
                }
            } else {
                nodesList.add(new Element(result.word, "", Level.UNKNOWN, result.offset, result.end));
            }
        }
        return nodesList;
    }

    /**
     * 精确匹配，找到地名对应的Element.
     *
     * @param text 带确定的地名
     * @return 此地名的确定的Element
     */
    public List<AddressNode> keywordMatch(String text) {
        List<AddressNode> results = tokenTrie.get(text);
        //tokenTrie.get()重复返回,去重
        if (results.size() > 1) {
            Set<AddressNode> rsSet = new HashSet<>();
            rsSet.addAll(results);
            results.clear();
            results.addAll(rsSet);
        }

        return results == null ? Collections.<AddressNode>emptyList() : results;
    }

    /**
     * 根据节点将此节点的所有上级和本节点输出.
     *
     * @param addressNode 明确的地址节点
     * @return 存放地址元素的列表
     */
    public List<AddressNode> getAllParentAddressNode(AddressNode addressNode) {
        List<AddressNode> nodeList = new ArrayList<>();
        nodeList.add(addressNode);

        int levelDifference = addressNode.getLevel().ordinal();
        for (int i = 0; i < levelDifference; i++) {
            addressNode = addressNode.getParentAddressNode();

            // 当查询父节点遇到直辖市时，不添加，继续向上查询
            if (addressNode.getKeyString().equals("直辖市")) {
                continue;
            }
            nodeList.add(addressNode);
        }
        return nodeList;
    }

    /**
     * 将node转化为Element.
     *
     * @param addressNode 待转化的地址节点
     * @param offset      偏移量
     * @return 地址元素Element
     */
    public Element nodeTransToElement(AddressNode addressNode, int offset, int end) {
        if (addressNode.getFullName() != null) {
            return new Element(addressNode.getFullName(), addressNode.getSuffix(),
                    addressNode.getLevel(), offset, end);
        } else {
            return new Element(addressNode.getKeyString(), addressNode.getSuffix(),
                    addressNode.getLevel(), offset, end);
        }
    }
}
