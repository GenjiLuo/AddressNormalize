package com.jhzhang.address.normalizer.common;

import com.jhzhang.address.normalizer.cache.AddressQuery;
import com.jhzhang.address.normalizer.cache.exception.InvalidAddressException;
import com.jhzhang.address.normalizer.cache.exception.NotFindAddressException;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.core.support.Position;
import com.jhzhang.address.normalizer.segment.Segment;
import com.jhzhang.address.normalizer.segment.Segments;
import com.jhzhang.address.normalizer.util.CommonUtils;
import com.jhzhang.address.normalizer.util.VectorUtil;
import com.jhzhang.address.normalizer.cache.AddressQuery;
import com.jhzhang.address.normalizer.cache.exception.InvalidAddressException;
import com.jhzhang.address.normalizer.cache.exception.NotFindAddressException;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.core.support.Position;
import com.jhzhang.address.normalizer.segment.Segment;
import com.jhzhang.address.normalizer.segment.Segments;
import com.jhzhang.address.normalizer.util.CommonUtils;
import com.jhzhang.address.normalizer.util.VectorUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * 地址向量.
 */
public class Vector {
    /**
     * 第五个等级在枚举类中对应的序号.
     */
    private static final int MAX_NORMALIZED_LEVEL = Level.E.ordinal();
    private static final Segment SEGMENT = Segments.getLongestSegment();
    private static AddressQuery query = AddressQuery.getInstance();
    /**
     * 等级已经确定的地址元素数组.
     */
    @SuppressWarnings("unchecked")
    public List<Element>[] elements = new LinkedList[Level.size()];

    /**
     * 构造函数.
     */
    public Vector() {
        for (int i = 0; i < Level.size(); i++) {
            elements[i] = new LinkedList<>();
        }
    }

    /**
     * 将字符串的地名经最长分词后挂载到对应的等级形成初步的地址向量.
     *
     * @param address 将字符串的地名
     * @return 初步的地址向量
     */
    public static Vector valueOf(String address) {
        Vector vector = new Vector();
        List<Element> segElement = VectorUtil.removeDuplicateElement(SEGMENT.seg(address));
        int i = 0;
        // 遍历每一个地址元素
        while (i < segElement.size() - 1) {
            Element element = segElement.get(i);
            // 如果是前五个等级，并且地名不为空，则挂载到指定位置
            if (element.getLevel().ordinal() <= MAX_NORMALIZED_LEVEL) {
                vector.mount(element);
            } else {
                // 如果不是前五个等级
                Element elementAfter = segElement.get(i + 1);
                if ("UNKNOWN".equals(element.getLevel().name())
                        && !"UNKNOWN".equals(elementAfter.getLevel().name())
                        && elementAfter.getLevel().ordinal() > MAX_NORMALIZED_LEVEL
                        && !notAddSuffix(elementAfter)) {
                    // 如果前一个地址元素为UNKNOWN那么它为地名，如果后一个不是UNKNOWN那么它为地址关键字，则将前后地址元素一起添加到地址向量
                    vector.mount(elementAfter.getLevel().ordinal(), element);
                    vector.mount(elementAfter.getLevel().ordinal(), elementAfter);
                    i++;
                } else {
                    vector.mount(element);
                }
            }
            i++;
        }
        // 最后一个地址元素
        if (i == segElement.size() - 1) {
            Element lastElement = segElement.get(i);
            vector.mount(lastElement);
        }
        return vector;
    }

    /**
     * 判断一个地址元素是否可靠 即他的后缀是本身存在的 还是在分词时根据词库加上去的
     *
     * @param element 需要判断的元素
     * @return 如果是本身存在的则返回true，否则返回false.
     */
    public static boolean notAddSuffix(Element element) {
        if (element.getStart() == -1 || element.getEnd() == -1) {
            return false;
        }
        if (element.getSuffix().length() >= 0) {
            if (element.getEnd() - element.getStart()
                    == element.getName().length() + element.getSuffix().length()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获得结果中最高得分的列表
     *
     * @param sets
     * @return
     */
    public List<Element> getMaxScoreList(HashSet<AddressNode> sets) throws InvalidAddressException {
        List<Element> maxScoreList = null;
        // 记录最大得分
        int max = 0;
        // 记录重复数据最大得分
        int replaceScore = 0;
        for (AddressNode adNode : sets) {
            // 获得该节点下的列表
            List<Element> lists = getCondidateElementList(adNode);
            int score = countSimilarBetweenTwoList(lists);
            if (max < score) {
                max = score;
                maxScoreList = lists;
            } else if (max == score) {
                replaceScore = score;
            }
        }
        if (max <= 2) {
            // 得分过低
            throw new InvalidAddressException("02 the administration score is too low");
        }
        if (replaceScore == max) {
            throw new InvalidAddressException("03 the administration contain same address score");
        }
        return maxScoreList;
    }

    /**
     * 获得某个节点对应的切分元素,如果不存在，则使用AddressNode中节点元素替代
     *
     * @param adNode
     * @return
     */
    public List<Element> getCondidateElementList(AddressNode adNode) {
        int index = adNode.getLevel().ordinal();
        List<Element> result = new ArrayList<>(index + 1);
        // 初始化
        for (int i = 0; i < index + 1; i++) {
            result.add(Element.EMPTY);
        }
        AddressNode pNode = adNode;
        // 是否查找到相应元素
        boolean isFound = false;
        while (true) {
            index = pNode.getLevel().ordinal();
            isFound = false;
            for (Element ele : elements[index]) {
                if (ele.getName().equals(pNode.getKeyString())) {
                    // 如果元素后缀不等，则更改
                    if (!ele.getSuffix().equals(pNode.getSuffix())) {
                        ele = new Element(ele.getName(), pNode.getSuffix(), ele.getLevel(), ele.getStart(), ele.getEnd());
                    }
                    result.set(index, ele);
                    isFound = true;
                    break;
                }
            }
            // 如果没有找到
            if (!isFound) {
                result.set(index, new Element(pNode.getKeyString(), pNode.getSuffix(), pNode.getLevel()));
            }
            // 最后找到了A等级，则输出
            if (pNode.getLevel().equals(Level.A)) {
                return result;
            }
            pNode = pNode.getParentAddressNode();
        }
    }

    /**
     * 计算计算元素列表与vector之间的相似度
     *
     * @param lists
     * @return
     */
    public int countSimilarBetweenTwoList(List<Element> lists) {
        int score = 0;
        for (int i = 0, length = lists.size(); i < length; i++) {
            for (Element ele : elements[i]) {
                Element element = lists.get(i);
                // 如果elements中包含了相同数据(名和等级相同)，得分+1，并且替换其中的元素
                if (element.equals(ele)) {
                    score++;
                    // 元素后缀相等+1,且不是补全添加的+1
                    if (notAddSuffix(ele) && ele.getSuffix().equals(element.getSuffix())) {
                        score++;
                    }
                    // 如果包含了起始位置元素，则多加一分
                    if (ele.getStart() == 0) {
                        score++;
                    }
//                    lists.set(i, ele);
                }
            }
        }
        // 元素的偏移量符合规则
        for (int i = 0; i < lists.size() - 1; i++) {
            Element current = lists.get(i);
            Element next = lists.get(i + 1);
            if (current.getStart() != -1) {
                if (current.getStart() < next.getStart()) {
                    score++;
                }
            }
        }
        return score;
    }

    /**
     * 预处理前面的五个等级
     *
     * @param vector 输入的地址
     */
    public HashSet<AddressNode> findAdministrationCondidate(Vector vector) {
        // 用来存放所有匹配的
        HashSet<AddressNode> results = new HashSet<>();
        // 从后往前来读取挂载的数据
        for (int i = MAX_NORMALIZED_LEVEL; i >= 0; i--) {
            for (int j = vector.indexOf(i).size() - 1; j >= 0; j--) {
                Element childElement = vector.indexOf(new Position(i, j));
                if (ElementType.valueOf(childElement).equals(ElementType.SUFFIX)
                        || ElementType.valueOf(childElement).equals(ElementType.UNKNOW)) {
                    continue;
                }
                // 查询单个元素是否能够单一的匹配到数据
                List<AddressNode> firstResult = query.getSpecificAddressNodeList(childElement);
                if (firstResult != null && firstResult.size() == 1) {
                    results.add(firstResult.get(0));
                } else {
                    // 存在多个可能数据
                    for (int h = i - 1; h > 0; h--) {
                        for (int v = vector.indexOf(h).size() - 1; v >= 0; v--) {
                            Element parentElement = vector.indexOf(new Position(h, v));
                            if (ElementType.valueOf(parentElement).equals(ElementType.SUFFIX)
                                    || ElementType.valueOf(parentElement).equals(ElementType.UNKNOW)) {
                                continue;
                            }
                            if (childElement.getStart() != parentElement.getStart()) {
                                List<AddressNode> secondResult;
                                try {
                                    secondResult = query.getSpecificAddressNodeList(parentElement, childElement);
                                } catch (NotFindAddressException | NullPointerException note) {
                                    continue;
                                }
                                results.addAll(secondResult);
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * 根据给定的下标获取对应位置的地址元素.
     *
     * @param index 指定下标
     * @return 对应位置的地址元素
     */
    public List<Element> indexOf(int index) {
        if (index >= elements.length || index < 0) {
            return null;
        }
        List<Element> list = new LinkedList<>();
        list.addAll(elements[index]);
        return list;
    }

    /**
     * 根据给定的元素匹配获取元素的位置.
     *
     * @param element 给定的元素
     * @return 匹配中的元素
     */
    public Position indexOf(Element element) {
        for (int i = 0; i < elements.length; i++) {
            List<Element> list = indexOf(i);
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).equals(element) && list.get(j).getStart() == element.getStart()) {
                    return new Position(i, j);
                }
            }
        }
        return null;
    }

    /**
     * 某个元素是否在该元素前被挂载
     *
     * @param element
     * @param position 参考的位置
     * @return
     */
    public boolean isMountedBefore(Element element, Position position) {
        Position pre = getPreviousPosition(position);
        while (pre != null) {
            Element elementPre = indexOf(pre);
            if (elementPre.getStart() == element.getStart()) {
                if (elementPre.getName().equals(element.getName())) {
                    return true;
                }
            }
            pre = getPreviousPosition(pre);
        }
        return false;
    }

    /**
     * 某个元素是否在该元素后被挂载.偏移量相同，名或后缀之一相同
     *
     * @param element
     * @param position 参考的位置
     * @return
     */
    public boolean isMountedAfter(Element element, Position position) {
        Position next = getNextPosition(position);
        while (next != null) {
            Element elementAfter = indexOf(next);
            if (elementAfter.getStart() == element.getStart()) {
                if (elementAfter.getName().equals(element.getName())
                        || element.getSuffix().equals(element.getSuffix())) {
                    return true;
                }
            }
            next = getNextPosition(next);
        }
        return false;
    }

    /**
     * 获取指定位置上的元素.
     *
     * @param position 指定的位置
     * @return 指定位置上的元素
     */
    public Element indexOf(Position position) {
        int i = position.getHorizon();
        if (i >= elements.length || i < 0) {
            return null;
        }
        List<Element> list = indexOf(i);
        int j = position.getVertical();
        if (j < 0 || j >= list.size()) {
            return null;
        }
        return list.get(j);
    }

    /**
     * 将list放在地址向量中指定的位置.
     *
     * @param list  要放置的Element列表
     * @param index 指定的位置
     */
    public void setListOnIndex(List<Element> list, int index) {
        if (index >= elements.length || index < 0) {
            return;
        }
        elements[index] = list;
    }

    /**
     * 获取一个位置的前驱位置.
     *
     * @param position 当前位置
     * @return 前驱位置
     */
    public Position getPreviousPosition(Position position) {
        int horizon = position.getHorizon();
        int vertical = position.getVertical();
        if (vertical <= 0) {
            horizon = horizon - 1;
            if (horizon < 0) {
                return null;
            }
            vertical = indexOf(horizon).size() - 1;
            if (vertical < 0) {
                return getPreviousPosition(new Position(horizon, vertical));
            }
        } else {
            vertical = vertical - 1;
        }
        return new Position(horizon, vertical);
    }

    /**
     * 获取当前位置前一个不为空的的Element.
     *
     * @param position 当前位置
     * @return 前一个不为空的的Element
     */
    public Element getPreviousElement(Position position) {
        Position prePosition = getPreviousPosition(position);
        if (prePosition == null) {
            return null;
        }
        return indexOf(prePosition);
    }

    /**
     * 获取当前位置的后继元素的位置.
     *
     * @param position 当前位置
     * @return 当前位置的后继元素
     */
    public Position getNextPosition(Position position) {
        int horizon = position.getHorizon();
        int vertical = position.getVertical();
        if (vertical >= indexOf(horizon).size() - 1) {
            horizon = horizon + 1;
            if (horizon > elements.length - 1) {
                return null;
            }
            vertical = 0;
            if (vertical > indexOf(horizon).size() - 1) {
                return getNextPosition(new Position(horizon, vertical));
            }
        } else {
            vertical = vertical + 1;
        }
        return new Position(horizon, vertical);
    }

    /**
     * 在指定位置挂载给定的地址元素.
     *
     * @param index   指定位置
     * @param element 给定地址元素
     */
    public void mount(int index, Element element) {
        if (index > size() || index < 0) {
            return;
        }
        Position position = null;
        // 重复元素不挂载
        if (elements[index] != null && !indexOf(index).contains(element)) {
            position = getMountPosition(element, index);
        }
        if (position == null) {
            return;
        }
        addElement(element, position);
    }

    /**
     * 向地址向量中插入地址元素，地址元素挂载在对应的等级下面.切分的同一元素只会在同一等级下挂载一次<P>
     * 例如,同时切分出<望江，镇，D>和<望江，街道，D>只会挂载一个元素在对应等级下</P>
     *
     * @param element 给定地址元素
     */
    public void mount(Element element) {
        Position position = null;
        // 如果element为空，则找到地址向量中下一个为空的位置，直接挂载element
        // 如果element不为空，则获取其等级，然后在地址向量中对应的等级上挂载element
        if (element.getLevel().equals(Level.UNKNOWN)) {
            position = getMountPosition(element);
        } else {
            int level = element.getLevel().ordinal();
            if (elements[level] != null && !indexOf(level).contains(element)) {
                position = getMountPosition(element, element.getLevel().ordinal());
            }
        }
        if (position == null) {
            return;
        }
        addElement(element, position);
    }

    /**
     * 将定级错误的元素回退到原来的位置.
     *
     * @param element      定级错误的元素
     * @param currPosition 元素当前位置
     */
    public void rollBack(Element element, Position currPosition) {
        removeElement(currPosition);
        Position rollBackPosition = getRollBackPosition(element);
        Element subElement = indexOf(rollBackPosition);
        // 如果一个元素的前驱节点已被删除，那么它不需要回退
        if (subElement != null && isSubElementDeleted(element, subElement)) {
            return;
        }
        Element el;
        // 确保元素后移
        if (rollBackPosition.getHorizon() > currPosition.getHorizon()) {
            if (notAddSuffix(element)) {
                el = element;
            } else {
                el = new Element(element.getName(), "", Level.UNKNOWN, element.getStart(), element.getEnd());
            }
            addElement(el, rollBackPosition);
        }
    }

    /**
     * 判断一个地址元素的后继元素是否被删除 如果删除返回TRUE 如果没有删除 返回FALSE.
     *
     * @param preElement 当前地址元素
     * @param subElement 后继地址元素
     * @return 当前地址元素的后继元素是否被删除
     */
    private boolean isSubElementDeleted(Element preElement, Element subElement) {
        int preOffset = preElement.getStart();
        int subOffset = subElement.getStart();
        int length = preElement.getEnd() - preElement.getStart();
        return subOffset - preOffset > length;
    }

    /**
     * 获取要回退的地址元素应该回退的位置.
     *
     * @param element 要回退的地址元素
     * @return 回退的位置
     */
    public Position getRollBackPosition(Element element) {
        int offset = element.getStart();
        int min = Integer.MAX_VALUE;
        int horizon = elements.length - 1;
        int vertical = elements[elements.length - 1].size();
        // 搜寻与element最近的元素的位置
        for (int i = 0; i < elements.length; i++) {
            List<Element> elementList = indexOf(i);
            for (int j = 0; j < elementList.size(); j++) {
                if (elementList.get(j).getStart() > offset) {
                    int temp = elementList.get(j).getStart() - offset;
                    if (temp <= min) {
                        min = temp;
                        horizon = i;
                        vertical = j;
                    }
                }
            }
        }
        return new Position(horizon, vertical);
    }

    /**
     * 获取地址元素要挂载的位置.
     *
     * @param element 要挂载的地址元素
     * @return 要挂载的位置
     */
    private Position getMountPosition(Element element) {
        // 如果元素为UNKONWN,找到数组中下一个为空的位置的索引，把该索引作为插入位置
        if (element.getLevel().equals(Level.UNKNOWN)) {
            int offset = element.getStart();
            int min = Integer.MAX_VALUE;
            int horizon = 0;
            int vertical = 0;
            // 搜寻与element最近的元素的位置
            for (int i = elements.length - 1; i >= 0; i--) {
                List<Element> elementList = indexOf(i);
                for (int j = 0; j < elementList.size(); j++) {
                    if (elementList.get(j).getStart() < offset) {
                        int temp = offset - elementList.get(j).getStart();
                        if (temp <= min) {
                            min = temp;
                            horizon = i;
                            vertical = j + 1;
                        }
                    }
                }
            }
            return new Position(horizon, vertical);
        } else {
            return null;
        }
    }

    /**
     * 获取地址元素要挂载的位置.
     *
     * @param element 要挂载的地址元素
     * @param horizon 已经指定的水平位置
     * @return 要挂载的位置
     */
    private Position getMountPosition(Element element, int horizon) {
        int vertical = 0;
        if (indexOf(horizon).size() <= 0) {
            return new Position(horizon, vertical);
        }

        while (vertical < indexOf(horizon).size()) {
            if (indexOf(horizon).get(vertical).getStart() >= element.getStart()) {
                return new Position(horizon, vertical);
            }
            vertical++;
        }
        return new Position(horizon, vertical);
    }

    /**
     * 从当前级别的最后一个位置向前，找出对应等级列表上不连续的位置，即它前一个元素的回退位置不是当前位置.
     *
     * @param level 对应等级
     * @return 对应等级列表上不连续的位置
     */
    public Position getBreakOffPosition(Level level) {
        List<Element> list = indexOf(level.ordinal());
        int i = 0;
        while (i < list.size()) {
            Position currPosition = new Position(level.ordinal(), i);
            Element preElement;
            if (i == 0) {
                preElement = getPreviousElement(currPosition);
            } else {
                preElement = list.get(i - 1);
            }
            if (!currPosition.equals(getRollBackPosition(preElement))) {
                return currPosition;
            }
            i++;
        }
        return null;
    }

    /**
     * 删除指定位置的元素.
     *
     * @param index 指定位置
     * @return 删除的元素
     */
    public List<Element> remove(int index) {
        if (index < 0 || index > size()) {
            return null;
        }
        List<Element> temp = elements[index];
        elements[index] = new LinkedList<>();
        return temp;
    }

    /**
     * 删除指定位置上的元素.
     *
     * @param position 指定位置
     * @return 删除的元素
     */
    public Element removeElement(Position position) {
        if (position.getHorizon() >= elements.length || position.getHorizon() < 0) {
            return null;
        }
        List<Element> list = indexOf(position.getHorizon());
        if (list.size() <= 0) {
            return null;
        }
        if (position.getVertical() < 0 || position.getVertical() > list.size() - 1) {
            return null;
        }
        Element element = list.get(position.getVertical());
        list.remove(position.getVertical());
        elements[position.getHorizon()] = list;
        return element;
    }

    /**
     * 删除指定元素.
     *
     * @param element 指定的元素
     */
    public void removeElement(Element element) {
        Position position = indexOf(element);
        removeElement(position);
    }

    /**
     * 在指定位置添加指定地址元素.
     *
     * @param element  要添加的地址元素
     * @param position 指定的位置
     */
    private void addElement(Element element, Position position) {
        if (position.getHorizon() >= elements.length || position.getHorizon() < 0) {
            return;
        }
        List<Element> list = indexOf(position.getHorizon());
        if (position.getVertical() < 0 || position.getVertical() > list.size()) {
            return;
        }
        list.add(position.getVertical(), element);
        elements[position.getHorizon()] = list;
    }

    /**
     * Vector转换为List.
     *
     * @return 存储每个地址元素的List
     */
    public List<String> asList() {
        List<String> result = new ArrayList<>();
        for (List<Element> elementList : elements) {
            StringBuilder sb = new StringBuilder();
            for (Element element : elementList) {
                //将小区等级阿拉伯数字转为中文
                if (element.getLevel().ordinal() == Level.H.ordinal()) {
                    element.setName(CommonUtils.convertNumToChinese(element.getName()));
                    sb.append(element.toString());
                } else if (element.getLevel().ordinal() >= Level.J.ordinal()
                        && element.getLevel().ordinal() <= Level.M.ordinal()) {
                    //将楼栋、单元、楼层、房间号转化为阿拉伯数字
                    element.setName(CommonUtils.covertToNum(element.getName()));
                    sb.append(element.getName()).append(element.getSuffix());
                } else {
                    sb.append(element.toString());
                }
            }
            result.add(sb.toString());
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hashcode = 0;
        for (List<Element> list : elements) {
            for (Element element : list) {
                hashcode += element.hashCode();
            }
        }
        return hashcode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Vector) {
            Vector other = (Vector) obj;
            for (int i = 0; i < size(); i++) {
                if (!elements[i].equals(other.elements[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size() - 1; i++) {
            if (i > MAX_NORMALIZED_LEVEL) {
                sb.append(Level.values()[i] + "/");
            }
            sb.append(elements[i].toString()).append("\t");
        }
        sb.append(elements[size() - 1].toString()).append("\n");
        return sb.toString();
    }

    public int size() {
        return elements.length;
    }
}
