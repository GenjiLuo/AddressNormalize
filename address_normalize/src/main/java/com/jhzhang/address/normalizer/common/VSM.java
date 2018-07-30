package com.jhzhang.address.normalizer.common;

import com.jhzhang.address.normalizer.core.AddressProcessor;
import com.jhzhang.address.normalizer.segment.Segment;
import com.jhzhang.address.normalizer.segment.Segments;
import com.jhzhang.address.normalizer.util.CommonUtils;
import com.jhzhang.address.normalizer.core.AddressProcessor;
import com.jhzhang.address.normalizer.segment.Segment;
import com.jhzhang.address.normalizer.segment.Segments;
import com.jhzhang.address.normalizer.util.CommonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * todo 后续删除这个
 * @author jhZhang
 * @date 2018/4/20
 */
public class VSM {
    /**
     * 第五个等级在枚举类中对应的序号.
     */
    private static final Segment SEGMENT = Segments.getLongestSegment();
    /**
     * 等级已经确定的地址元素数组.
     */
    @SuppressWarnings("unchecked")
    private List<Element>[] elements = new LinkedList[Level.size()];

    public VSM() {
        for (int i = 0; i < Level.size(); i++) {
            elements[i] = new LinkedList<>();
        }
    }


    /**
     * 移除列表中的重复元素片段,只保留起始终止位置不同,同一位置只保留不同等级元素.
     *
     * @param segElement
     * @return
     */
    public static List<Element> removeDuplicateElement(List<Element> segElement) {
        int length = segElement.size();
        if (length <= 1) {
            return segElement;
        }
        // 当前切分片段的长度
        int levelLen = 1;
        // 去重后元素挂载位置
        int mountPoint = 0;
        // 前一个变量
        Element preElement;
        // 后一个变量
        Element curElement;
        for (int i = 1; i < length; i++) {
            preElement = segElement.get(i - 1);
            curElement = segElement.get(i);
            if (curElement.getStart() != preElement.getStart()
                    && curElement.getEnd() != preElement.getEnd()) {
                segElement.set(++mountPoint, curElement);
                levelLen = 1;
            } else {
                // 判断同一重复等级中是否含有相同等级元素
                boolean isInsert = true;
                for (int j = 0; j < levelLen; j++) {
                    if (segElement.get(mountPoint - j).getLevel().equals(curElement.getLevel())) {
                        isInsert = false;
                        break;
                    }
                }
                if (isInsert) {
                    segElement.set(++mountPoint, curElement);
                    levelLen++;
                }
            }
        }
        return segElement.subList(0, ++mountPoint);
    }

    public static VSM valueOf(String address) {
        List<Element> segElement = SEGMENT.seg(address);
        segElement = removeDuplicateElement(segElement);
        VSM vsm = new VSM();
        vsm.getTrans(segElement);
        return vsm;
    }

    public static void main(String[] args) throws Exception {

        String raw = "江苏省南京市云龙山路88号烽火科技大厦";
        String address = CommonUtils.formatAddress(raw);
        System.out.println("原始:" + address);
        VSM vsm = valueOf(address);
        System.out.println("解析" + vsm.toString());
        Vector vector = vsm.convert2Vector();
        List<String> lists = AddressProcessor.getInstance().process(vector).asList();
        System.out.println(lists);

    }

    /**
     * 找出挂载前一个元素.如果没有,则返回为空
     *
     * @param index
     * @param lists
     * @return
     */
    public static Element getPreDiffElement(int index, List<Element> lists) {
        int end = getPreDiffElementIndex(index, lists);
        return end != -1 ? lists.get(end) : null;
    }

    public static int getPreDiffElementIndex(int index, List<Element> lists) {
        int len = lists.size();
        if (index < 0 || index >= len) {
            return -1;
        }
        Element curElement = lists.get(index);
        Element preElement = curElement;
        while (preElement.getStart() == curElement.getStart() && index > 0) {
            preElement = lists.get(--index);
        }
        return (index < len) ? index : len - 1;
    }

    /**
     * 找出挂载后一个元素.找到末尾范围最后一个元素
     *
     * @param index
     * @param lists
     * @return
     */
    public static int getNextDiffElementIndex(int index, List<Element> lists) {
        int len = lists.size();
        if (index < 0 || index >= len) {
            return -1;
        }
        Element curElement = lists.get(index);
        Element nextElement = curElement;
        while (nextElement.getStart() == curElement.getStart() && index < len - 1) {
            nextElement = lists.get(++index);
        }
        return (index < len) ? index : len - 1;
    }

    public static Element getNextDiffElement(int index, List<Element> lists) {
        int end = getNextDiffElementIndex(index, lists);
        return end != -1 ? lists.get(end) : null;
    }

    public Vector convert2Vector() {
        Vector vector = new Vector();
        vector.elements = elements;
        return vector;
    }

    /**
     * 添加某个元素列表
     */
    public void addElement(int index, List list) {
        if (index >= 0 && index < Level.size()) {
            elements[index].addAll(list);
        }
    }

    /**
     * 添加单个元素
     */
    public void addElement(int index, Element element) {
        if (index >= 0 && index < Level.size()) {
            if (!elements[index].contains(element))
                elements[index].add(element);
        }
    }

    /**
     * 判断某个元素是否在对应元素级别上存在冲突
     *
     * @param level
     * @param compareEle
     * @return
     */
    public boolean isConflict(int level, Element compareEle) {
        if (elements[level] != null) {
            for (Element element : elements[level]) {
                if (element.getName().equals(compareEle)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void getTrans(List<Element> segElement) {

        Element curElement;
        Element nextElement;
        int nextIndex;
        // 记录最近可靠地挂载等级
        int levelOrder;
        List<Element> mountList = new ArrayList<>();
        // 是否挂载
        boolean isMount = false;
        ElementType type;
        ElementType nextType;

        for (int i = 0, length = segElement.size(); i < length; i++) {
            curElement = segElement.get(i);
            type = ElementType.valueOf(curElement);
            levelOrder = curElement.getLevel().ordinal();
            nextIndex = getNextDiffElementIndex(i, segElement);
            nextElement = segElement.get(nextIndex);
            if (nextIndex != length) {
                nextType = ElementType.valueOf(nextElement);
            } else {
                nextType = ElementType.FULL;
            }

            switch (type) {
                case FULL: {
                    addElement(levelOrder, curElement);
                    break;
                }
                case HALF: {
                    if (curElement.getLevel().equals(Level.E)) {
                        if (curElement.getName().equals("社区") || curElement.getName().equals("新村")) {
                            if (mountList.size() > 0) {
                                Element newElement = new Element("", curElement.getName(), Level.E, curElement.getStart(), curElement.getEnd());
                                mountList.add(newElement);
                                levelOrder = Level.E.ordinal();
                                isMount = true;
                                break;
                            }
                        }
                    }
                    if (nextType.equals(ElementType.HALF) || nextElement.equals(ElementType.FULL)) {
                        addElement(levelOrder, curElement);
                    } else if (nextType.equals(ElementType.SUFFIX)) {
                        mountList.add(curElement);
                    } else {
                        if (isConflict(levelOrder, curElement)) {
                            mountList.add(curElement);
                        } else {
                            addElement(levelOrder, curElement);
                        }
                    }
                    break;
                }
                case SUFFIX: {
                    if (mountList.size() > 0) {
                        mountList.add(curElement);
                        isMount = true;
                    }
                    break;
                }
                case UNKNOW: {
                    // 有部分数据被误分到UNKOWN,例如，"广西"
                    if (!curElement.getLevel().equals(Level.UNKNOWN)) {
                        addElement(levelOrder, curElement);
                    } else {
                        mountList.add(curElement);
                    }
                    // 如果到了最后的元素
                    if (i == length - 1) {
                        levelOrder = getPreDiffElement(i, segElement).getLevel().ordinal();
                        isMount = true;
                    }
                }
            }

            // 挂载需要挂载的元素
            if (mountList.size() > 0 && isMount) {
                addElement(levelOrder, mountList);
                mountList.clear();
                isMount = false;
            }
        }
    }

    @Override
    public String toString() {
        return "VSM{" +
                "elements=" + Arrays.toString(elements) +
                '}';
    }
}
