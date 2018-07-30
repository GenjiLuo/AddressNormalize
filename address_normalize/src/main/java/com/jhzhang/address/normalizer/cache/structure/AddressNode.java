package com.jhzhang.address.normalizer.cache.structure;

import com.jhzhang.address.normalizer.cache.bean.AddressBean;
import com.jhzhang.address.normalizer.common.Level;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 1.添加子节点
 * 2.设置父节点
 * 3.获取父节点
 * 4.获取节点的关键字
 * 5.获取地址别名
 * 6.获取地址后缀
 * 7.获取地址的全称和简称
 * 8.获取地址等级
 * 9.比较节点是否相同
 * 10.获得子节点列表，用于遍历
 * 11.将地址节点转化为地址元素
 */
public class AddressNode {
    private final AddressBean addressBean;
    private Map<String, AddressNode> childNodeMap;
    private AddressNode parentAddressNode;
    private String keyString;

    /**
     * 构造函数.
     *
     * @param addressBean 地址信息AddressBean
     */
    public AddressNode(AddressBean addressBean) {
        this.addressBean = addressBean;
        this.keyString = addressBean.getName();
        this.childNodeMap = new HashMap<>();
    }

    /**
     * 将输入的地址节点添加到父节点的子节点集中.
     *
     * @param addressNode 地址节点
     */
    public void addChildNode(AddressNode addressNode) {
        if (addressNode == null) {
            throw new RuntimeException("AddressNode can not be null.");
        }

        if (childNodeMap.containsValue(addressNode)) {
            throw new RuntimeException(addressNode.getKeyString() + " have same child address.");
        } else {
            childNodeMap.put(addressNode.keyString, addressNode);
        }
    }

    /**
     * 返回当前节点的父地址信息节点.
     *
     * @return 父地址信息节点
     */
    public AddressNode getParentAddressNode() {
        if (parentAddressNode == null) {
            throw new RuntimeException(this.getKeyString() + " parent is null.");
        }
        return this.parentAddressNode;
    }

    /**
     * 设置父地址信息节点.
     *
     * @param parentAddressNode 父地址信息节点
     */
    public void setParentAddressNode(AddressNode parentAddressNode) {
        if (parentAddressNode == null) {
            throw new RuntimeException("ParentAddressNode can not be null.");
        }
        this.parentAddressNode = parentAddressNode;
    }

    /**
     * 获取节点的关键字.
     *
     * @return 返回此节点的关键字
     */
    public String getKeyString() {
        return keyString == null || keyString.isEmpty() ? "" : keyString;
    }

    /**
     * 获取地址别名.
     *
     * @return 地址别名
     */
    public String getFullName() {
        String fullName = addressBean.getFullName();
        return fullName.isEmpty() ? null : fullName;
    }

    /**
     * 获取地址后缀.
     *
     * @return 地址后缀
     */
    public String getSuffix() {
        String suffix = addressBean.getSuffix();
        //修改，后缀可以为空
        //return suffix.isEmpty() ? null : suffix;
        return suffix;
    }

    /**
     * 获取地址等级.
     *
     * @return 地址等级
     */
    public Level getLevel() {
        return addressBean.getLevel() == null ? Level.UNKNOWN : addressBean.getLevel();
    }

    /**
     * 获取地址的全称和简称.
     *
     * @return 包含地址全称和简称的长度为2的数组
     */
    public String[] getAllNames() {

        //name存放地名简称
        String name = addressBean.getName();

        //addressName存放地名全称
        String addressName;
        if (!addressBean.getFullName().isEmpty()) {
            addressName = addressBean.getFullName() + addressBean.getSuffix();
        } else {
            addressName = name + addressBean.getSuffix();
        }

        //添加
        if (addressBean.getSuffix().isEmpty()) {
            return new String[]{addressName};
        } else {
            return new String[]{name, addressName};
        }
    }

    /**
     * 获取节点的子节点.
     *
     * @return 包含节点的所有的子节点的Map
     */
    public Collection<AddressNode> getChildNodeList() {
        if (childNodeMap.values().isEmpty()) {
            return null;
        }
        return Collections.unmodifiableCollection(childNodeMap.values());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof AddressNode) {
            AddressNode other = (AddressNode) obj;
            return other.addressBean == this.addressBean
                    && other.childNodeMap == this.childNodeMap;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return addressBean.getName().hashCode()
                + addressBean.getSuffix().hashCode()
                + addressBean.getLevel().hashCode();
    }

    @Override
    public String toString() {
        return addressBean.toString();
    }
}
