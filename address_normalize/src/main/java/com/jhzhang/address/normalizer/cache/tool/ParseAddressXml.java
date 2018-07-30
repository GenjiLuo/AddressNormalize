package com.jhzhang.address.normalizer.cache.tool;

import com.jhzhang.address.normalizer.cache.bean.AddressBean;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.cache.structure.AddressTree;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.cache.structure.AddressTree;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 1.解析节点生成地址树
 * 2.将节点属性转为地址信息
 * 3.初始化Level类
 */
public class ParseAddressXml {
    private static final String ADDRESS_NAME = "name";  //xml中，地址简称，属性名称
    private static final String ADDRESS_FULL_NAME = "fullName";  //xml中，地址别名，属性名称
    private static final String ADDRESS_SUFFIX = "suffix";  //xml中，地址后缀，属性名称
    private static final String ADDRESS_LEVEL = "level";  //xml中，地址级别，属性名称

    private static final String USE = "use";  //节点属性标志位，表示是否取该值
    private static final String LEVEL = "number";  //节点属性，表示级别大小，如A

    /**
     * 解析xml，生成树.
     *
     * @param addressDataPath 地址元素字典配置路径
     * @return 生成后的地址树
     */
    public static AddressTree createAddressTree(String addressDataPath) {
        InputStream stream = null;
        try {
            stream = new FileInputStream(new File(addressDataPath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return createAddressTree(stream);
    }

    /**
     * 解析xml，生成树.
     *
     * @param addressDataStream 地址元素字典配置读入流
     * @return 生成后的地址树
     */
    public static AddressTree createAddressTree(InputStream addressDataStream) {
        AddressTree addressTree = new AddressTree();
        Document document;
        try {
            document = new SAXReader().read(addressDataStream);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        Element root = document.getRootElement();// 获得根节点
        for (Iterator children = root.elementIterator(); children.hasNext(); ) {
            parseNode((Element) children.next(), addressTree.getRootAddressNode(), addressTree);
        }
        return addressTree;
    }

    /**
     * 解析节点，获得地址信息.
     *
     * @param xmlNode           xml地址元素节点
     * @param parentAddressNode 父地址节点
     * @param addressTree       地址树
     */
    private static void parseNode(Element xmlNode, AddressNode parentAddressNode,
                                  AddressTree addressTree) {
        if (xmlNode == null || parentAddressNode == null) {
            return;
        }

        //获取节点属性
        Iterator nodeAttributes = xmlNode.attributeIterator();

        //创建一个新的节点
        AddressNode addressNode = addressTree.createAddressNode(
                transAttributesToAddressBean(nodeAttributes));

        //新建标志，记录添加节点是否成功
        Boolean addSuccess = addressTree.addAddressNodeToTree(addressNode, parentAddressNode);
        if (addSuccess) {
            //出口
            for (Iterator childNode = xmlNode.elementIterator(); childNode.hasNext(); ) {
                parseNode((Element) childNode.next(), addressNode, addressTree);
            }

        } else {
            throw new RuntimeException(addressNode.getKeyString() + " has not added to parent.");
        }
    }

    /**
     * 根据节点元素生成AddressBean地址信息.
     *
     * @param nodeAttributes 节点属性迭代器
     * @return 地址信息AddressBean
     */
    private static AddressBean transAttributesToAddressBean(Iterator nodeAttributes) {
        String name = null;
        String fullName = null;
        String suffix = null;
        Level level = null;

        for (; nodeAttributes.hasNext(); ) {
            Attribute attr = (Attribute) nodeAttributes.next();
            if (ADDRESS_NAME.equals(attr.getName())) {
                if (attr.getValue() != null) {
                    name = attr.getValue();
                }
            }
            if (ADDRESS_FULL_NAME.equals(attr.getName())) {
                if (attr.getValue() != null) {
                    fullName = attr.getValue();
                }
            }
            if (ADDRESS_SUFFIX.equals(attr.getName())) {
                if (attr.getValue() != null) {
                    suffix = attr.getValue();
                }
            }
            if (ADDRESS_LEVEL.equals(attr.getName())) {
                if (attr.getValue() != null) {
                    level = Level.valueOf(attr.getValue());
                }
            }
        }
        //注释后缀不可为空 || suffix.isEmpty()
        if (name == null || name.isEmpty()
//                || suffix == null
                || level == null) {
            return null;
        }
        return new AddressBean(name, fullName, suffix, level);
    }

    /**
     * 初始化Level类，赋常量值.
     *
     * @param addressLevelStream 地址等级识别词配置输入流
     */
    public static void initLevelWithSuffix(InputStream addressLevelStream) {
        Document document;
        try {
            // 拿到当前线程，然后拿到它对用的ClassLoader，执型read（）方法返回一个Document，对应了将要解析的xml文件
            /*document = new SAXReader().read(Thread.currentThread()
                    .getContextClassLoader().getResourceAsStream(addressLevelPath));*/
            document = new SAXReader().read(addressLevelStream);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        Element root = document.getRootElement();
        for (Iterator children = root.elementIterator(); children.hasNext(); ) {
            Element rootChild = (Element) children.next();
            //获得level这一节点的属性值,是否取该值和该地址级别
            Boolean useLevel = Boolean.valueOf(rootChild.attribute(USE).getValue());
            Level level = Level.valueOf(rootChild.attribute(LEVEL).getValue());

            //如果use属性值为true，则遍历子节点
            if (useLevel) {
                for (Iterator subChildren = rootChild.elementIterator(); subChildren.hasNext(); ) {
                    Element child = (Element) subChildren.next();
                    //如果子节点的use属性为真，则取该值
                    Boolean useSuffix = Boolean.valueOf(child.attribute(USE).getValue());
                    if (useSuffix) {
                        //获得此子节点对应的等级，为等级设置常量值
                        level.addKeyword(child.getText());
                    }
                }
            }
        }
    }

    /**
     * 初始化Level类，赋常量值.
     *
     * @param addressLevelPath 地址等级识别词配置路径
     */
    public static void initLevelWithSuffix(String addressLevelPath) {
        try {
            initLevelWithSuffix(new FileInputStream(new File(addressLevelPath)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将所有的后缀都放入到一个列表中
     *
     * @param addressLevelPath 后缀名的xml文件
     * @return 地址等级及关键词
     */
    public static HashMap<Level, HashSet> getAllLevelSuffix(String addressLevelPath) {
        Document document;
        try {
            document = new SAXReader().read(new FileInputStream(new File(addressLevelPath)));
        } catch (DocumentException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        // 初始化
        HashMap<Level, HashSet> suffixMap = new HashMap<>(16);
        for (Level level : Level.values()) {
            suffixMap.put(level, new HashSet());
        }

        Element root = document.getRootElement();
        for (Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            //获得该地址级别
            Level level = Level.valueOf(element.attribute(LEVEL).getValue());
            //遍历子节点
            HashSet set = suffixMap.get(level);
            for (Iterator subChildren = element.elementIterator(); subChildren.hasNext(); ) {
                Element child = (Element) subChildren.next();
                String text = child.getText();
                if (!set.contains(text)) {
                    set.add(text);
                }
            }
        }
        return suffixMap;
    }
}
