package com.jhzhang.address.normalizer.prob.complete;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * 测试自己写的链表类方法
 */
public class LinkListTest {

    @Test
    public void size() throws Exception {
        LinkList<Integer> testLists = new LinkList<>();
        assertEquals(0, testLists.size());
        testLists.put(3);
        testLists.put(4);
        assertEquals(2, testLists.size());
    }

    @Test
    public void iterator() throws Exception {
        LinkList<Integer> testLists = new LinkList<>();
        assertNotNull(testLists.iterator());
        testLists.put(3);
        testLists.put(4);
        Iterator<Integer> it = testLists.iterator();
        assertTrue(it.hasNext());
        assertEquals((Object) 3, it.next());
        assertTrue(it.hasNext());
        assertEquals((Object) 4, it.next());
    }
}