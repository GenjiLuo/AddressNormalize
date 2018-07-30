package com.jhzhang.address.normalizer.core.support;


/**
 * Element 在Vector中的位置是二维的，用这个对象包装这个二维位置信息.
 * Created by Administrator on 2016/9/1.
 */
public class Position {
    /**
     * Element的水平位置，即它对应的等级.
     */
    private final int horizon;
    /**
     * Element的垂直位置，即他在list中的位置.
     */
    private final int vertical;

    /**
     * 构造函数.
     *
     * @param horizon  水平位置
     * @param vertical 垂直位置
     */
    public Position(int horizon, int vertical) {
        this.horizon = horizon;
        this.vertical = vertical;
    }

    /**
     * get方法.
     *
     * @return Element的水平位置
     */
    public int getHorizon() {
        return horizon;
    }

    /**
     * get方法.
     *
     * @return Element的垂直位置
     */
    public int getVertical() {
        return vertical;
    }


    @Override
    public int hashCode() {
        return horizon * vertical;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Position) {
            Position other = (Position) obj;
            return horizon == other.horizon && vertical == other.vertical;

        }

        return false;
    }

    @Override
    public String toString() {
        return "Position{" +
                "horizon=" + horizon +
                ", vertical=" + vertical +
                '}';
    }
}
