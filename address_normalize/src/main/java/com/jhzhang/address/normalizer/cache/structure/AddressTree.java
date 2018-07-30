package com.jhzhang.address.normalizer.cache.structure;

import com.jhzhang.address.normalizer.cache.bean.AddressBean;
import com.jhzhang.address.normalizer.common.Level;

import java.util.*;


/**
 * 1.添加节点到树中
 * 2.创建一个新的地址节点
 * 3.获取树的根节点
 * 4.获取存放所有简称和全称及其所对应的地址节点
 * 5.遍历树,获取所有地名的简称和全称及其所对应的地址节点
 * 6.遍历地址和后缀库的所有地名，获得所有地址后缀构建其所对应的地址节点
 */
public class AddressTree extends TreeMap {
    /** 定义一个树的根节点 */
    private AddressNode rootAddressNode = new AddressNode(AddressBean.EMPTY);

    /**
     * 动态的将一个节点插入到树中.
     *
     * @param addressNode       准备插入的节点
     * @param parentAddressNode 要插入的节点的父节点
     * @return 如果添加成功返回true，失败，返回false
     */
    public boolean addAddressNodeToTree(AddressNode addressNode, AddressNode parentAddressNode) {
        if (addressNode == null || parentAddressNode == null) {
            return false;
        }
        //设置节点，为子节点设置父节点，为父节点添加子节点
        addressNode.setParentAddressNode(parentAddressNode);
        parentAddressNode.addChildNode(addressNode);

        return true;
    }

    /**
     * 创建一个地址节点.
     *
     * @param addressBean 包含地址信息的addressBean
     * @return 地址节点
     */
    public AddressNode createAddressNode(AddressBean addressBean) {
        return ((addressBean == null) || (addressBean == AddressBean.EMPTY)) ? null
                : new AddressNode(addressBean);
    }

    /**
     * 获取此树的根节点.
     *
     * @return 树的根节点
     */
    public AddressNode getRootAddressNode() {
        return rootAddressNode;
    }

    /**
     * 遍历树，生成用于初始化的TreeMap.
     *
     * @return 存有关键字和对应节点的TreeMap
     */
    public TreeMap<String, List<AddressNode>> getAllNodes() {
        TreeMap<String, List<AddressNode>> map = new TreeMap<>();

        //遍历树，得到地名的地址节点
        travelTreeGetAllNodes(rootAddressNode, map);

        //得到等级和后缀的地址节点
        getAllLevelWithSuffixNodes(map);

        //将地址节点库设为不可更改的
        Collections.unmodifiableMap(map);
        return map;
    }

    /**
     * 遍历树.
     *
     * @param rootAddressNode 树的根节点
     * @param map             存有关键字和对应节点的TreeMap
     */
    private void travelTreeGetAllNodes(AddressNode rootAddressNode,
                                       TreeMap<String, List<AddressNode>> map) {

        if (rootAddressNode == null) {
            return;
        }

        Collection<AddressNode> childNodeList = rootAddressNode.getChildNodeList();
        //出口，如果子节点列表为空时，列表对象为null，此时跳出本次循环
        if (childNodeList == null) {
            return;
        }

        for (AddressNode addressNode : childNodeList) {
            String[] names = addressNode.getAllNames();
            for (String name : names) {
                //如果map中已经包含此name，那么将这个节点存到该name对应的list中；否则新建一个list，存入节点，再放到map中
                if (map.containsKey(name)) {
                    map.get(name).add(addressNode);
                } else {
                    List<AddressNode> nodeList = new ArrayList<>();
                    nodeList.add(addressNode);
                    map.put(name, nodeList);
                }
            }
            travelTreeGetAllNodes(addressNode, map);
        }
    }

    private void getAllLevelWithSuffixNodes(TreeMap<String, List<AddressNode>> map) {
        for (Level level : Level.values()) {
            for (String suffix : level.getKeywords()) {
                if (!suffix.isEmpty()) {
                    List<AddressNode> nodeList = map.get(suffix);
                    if (nodeList == null) {
                        //存放节点
                        nodeList = new ArrayList<>();
                        map.put(suffix, nodeList);
                    }
                    AddressNode levelAddressNode = new AddressNode(
                            //新建一个节点
                            new AddressBean("", "", suffix, level));
                    nodeList.add(levelAddressNode);
                }
            }
        }
    }
}
