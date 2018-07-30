package com.jhzhang.address.normalizer.cache.bean;

import com.jhzhang.address.normalizer.common.Level;

/**
 * 存放每个地名的地址信息.
 */
public class AddressBean {
    public static final AddressBean EMPTY = new AddressBean("", "", "", Level.UNKNOWN);

    private final String name;  //地名的简称
    private final String fullName;  //地名的别名
    private final String suffix;  //地名的地址后缀
    private final Level level;  //地名的等级

    /**
     * 构造函数中不带偏移量.
     *
     * @param name     简称
     * @param fullName 别名
     * @param suffix   地址后缀
     * @param level    地址等级
     */
    public AddressBean(String name, String fullName, String suffix, Level level) {
        this.name = name == null ? "" : name;
        this.fullName = fullName == null ? "" : fullName;
        this.suffix = suffix == null ? "" : suffix;
        this.level = level == null ? Level.UNKNOWN : level;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSuffix() {
        return suffix;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof AddressBean) {
            AddressBean other = (AddressBean) obj;
            return other.name.equals(this.name)
                    && other.suffix.equals(this.suffix)
                    && other.level.equals(this.level);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + suffix.hashCode() + level.hashCode();
    }

    @Override
    public String toString() {
        return name + " ( " + suffix + " ) " + fullName + "/" + level;
    }
}
