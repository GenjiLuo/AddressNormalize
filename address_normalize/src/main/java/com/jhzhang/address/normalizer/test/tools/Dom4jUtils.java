package com.jhzhang.address.normalizer.test.tools;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author jhZhang
 * @date 2018/4/18
 */
public class Dom4jUtils {

    private static Document document;

    public static Document getInstance() throws DocumentException {
        if (document == null) {
            String parentAddressXml = "E:\\software\\SVN\\address_normalizer_full\\address_normalizer_full\\address_normalizer_config\\src\\main\\resources\\";
        String suffixLevelXml = "address.xml";
//            String suffixLevelXml = "1.xml";
            Dom4jUtils dom4jUtils = new Dom4jUtils();
            document = dom4jUtils.parse(parentAddressXml + suffixLevelXml);
        }
        return document;
    }


    public static void main(String[] args) throws DocumentException, IOException {
        Dom4jUtils.getInstance();
        Dom4jUtils.bar();
        Dom4jUtils.writeXml();
    }

    public static void bar() throws DocumentException {
        // 省
        Element root = document.getRootElement();
        //1. 迭代通过root节点的子节点
        for (Iterator<Element> it = root.elementIterator(); it.hasNext(); ) {
            pareNode(it, root);
        }
    }


    public static void pareNode(Iterator<Element> it, Element parent) {
        HashSet<String> sets = new HashSet<>();
        Element cur;
        String keyString;
        while (it.hasNext()) {
            cur = it.next();
            if (cur.elements().size() > 0) {
                pareNode(cur.elementIterator(), cur);
            }
            // 组合地址元素成关键词
            keyString = cur.attributeValue("name") + "/" + cur.attributeValue("suffix");
            if (sets.contains(keyString)) {
//                System.out.println(cur.attributeValue("level") + keyString);
                // 如果已经存在该元素，则删除元素
                parent.remove(cur);
            } else {
                sets.add(keyString);
            }
        }
    }

    public static void writeXml() throws IOException {
        XMLWriter writer = null;
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        try {
            writer = new XMLWriter(new FileWriter("E:\\software\\SVN\\address_normalizer_full\\address_normalizer_full\\address_normalizer\\src\\main\\resources\\address1.xml"), format);
            writer.write(document);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (writer != null) {
            writer.close();
        }
    }

    public Document parse(String url) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(url);
        return document;
    }
}
