package com.jhzhang.address.normalizer.test;

/**
 * 有限状态机实现中英文匹配
 *
 * @author jhZhang
 * @date 2018/3/28
 */
public class FSTNumberEN {
    final static int otherState = 1;
    final static int numberState = 2;
    final static int englishState = 3;
    final static char startChar = '0';
    // 状态转移表
    int next[][];

    public FSTNumberEN() {
        // 3个状态，127个字符
        this.next = new int[3][127];
        for (int i = (int) '0'; i <= (int) '9'; ++i) {
            setTrans(numberState, (char) i, numberState);
            setTrans(otherState, (char) i, numberState);
        }
        for (int i = (int) 'a'; i <= (int) 'z'; ++i) {
            setTrans(englishState, (char) i, englishState);
            setTrans(otherState, (char) i, englishState);
        }
        for (int i = (int) 'Z'; i <= (int) 'Z'; ++i) {
            setTrans(englishState, (char) i, englishState);
            setTrans(otherState, (char) i, englishState);
        }
    }

    public static void main(String[] args) {
        String text = "zhong232";
        FSTNumberEN fstNumberEN = new FSTNumberEN();
        int index = fstNumberEN.matchNumOrEn(text, 0);
        System.out.println(text.charAt(index));
        for (int i = (int) '0'; i < (int) '9'; ++i) {
            System.out.println((int) '0' + "\t" + i);
        }
    }

    /**
     * 设置状态转移函数
     *
     * @param s 初始状态
     * @param c 填入条件
     * @param t 目标状态
     */
    public void setTrans(int s, char c, int t) {
        next[s - 1][c - startChar] = t;
    }

    public int matchNumOrEn(String text, int offset) {
        int currState = otherState;
        int i = offset;
        while (i < text.length()) {
            char c = text.charAt(i);
            int pos = c - startChar;
            // 如果匹配到了非数字和英文等其它不包含在内的字符
            if (pos > next[0].length) {
                return i;
            }
            // 获得目标状态
            int nextState = next[currState - 1][pos];
            if (nextState == 0) {
                return i;
            }
            // 如果状态发生改变
            if (currState != nextState && i > offset) {
                return i;
            }
            i++;
            currState = nextState;
        }
        return i;
    }
}
