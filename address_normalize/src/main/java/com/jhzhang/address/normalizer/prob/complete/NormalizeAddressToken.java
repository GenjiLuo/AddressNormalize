package com.jhzhang.address.normalizer.prob.complete;

import com.jhzhang.address.normalizer.AddressTagger;
import com.jhzhang.address.normalizer.Normalizer;
import com.jhzhang.address.normalizer.cache.AddressTokenMatcher;
import com.jhzhang.address.normalizer.cache.exception.InvalidAddressException;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;
import com.jhzhang.address.normalizer.prob.task.*;
import com.jhzhang.address.normalizer.cache.AddressTokenMatcher;
import com.jhzhang.address.normalizer.cache.exception.InvalidAddressException;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.jhzhang.address.normalizer.prob.common.Conf.MAX_ADDRESS_LEVEL;
import static com.jhzhang.address.normalizer.prob.common.Conf.MAX_NORMALIZE_LEVEL;

/**
 * 构建一个图结构用来表示不同等级的地址
 *
 * @author jhZhang
 * @date 2018/6/12
 */
public class NormalizeAddressToken {
    AddressTokenMatcher query = null;
    private LinkList<AddressToken>[] addrLevels = null;

    NormalizeAddressToken() {
        addrLevels = new LinkList[MAX_ADDRESS_LEVEL];
        for (int i = 0; i < MAX_ADDRESS_LEVEL; i++) {
            addrLevels[i] = new LinkList();
        }
        query = AddressTokenMatcher.getInstance();
    }

    public static void main(String[] args) throws Exception {
//        String address = "浙江省杭州市上城区小营街道小营社区上马街34号梅林二村503室";
//        String address = "浙江省杭州市文一西路87号5楼537";
//        String address = "上海市长宁区虹桥街道虹梅路3818号6号402";
        String address = "上海市闵行区古美街道上海市闵行区莲花路755弄19号902室";
        String parentAddressXml = "E:\\software\\SVN\\address_normalizer_full\\address_normalizer_full\\address_normalizer_config\\src\\main\\resources\\";
        String suffixLevelXml = "addressSuffixLevelData.xml";
        Normalizer normalizer = new Normalizer(parentAddressXml + "address.xml", parentAddressXml + suffixLevelXml);

        System.out.println(normalizer.normalizeAsStr(address));
    }

    public static NormalizeAddressToken valueOf(String address) {
        ArrayList<AddressToken> ret = AddressTagger.tag(address);
        List<AddressToken> rets = ret.subList(1, ret.size() - 1);
        NormalizeAddressToken addressToken = new NormalizeAddressToken();
        new LevelABCMountTask().task(addressToken, rets);
        new LevelDMountTask().task(addressToken, rets);
        new LevelEMountTask().task(addressToken, rets);
        new LevelFGMountTask().task(addressToken, rets);
        new LevelHMountTask().task(addressToken, rets);
        new LevelJKLMMountTask().task(addressToken, rets);
        return addressToken;
    }

    /**
     * 按照AC自动机中的数据结构补全缺失的地址路径，元素等级之间的调整放在了规则配置中
     */
    public void fixABCDELevel() throws InvalidAddressException {
        AdjList<AddressNode> nodes = new AdjList(5);
        // 1. 取前面五级，获取所有对应等级的地址元素
        // 行政区划有值统计
        int countValue = 0;
        for (int i = 0; i < MAX_NORMALIZE_LEVEL; i++) {
            Iterator it = addrLevels[i].iterator();
            while (it.hasNext()) {
                AddressToken token = (AddressToken) it.next();
                List<AddressNode> levelNodes = query.keywordsMatch(token.termText, Level.values()[i]);
                // 剔除掉“新村、社区”这类普遍词
                if (levelNodes.size() > 0 && levelNodes.size() < 5) {
                    countValue++;
                    nodes.putAll(levelNodes, i);
                }
            }
        }
        if (countValue <= 0) {
            throw new InvalidAddressException("输入的地址缺失行政数据");
        }

        // 2. 获取所有可能存在的路径
        ArrayList<LinkList<AddressNode>> addrNodeList = new ArrayList<>();
        for (int i = MAX_NORMALIZE_LEVEL - 1; i >= 0; i--) {
            for (Iterator<AddressNode> it = nodes.getIter(i); it.hasNext(); ) {
                // 获取子结点
                AddressNode cNode = it.next();
                LinkList<AddressNode> stacks = new LinkList<>();
                stacks.put(cNode);
                // 往上查找父结点
                for (int j = i - 1; j >= 0; j--) {
                    for (Iterator<AddressNode> preIt = nodes.getIter(j); preIt.hasNext(); ) {
                        AddressNode pNode = preIt.next();
                        if (AddressTokenMatcher.isSameTrie(pNode, cNode)) {
                            stacks.put(pNode);
                            break;
                        }
                    }
                }
                addrNodeList.add(stacks);
            }
        }
        // 3. 计算每个列表的得分获得最靠谱的数据--直接以最大的个数为依据
        // 记录最大得分
        int max = 0;
        // 记录重复数据最大得分
        int replaceScore = -1;
        LinkList<AddressNode> candidate = null;
        for (LinkList<AddressNode> addressNodes : addrNodeList) {
            int score = addressNodes.size();
            if (max < score) {
                max = score;
                candidate = addressNodes;
            } else if (max == score) {
                replaceScore = max;
            }
        }
//        if (replaceScore == max) {
//            throw new InvalidAddressException("03 the administration contain same address score");
//        }

        // 4. 将查找到数据补全放入到元素数据中
        AddressNode head = candidate.getHead().item;
        // 4.1 获得AC自动机中的所有AddressNode元素
        LinkList<AddressNode> acLinkList = new LinkList<>();
        AddressNode cNode = head;
        while (true) {
            acLinkList.put(cNode);
            if (cNode.getLevel().equals(Level.A)) {
                break;
            }
            AddressNode pNode = cNode.getParentAddressNode();
            cNode = pNode;
        }
        // 4.2 将获得的列表转成AddressToken存入原始数据中
        for (AddressNode addressNode : acLinkList) {
            int index = addressNode.getLevel().ordinal();
            addrLevels[index].reset();
            AddressToken token = new AddressToken(0, 0, addressNode.getKeyString() + addressNode.getSuffix(), AddressType.Unknow);
            addrLevels[index].put(token);
        }
    }

    /**
     * 移除某一级别的所有元素
     *
     * @param index
     */
    public void reset(int index) {
        if (index < 0 || index > MAX_ADDRESS_LEVEL) {
            throw new IndexOutOfBoundsException("超出了插入的界限");
        }
        addrLevels[index].reset();
    }


    public void mount(AddressToken token, Level level) {
        addrLevels[level.ordinal()].put(token);
    }

    /**
     * 将等级地址划分成链表
     *
     * @return
     */
    public List<String> asList() {
        List<String> normalizes = new ArrayList<>();
        for (LinkList<AddressToken> token : addrLevels) {
            Node<AddressToken> head = token.getHead();
            if (head != null) {
                normalizes.add(head.item.termText);
            } else {
                normalizes.add(null);
            }
        }
        return normalizes;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 0;
        while (true) {
            if (addrLevels[i].size() > 0) {
                sb.append(Level.values()[i]).append("|");
                sb.append(addrLevels[i].toString());
            }
            i++;
            if (i < MAX_ADDRESS_LEVEL - 1) {
                sb.append(",");
            } else {
                break;
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
