package com.ripple.core.types.shamap2;

import com.ripple.config.Config;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.Prefix;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class ShaMapTest extends TestCase {
    /*

    See the README.md related to shamaps for an overview

    */
    static {
        Config.initBouncy();
    }

    public static class Hash256Item extends ShaMapItem<Hash256> {
        Hash256 item;

        public Hash256Item(Hash256 item) {
            this.item = item;
        }

        @Override
        void toBytesSink(BytesSink sink) {
            item.toBytesSink(sink);
        }

        @Override
        void fromParser(BinaryParser parser) {
            item = Hash256.translate.fromParser(parser);
        }

        @Override
        void copyFrom(Hash256 other) {
            item = other;
        }

        @Override
        public Prefix hashPrefix() {
            return new Prefix() {
                @Override
                public byte[] bytes() {
                    return new byte[]{};
                }
            };
        }
    }

    public static Hash256 H(String hex) {
        hex = zeroPadAfterTo(hex, 64);
        return Hash256.fromHex(hex);
    }

    private static String zeroPadAfterTo(String hex, int i) {
        if (hex.length() == i) {
            return hex;
        }
        else if (hex.length() > i) {
            throw new AssertionError();
        }
        StringBuilder sb = new StringBuilder();

        for (int j = 0; j < i - hex.length(); j++) {
            sb.append('0');
        }
        String s = hex + sb.toString();
        if (s.length() != i) throw new AssertionError();
        return s;
    }

    public static ShaMapLeaf L(String hex) {
        Hash256Item hash256Item = new Hash256Item(H(hex));
        return new ShaMapLeaf(hash256Item.item, hash256Item);
    }

    @Test
    public void testAddLeaf() {
        // After adding this first
        ShaMap sm = new ShaMap();
        sm.addLeaf(L("000"));
        assertTrue(sm.branch(0).isLeaf());
        for (int i = 1; i < 16; i++) sm.hasNone(i);
    }

    @Test
    public void testGetLeaf() {
        ShaMap sm = new ShaMap();
        sm.addLeaf(L("000"));
        String index = "000123";
        ShaMapLeaf l = L(index);
        sm.addLeaf(l);
        ShaMapLeaf retrieved = sm.getLeaf(H(index));
        assertTrue(retrieved == l);
    }

    @Test
    public void testWalkHashedTree() {
        ShaMap sm = new ShaMap();
        sm.addLeaf(L("01"));
        sm.addLeaf(L("03"));
        sm.addLeaf(L("0345"));

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
        sm.addLeaf(L("000"));
        Hash256 afterOne = sm.hash();

        // Add a second down same path/index
        sm.addLeaf(L("001"));
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

        sm.removeLeaf(H("001"));
        assertTrue(sm.branch(0).isLeaf());
        for (int i = 1; i < 16; i++) sm.hasNone(i);
        assertEquals(sm.hash(), afterOne);

        sm.removeLeaf(H("000"));
        assertEquals(sm.hash(), H("0"));
    }

    @Test
    public void testAnEmptyInnerHasAZeroHash() {
        ShaMap sm = new ShaMap();
        assertEquals(sm.hash(), H("0"));
    }
}