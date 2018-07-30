package com.jhzhang.address.normalizer.common;

/**
 * 表示地址向量中的一个地址元素.
 *
 * @author Administrator
 */
public class Element {
    public static final Element EMPTY = new Element("", "", Level.UNKNOWN, -1, -1);

    /**
     * 元素对应的等级.
     */
    private final Level level;
    /**
     * 地址元素的地址后缀.
     */
    private final String suffix;
    /**
     * 地址元素在原始字符串中的偏移量.
     */
    private int start;
    /**
     * 记录地址片段在原始数据上的终止位置
     */
    private int end;
    /**
     * 地址元素内容.
     */
    private String name;

    /**
     * 地址元素构建器.
     *
     * @param name   地名
     * @param suffix 地名后缀，比如省、市
     * @param level  地址等级
     * @param start  地名在原始地址中的偏移量
     * @param end    地名在原始地址中结束位置
     */
    public Element(String name, String suffix, Level level, int start, int end) {
        this.name = name;
        this.level = level;
        this.suffix = suffix;
        this.start = start;
        this.end = end;
    }

    public Element(String name, String suffix, Level level) {
        this.level = level;
        this.suffix = suffix;
        this.name = name;
        this.start = this.end = -1;
    }

    public Level getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEnd() {
        return end;
    }

    public String getSuffix() {
        return suffix;
    }

    public int getStart() {
        return start;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Element) {
            Element other = (Element) obj;
            return other.name.equals(name) && other.level == level;
        }
        return false;
    }

    @Override
    public String toString() {
        return name + suffix;
    }
}
