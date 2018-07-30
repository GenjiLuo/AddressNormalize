package com.jhzhang.address.normalizer.cache;

import com.jhzhang.address.normalizer.cache.exception.NotFindAddressException;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.ElementType;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.cache.exception.NotFindAddressException;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 实现地址查询的功能，对外提供两个接口.
 */
public final class AddressQuery implements IQuery {

    private static volatile AddressQuery addressQuery = null;
    private AddressMatcher addressMatcher;

    private AddressQuery() {
    }

    /**
     * 实例化.
     *
     * @return 返回一个AddressQuery实例
     */
    public static AddressQuery getInstance() {
        if (addressQuery == null) {
            synchronized (AddressQuery.class) {
                if (addressQuery == null) {
                    addressQuery = new AddressQuery();
                }
            }
        }
        return addressQuery;
    }

    public void init(String addressDataPath, String addressLevelPath) {
        addressMatcher = new AddressMatcher();
        addressMatcher.init(addressDataPath, addressLevelPath);
    }

    public void init(InputStream addressDataStream, InputStream addressLevelStream) {
        addressMatcher = new AddressMatcher();
        addressMatcher.init(addressDataStream, addressLevelStream);
    }

    /**
     * 根据输入的确定的上级元素和本级元素，得到中间的地址元素.
     *
     * @param parentElement 确定的上级地址元素；如：[江苏，省，A]
     * @param childElement  确定的本级地址元素；如：[建邺，区，C]
     * @return 中间的地址元素；如：{[南京，市，B]}
     * @throws NotFindAddressException 输入的地址信息不正确，在地名库中找不到具体地址信息
     */
    @Override
    public List<Element> getSpecificAddressElementList(Element parentElement, Element childElement)
            throws NotFindAddressException {

        if (parentElement == null || childElement == null || childElement == Element.EMPTY) {
            throw new NotFindAddressException("Element can not be null.");
        }

        //新建一个List存放转换后的Element
        List<Element> elementList = new ArrayList<>();

        //如果parentElement为EMPTY，则输出childElement的所有上级节点
        if (parentElement == Element.EMPTY) {
            elementList = getAllElements(childElement);
            return elementList;
        }

        //Element转为AddressNode
        List<AddressNode> parentAddressNodeList = addressMatcher
                .keywordMatch(parentElement.getName() + parentElement.getSuffix());
        List<AddressNode> childAddressNodeList = addressMatcher
                .keywordMatch(childElement.getName() + childElement.getSuffix());

        //如果匹配后的列表为空，则抛出异常；我们要处理的是能匹配到地名库中的地址
        if (parentAddressNodeList.isEmpty() || childAddressNodeList.isEmpty()) {
            throw new NotFindAddressException("Element has no AddressNode matched, "
                    + "Element may be null.");
        }

        //定义一个参数记录返回的地址数，如果地址树大于1说明返回的地址不为1
        int addressNumber = 0;

        //nodeList存放包含本级地址节点的所有地址节点
        List<AddressNode> nodesList = new ArrayList<>();

        //遍历验证每一个parentAddressNode和childAddressNode
        for (AddressNode parentNode : parentAddressNodeList) {
            if (parentNode.getLevel() != parentElement.getLevel()
                    || !Objects.equals(parentNode.getSuffix(), parentElement.getSuffix())) {
                continue;
            }
            for (AddressNode childNode : childAddressNodeList) {
                if (childNode.getLevel() != childElement.getLevel()
                        || !Objects.equals(childNode.getSuffix(), childElement.getSuffix())) {
                    continue;
                }

                //新建temp暂存查询结果
                List<AddressNode> temp = addressMatcher.getSpecificAddressNodeList(
                        parentNode, childNode);

                //如果节点为空，则继续下一次循环
                if (temp.isEmpty()) {
                    continue;
                }
                addressNumber++;
                nodesList.addAll(temp);
            }
        }

        //将AddressNode转为Element，上级地址列表不唯一时，抛出异常
        if (!nodesList.isEmpty() && addressNumber == 1) {
            for (AddressNode node : nodesList) {
                Element element = addressMatcher.nodeTransToElement(node, -1, -1);
                elementList.add(element);
            }
        } else {
            throw new NotFindAddressException("AddressName may have AddressNode more than one.");
        }
        return elementList;
    }

    /**
     * 将未切分的字符串，切分返回其对应的地址元素.
     *
     * @param addressString 原始字符串；如：江苏南京建邺区
     * @return 切词后对应的地址元素；如：{[江苏，省，A，-1],[南京，市，B，-1],[建邺，区，C，-1]}
     */
    @Override
    public List<Element> getSegmentAddressElementList(String addressString) {
        return addressMatcher.keywordsMatch(addressString);
    }

    /**
     * 根据输入的Element返回此Element的所有上级.
     *
     * @param element 确定的本级Element
     * @return 含有所有上级Element的列表
     * @throws NotFindAddressException 如果Element为null时抛出异常，如果本级Element有歧义抛出异常
     */
    public List<Element> getAllElements(Element element) throws NotFindAddressException {
        //对Element进行判空
        if (element == null || element == Element.EMPTY) {
            throw new NotFindAddressException("childElement can not be null or EMPTY.");
        }

        //新建一个list来存放所有的上级节点
        List<AddressNode> allParentNodesList;

        //获得Element的关键字，地名的全称，用于全称匹配
        String keyString = element.getName() + element.getSuffix();

        //新建一个变量用于计数，如果数字大于1，说明地址有歧义，抛出异常，如果地址为1，输出所有上级
        int count = 0;

        //新建一个变量暂存满足要求的地址节点
        AddressNode tempAddressNode = null;

        for (AddressNode address : addressMatcher.keywordMatch(keyString)) {
            if (address.getLevel() == element.getLevel()
                    && Objects.equals(address.getSuffix(), element.getSuffix())) {
                count++;
                tempAddressNode = address;
            }
        }

        if (count == 1) {
            allParentNodesList = addressMatcher.getAllParentAddressNode(tempAddressNode);
        } else {
            throw new NotFindAddressException("This Element has AddressNode more than one "
                    + "or element's level is wrong");
        }

        Collections.reverse(allParentNodesList);

        //新建一个list存放节点转化为Element
        List<Element> elementList = new ArrayList<>();

        //将节点转化为Element
        for (AddressNode addressNode : allParentNodesList) {
            elementList.add(addressMatcher.nodeTransToElement(addressNode, -1, -1));
        }
        return elementList;
    }

    /**
     * 获得某个子类元素没有后缀的地址节点
     *
     * @param cElement
     * @param cElement
     * @return
     */
    @Override
    public List<AddressNode> getSpecificAddressNodeList(Element pElement, Element cElement) throws NotFindAddressException {
        //对Element进行判空
        if (cElement == null || cElement == Element.EMPTY) {
            throw new NotFindAddressException("childElement can not be null or EMPTY.");
        }

        //获得Element的关键字，地名的全称，用于全称匹配
        String keyString = cElement.getName();
        if (!ElementType.valueOf(cElement).equals(ElementType.HALF)) {
            keyString += cElement.getSuffix();
        }

        // 如果父类元素等级比子类元素等级小直接返回
        if (pElement.getLevel().compareTo(cElement.getLevel()) >= 0) {
            throw new NotFindAddressException("childElement level can’t be less than parentElement ");
        }

        //新建一个变量暂存满足要求的地址节点
        List<AddressNode> allResultNode = new ArrayList<>();
        // 迭代子类元素匹配的节点
        for (AddressNode cAddressNode : addressMatcher.keywordMatch(keyString)) {
            if (cAddressNode.getLevel() != cElement.getLevel()) {
                continue;
            }
            AddressNode pNode = cAddressNode.getParentAddressNode();
            // 如果等级不相等，则继续向上查找，直到相等
            while (pNode.getLevel().compareTo(pElement.getLevel()) > 0) {
                pNode = pNode.getParentAddressNode();
            }
            // 查找成功
            if (pNode.getKeyString().equals(pElement.getName())) {
                allResultNode.add(cAddressNode);
            }
        }
        return allResultNode;
    }

    /**
     * 查找单个元素能够定位出所有地址元素
     *
     * @param findEle
     * @return
     */
    public List<AddressNode> getSpecificAddressNodeList(Element findEle) {
        //对Element进行判空
        if (findEle == null || findEle == Element.EMPTY) {
            return null;
        }
        //获得Element的关键字，地名的全称，用于全称匹配
        String keyString = findEle.getName();
        if (!ElementType.valueOf(findEle).equals(ElementType.HALF)) {
            keyString += findEle.getSuffix();
        }
        // 如果名称和等级相等，则返回地址数据
        ArrayList<AddressNode> result = new ArrayList<>();
        for (AddressNode adNode : addressMatcher.keywordMatch(keyString)) {
            if (adNode.getLevel() == findEle.getLevel()) {
                result.add(adNode);
            }
        }
        return result;
    }

    /**
     * 获取某个字符串匹配到所有结点信息
     *
     * @param keyString
     * @return
     */
    public List<AddressNode> getSpecificAddressNodeList(String keyString) {
        //对Element进行判空
        if (keyString == null || keyString.isEmpty()) {
            return null;
        }
        // 如果名称和等级相等，则返回地址数据
        ArrayList<AddressNode> result = new ArrayList<>();
        for (AddressNode adNode : addressMatcher.keywordMatch(keyString)) {
            result.add(adNode);
        }
        return result;
    }

    /**
     * 获得某个地址节点所有的上级元素
     *
     * @param node
     */
    public List<Element> getAllElements(AddressNode node) {
//        if (node == null || node.getKeyString().isEmpty()) {
//            throw new RuntimeException("Address Node can not be null or EMPTY.");
//        }
        List<Element> elementList = new ArrayList<>();

        AddressNode pNode = node;
        while (true) {
            Element newElement = new Element(pNode.getKeyString(), pNode.getSuffix(), pNode.getLevel());
            elementList.add(newElement);
            // 如果到达了A级别
            if (pNode.getLevel().equals(Level.A)) {
                return elementList;
            }
            // 读取上一次父节点
            pNode = pNode.getParentAddressNode();
        }
    }
}
