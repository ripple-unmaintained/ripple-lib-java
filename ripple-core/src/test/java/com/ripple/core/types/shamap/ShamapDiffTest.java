package com.ripple.core.types.shamap;

import com.ripple.config.Config;
import com.ripple.core.coretypes.hash.Hash256;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static junit.framework.TestCase.assertEquals;

public class ShamapDiffTest {
    static {
        Config.initBouncy();
    }

    public interface Comparison {
        String[] A();
        String[] B();
        int[] modded_added_deleted();

        String[] modded();
        String[] added();
        String[] deleted();
    }

    Comparison one = new Comparison() {
        @Override
        public String[] A() {
            return new String[]{
              //"0000000000000000000000000000000000000000000000000000000000000000: A",
                "1000000000000000000000000000000000000000000000000000000000000000: A",
                "2000000000000000000000000000000000000000000000000000000000000000: A",
                "2100000000000000000000000000000000000000000000000000000000000000: A",
            };
        }
        @Override
        public String[] deleted() {
            return new String[] {"2000000000000000000000000000000000000000000000000000000000000000"};
        }

        @Override
        public String[] modded() {
            return new String[0];
        }

        @Override
        public String[] added() {
            return new String[0];
        }

        @Override
        public String[] B() {
            return new String[]{
              //"0000000000000000000000000000000000000000000000000000000000000000: A",
                "1000000000000000000000000000000000000000000000000000000000000000: A",
//                "2000000000000000000000000000000000000000000000000000000000000000: A",
                "2100000000000000000000000000000000000000000000000000000000000000: A",
            };
        }

        @Override
        public int[] modded_added_deleted() {
            return new int[]{0, 0, 1};
        }
    };

    Comparison two = new Comparison() {
        @Override
        public String[] A() {
            return new String[]{
              //"0000000000000000000000000000000000000000000000000000000000000000: A",
                "1000000000000000000000000000000000000000000000000000000000000000: A",
                "2000000000000000000000000000000000000000000000000000000000000000: A",
                "2100000000000000000000000000000000000000000000000000000000000000: A",
            };
        }
        @Override
        public String[] B() {
            return new String[]{
              //"0000000000000000000000000000000000000000000000000000000000000000: A",
                "1000000000000000000000000000000000000000000000000000000000000000: B", // <-- modified
                "2000000000000000000000000000000000000000000000000000000000000000: A",
                "2100000000000000000000000000000000000000000000000000000000000000: A",
                "2200000000000000000000000000000000000000000000000000000000000000: A", // <-- added
            };
        }

        @Override
        public int[] modded_added_deleted() {
            return new int[]{1, 1, 0};
        }

        @Override
        public String[] modded() {
            return new String[0];
        }

        @Override
        public String[] added() {
            return new String[0];
        }

        @Override
        public String[] deleted() {
            return new String[0];
        }
    };


    Comparison three = new Comparison() {
        @Override
        public String[] A() {
            return new String[]{

                "1000000000000000000000000000000000000000000000000000000000000000: A",
                "2000000000000000000000000000000000000000000000000000000000000000: A",
                "2100000000000000000000000000000000000000000000000000000000000000: A",
                "2111110000000000000000000000000000000000000000000000000000000000: A", // <-- deleted
                "2111120000000000000000000000000000000000000000000000000000000000: A", // <-- deleted
            };
        }
        @Override
        public String[] B() {
            return new String[]{

                "1000000000000000000000000000000000000000000000000000000000000000: B", // <-- modified
                "2000000000000000000000000000000000000000000000000000000000000000: A",
                "2100000000000000000000000000000000000000000000000000000000000000: A",

                "2200000000000000000000000000000000000000000000000000000000000000: A", // <-- added
            };
        }

        @Override
        public int[] modded_added_deleted() {
            return new int[]{1, 1, 2};
        }

        @Override
        public String[] modded() {
            return new String[0];
        }

        @Override
        public String[] added() {
            return new String[0];
        }

        @Override
        public String[] deleted() {
            return new String[0];
        }
    };

    @Test
    public void testDoDiff() throws Exception {
        testComparison(one);
        testComparison(two);
        testComparison(three);
    }

    private void testComparison(Comparison cmp) {
        ShaMap sa = buildShaMap(cmp.A());
        ShaMap sb = buildShaMap(cmp.B());
        ShamapDiff differ = new ShamapDiff(sa, sb);

        differ.find();
        differ.apply(sa);

        assertEquals(sa.hash().toHex(), sb.hash().toHex());

        int[] ints = cmp.modded_added_deleted();
        assertEquals(ints[0], differ.modified.size());
        assertEquals(ints[1], differ.added.size());
        assertEquals(ints[2], differ.deleted.size());
    }

    private ShaMap buildShaMap(String[] a) {
        ShaMap sm = new ShaMap();

        for (String s : a) {
            String[] split = s.split(":");
            Hash256 hash256 = Hash256.fromHex(split[0]);
            final byte[] bytes;
            try {
                bytes = split[1].getBytes("ascii");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            sm.addItem(hash256, ShaMapNode.NodeType.tnACCOUNT_STATE, new ShaMapLeaf.Item() {
                @Override
                public byte[] bytes() {
                    return bytes;
                }
            });
        }

        return sm;
    }
}