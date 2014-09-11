package com.ripple.core.types.shamap2;

import com.ripple.config.Config;
import com.ripple.core.coretypes.hash.Hash256;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.ripple.core.types.shamap2.TestHelpers.Pad256;
import static com.ripple.core.types.shamap2.TestHelpers.Leaf;

public class ShaMapTest extends TestCase {
    /*

    See the README.md related to shamaps for an overview

    */
    static {
        Config.initBouncy();
    }

    @Test
    public void testAddLeaf() {
        // After adding this first
        ShaMap sm = new ShaMap();
        sm.addLeaf(Leaf("000"));
        assertTrue(sm.branch(0).isLeaf());
        for (int i = 1; i < 16; i++) sm.hasNone(i);
    }

    @Test
    public void testGetLeaf() {
        ShaMap sm = new ShaMap();
        sm.addLeaf(Leaf("000"));
        String index = "000123";
        ShaMapLeaf l = Leaf(index);
        sm.addLeaf(l);
        ShaMapLeaf retrieved = sm.getLeaf(Pad256(index));
        assertTrue(retrieved == l);
    }

    @Test
    public void testWalkHashedTree() {
        ShaMap sm = new ShaMap();
        sm.addLeaf(Leaf("01"));
        sm.addLeaf(Leaf("03"));
        sm.addLeaf(Leaf("0345"));

        final AtomicInteger inners = new AtomicInteger();
        final AtomicInteger leaves = new AtomicInteger();

        sm.walkHashedTree(new ShaMapInner.HashedTreeWalker() {
            @Override
            public void onLeaf(Hash256 h, ShaMapLeaf le) {
                leaves.incrementAndGet();
            }

            @Override
            public void onInner(Hash256 h, ShaMapInner inner) {
                inners.incrementAndGet();
            }
        });

        assertEquals(leaves.get(), 3);
        assertEquals(inners.get(), 3);
    }

    @Test
    public void testRemoveLeaf() {
        ShaMap sm = new ShaMap();
        // Add one leaf
        removeLeafTestHelper(sm);
    }

    public void removeLeafTestHelper(ShaMap sm) {
        sm.addLeaf(Leaf("000"));
        Hash256 afterOne = sm.hash();

        // Add a second down same path/index
        sm.addLeaf(Leaf("001"));
        for (int i = 1; i < 16; i++) sm.hasNone(i);

        // Where before this was a leaf, now it's an inner,
        // cause of common path/prefix in index
        assertTrue(sm.branch(0).isInner());
        // The common prefix `00` leads to an inner branch
        assertTrue(sm.branch(0).asInner().branch(0).isInner());

        // The children of `00`
        ShaMapInner common = sm.branch(0).asInner().branch(0).asInner();

        assertTrue(common.branch(0).isLeaf());
        assertTrue(common.branch(1).isLeaf());
        for (int i = 2; i < 16; i++) common.hasNone(i);

        sm.removeLeaf(Pad256("001"));
        assertTrue(sm.branch(0).isLeaf());
        for (int i = 1; i < 16; i++) sm.hasNone(i);
        assertEquals(sm.hash(), afterOne);

        sm.removeLeaf(Pad256("000"));
        assertEquals(sm.hash(), Pad256("0"));
    }

    @Test
    public void testAnEmptyInnerHasAZeroHash() {
        ShaMap sm = new ShaMap();
        assertEquals(sm.hash(), Pad256("0"));
    }

    @Test
    public void testCopyOnWrite() throws Exception {
        ShaMap sm = new ShaMap();
        assertEquals(sm.hash(), Pad256("0"));
        ShaMap copy1 = sm.copy();
        removeLeafTestHelper(copy1);
        sm.addLeaf(Leaf("01"));
        sm.addLeaf(Leaf("02"));
        sm.addLeaf(Leaf("023"));
        sm.addLeaf(Leaf("024"));
        assertEquals(copy1.hash(), Hash256.ZERO_256);

        ShaMap copy2 = sm.copy();
        Hash256 copy2Hash = copy2.hash();
        assertEquals(copy2Hash, sm.hash());

        sm.removeLeaf(Pad256("01"));
        sm.removeLeaf(Pad256("02"));
        sm.removeLeaf(Pad256("023"));
        sm.removeLeaf(Pad256("024"));

        assertEquals(sm.hash(), Hash256.ZERO_256);
        removeLeafTestHelper(sm);

        copy2.invalidate();
        assertEquals(copy2.hash(), copy2Hash);
    }
}