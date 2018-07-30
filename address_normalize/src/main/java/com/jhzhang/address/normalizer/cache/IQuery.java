package com.jhzhang.address.normalizer.cache;

import com.jhzhang.address.normalizer.cache.exception.NotFindAddressException;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.cache.exception.NotFindAddressException;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;

import java.util.List;

/**
 * 用于查询数据库中的地址信息.
 */
public interface IQuery {

    /**
     * 根据输入的确定的本级地址元素和确定的本级地址元素的上级地址元素，返回本级和上级之间的具体地址信息
     *
     * @param parentElement 确定的上级地址元素；如：[江苏,省,A]
     * @param childElement  确定的本级地址元素；如：[建邺,区,C]
     * @return 包含所有本级地址元素和上级地址元素之间的地址元素的列表；如：{[南京,市,B]}
     * @throws NotFindAddressException 输入的地址信息不正确，在地名库中找不到具体地址信息
     */
    public List<Element> getSpecificAddressElementList(Element parentElement, Element childElement) throws NotFindAddressException;

    public List<AddressNode> getSpecificAddressNodeList(Element parentElement, Element childElement) throws NotFindAddressException;

    /**
     * 根据输入的原始的字符串，进行切割匹配后，返回这些地名对应的地址元素
     *
     * @param addressString 原始字符串；如：江苏南京建邺区
     * @return 切割后的地名对应的地址节点列表；如：{[江苏,省,A,0], [南京,市,B,2], [建邺,区,C,4]}
     */
    public List<Element> getSegmentAddressElementList(String addressString);
}
