package com.ripple.core.types.shamap;

import com.ripple.config.Config;
import com.ripple.core.coretypes.hash.Hash256;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.ripple.core.types.shamap.TestHelpers.H256;
import static com.ripple.core.types.shamap.TestHelpers.Leaf;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class ShaMapTest {
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
        ShaMapLeaf retrieved = sm.getLeaf(H256(index));
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

        sm.walkHashedTree(new HashedTreeWalker() {
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
    public void testInners() {
        // 775 and 776 are in the same inner node because they match on the first 7.
        // If you add 731,

        ShaMap sm = new ShaMap();
        sm.addLeaf(Leaf("775"));
        sm.addLeaf(Leaf("776"));

        NodeCount nodeCount = NodeCount.get(sm);
        assertEquals(nodeCount.leaves(), 2);
        // root + 7 + 7
        assertEquals(nodeCount.inners(), 3);
        assertEquals(sm.branchCount(), 1);
        assertEquals(sm.branches[7].asInner().branchCount(), 1);
        assertEquals(sm.branches[7].asInner().branches[7].asInner().branchCount(), 2);

        sm.addLeaf(Leaf("0345"));
        nodeCount.update();

        assertEquals(nodeCount.inners(), 3);
        assertEquals(nodeCount.leaves(), 3);

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

        sm.removeLeaf(H256("001"));
        assertTrue(sm.branch(0).isLeaf());
        for (int i = 1; i < 16; i++) sm.hasNone(i);
        assertEquals(sm.hash(), afterOne);

        sm.removeLeaf(H256("000"));
        assertEquals(sm.hash(), H256("0"));
    }

    @Test
    public void testAnEmptyInnerHasAZeroHash() {
        ShaMap sm = new ShaMap();
        assertEquals(sm.hash(), H256("0"));
    }

    @Test
    public void testCopyOnWrite() throws Exception {
        ShaMap sm = new ShaMap();
        assertEquals(sm.hash(), H256("0"));
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

        sm.removeLeaf(H256("01"));
        sm.removeLeaf(H256("02"));
        sm.removeLeaf(H256("023"));
        sm.removeLeaf(H256("024"));

        assertEquals(sm.hash(), Hash256.ZERO_256);
        removeLeafTestHelper(sm);

        copy2.invalidate();
        assertEquals(copy2.hash(), copy2Hash);
        assertEquals(copy1.hash(), Hash256.ZERO_256);
    }

    @Test
    public void testCopyOnWriteSemanticsUsing_getLeafForUpdating() throws Exception {
        ShaMap sm = new ShaMap();
        sm.addLeaf(Leaf("01"));
        sm.addLeaf(Leaf("02"));
        sm.addLeaf(Leaf("023"));
        copyOnWriteTestHelper(sm, Leaf("024"));
    }

    @Test
    public void testCopyOnWriteSemanticsUsing_getLeafForUpdating2() throws Exception {
        // Just the one leaf, which makes sure we copy leaves probably
        ShaMap sm = new ShaMap();
        copyOnWriteTestHelper(sm, Leaf("0"));
    }

    public void copyOnWriteTestHelper(ShaMap sm, ShaMapLeaf leaf) {
        sm.addLeaf(leaf);

        // At this point the shamap doesn't do any copy on write
        assertTrue(leaf == sm.getLeaf(leaf.index));
        // We can update the leaf in place
        assertTrue(leaf == sm.getLeafForUpdating(leaf.index));
        // It's still the same
        assertTrue(leaf == sm.getLeaf(leaf.index));

        // We make a copy, which means any changes to either
        // induce a copy on write
        ShaMap copy = sm.copy();

        // The leaf is still the same
        assertTrue(leaf == sm.getLeaf(leaf.index));
        // because we need to make sure we don't mess with our clones ;)
        assertTrue(leaf != sm.getLeafForUpdating(leaf.index));
        // now this has been updated via getLeafForUpdating
        // there is no `commit`
        assertTrue(leaf != sm.getLeaf(leaf.index));

        // And wow, we didn't mess with the original leaf in the copy ;)
        assertTrue(leaf == copy.getLeaf(leaf.index));
        // But now it's different after updating
        assertTrue(leaf != copy.getLeafForUpdating(leaf.index));

        // We haven't actually caused any substantive changes
        assertEquals(sm.hash(), copy.hash());
    }

    private static class NodeCount {
        private ShaMap sm;
        private AtomicInteger inners;
        private AtomicInteger leaves;

        public NodeCount(ShaMap sm) {
            this.sm = sm;
        }

        public static NodeCount get(ShaMap sm) {
            return new NodeCount(sm).invoke();
        }

        public int inners() {
            return inners.get();
        }

        public int leaves() {
            return leaves.get();
        }

        public NodeCount invoke() {
            inners = new AtomicInteger();
            leaves = new AtomicInteger();

            sm.walkHashedTree(new HashedTreeWalker() {
                @Override
                public void onLeaf(Hash256 h, ShaMapLeaf le) {
                    leaves.incrementAndGet();
                }

                @Override
                public void onInner(Hash256 h, ShaMapInner inner) {
                    inners.incrementAndGet();
                }
            });
            return this;
        }

        public NodeCount update() {
            this.invoke();
            return this;
        }
    }
}