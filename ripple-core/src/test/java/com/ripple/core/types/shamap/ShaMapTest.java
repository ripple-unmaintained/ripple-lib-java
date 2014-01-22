
package com.ripple.core.types.shamap;

import static com.ripple.core.types.shamap.ShaMapNode.NodeType.tnTRANSACTION_MD;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.util.*;

import com.ripple.core.coretypes.Vector256;
import org.json.JSONArray;
import org.junit.Test;
import org.ripple.bouncycastle.util.encoders.Hex;

import com.ripple.config.Config;
import com.ripple.core.serialized.BinarySerializer;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.encodings.common.B16;

public class ShaMapTest {
    static {
        Config.initBouncy();
    }

    @Test
    public void testNibblet() throws Exception {
        String ledgerIndex = "D66D0EC951FD5707633BEBE74DB18B6D2DDA6771BA0FBF079AD08BFDE6066056";
        Hash256 index = hash(ledgerIndex);

        for (int i = 0; i < ledgerIndex.length(); i++) {
            int n1 = index.nibblet(i);
            String s = Integer.toHexString(n1).toUpperCase();
            assertEquals(ledgerIndex.substring(i, i + 1), s);
        }
    }

    @Test
    public void testArrayAssumptions() throws Exception {
        String[] arr = new String[16];
        assertEquals(null, arr[0]);
        assertEquals(null, arr[15]);

        ShaMapNode[] nodes = new ShaMapNode[16];
        ShaMapLeafNode.Item item = createItem(ShaMapInnerNode.ZERO_256);

        ShaMapLeafNode leaf = new ShaMapLeafNode(ShaMapInnerNode.ZERO_256, tnTRANSACTION_MD, item);
        nodes[1] = leaf;
    }

    @Test
    public void testEndianIssues() throws Exception {
    }

    @Test
    public void donTestLeafNodeHashing() throws Exception {
        // Note that this is node starting with nibble `2` below
        byte[] tag = Hex.decode("A197ECCF23E55193CBE292F7A373F0DE0F521D4DCAE32484E20EC634C1ACE528");
        final byte[] node = Hex
                .decode("9E12000822000000002400113FCF201900113F3268400000000000000A73210256C64F0378DCCCB4E0224B36F7ED1E5586455FF105F760245ADB35A8B03A25FD7447304502200A8BED7B8955F45633BA4E9212CE386C397E32ACFF6ECE08EB74B5C86200C606022100EF62131FF50B288244D9AB6B3D18BACD44924D2BAEEF55E1B3232B7E033A27918114E0E893E991B2142E74486F7D3331CF711EA84213C304201C00000001F8E511006125003136FA55610A3178D0A69167DF32E28990FD60D50F5610A5CF5C832CBF0C7FCC0913516B5656091AD066271ED03B106812AD376D48F126803665E3ECBFDBBB7A3FFEB474B2E62400113FCF2D000000456240000000768913E4E1E722000000002400113FD02D000000446240000000768913DA8114E0E893E991B2142E74486F7D3331CF711EA84213E1E1E5110064565943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919AE7220000000036561993D688DA919A585943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919A01110000000000000000000000004C54430000000000021192D705968936C419CE614BF264B5EEB1CEA47FF403110000000000000000000000004254430000000000041192D705968936C419CE614BF264B5EEB1CEA47FF4E1E1E411006F5678812E6E2AB80D5F291F8033D7BC23F0A6E4EA80C998BFF38E80E2A09D2C4D93E722000000002400113F32250031361633000000000000000034000000000000329255C7D1671589B1B4AB1071E38299B8338632DAD19A7D0F8D28388F40845AF0BCC550105943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919A64D4C7A75562493C000000000000000000000000004C5443000000000092D705968936C419CE614BF264B5EEB1CEA47FF465D44AA183A77ECF80000000000000000000000000425443000000000092D705968936C419CE614BF264B5EEB1CEA47FF48114E0E893E991B2142E74486F7D3331CF711EA84213E1E1E511006456F78A0FFA69890F27C2A79C495E1CEB187EE8E677E3FDFA5AD0B8FCFC6E644E38E72200000000310000000000003293320000000000000000582114A41BB356843CE99B2858892C8F1FEF634B09F09AF2EB3E8C9AA7FD0E3A1A8214E0E893E991B2142E74486F7D3331CF711EA84213E1E1F1031000");
        ShaMapLeafNode shaMapLeafNode = new ShaMapLeafNode(new Hash256(tag), tnTRANSACTION_MD,
                new ShaMapLeafNode.Item() {
                    @Override
                    public byte[] bytes() {
                        return node;
                    }
                });
    }

    @Test
    public void testShaMapDeepNodes() {
        Hash256 // 0 64

                id1 = hash("0000000000000000000000000000000000000000000000000000000000000000"), id2 = hash("1000000000000000000000000000000000000000000000000000000000000000"), id3 = hash("2100000000000000000000000000000000000000000000000000000000000000"), id4 = hash("2110000000000000000000000000000000000000000000000000000000000000"), id5 = hash("2120000000000000000000000000000000000000000000000000000000000000"), id6 = hash("3000000000000000000000000000000000000000000000000000000000000000");

        ensureUnique(id1, id2, id3, id4, id5, id6);

        ShaMapLeafNode.Item i1 = createItem(id1), i2 = createItem(id1), i3 = createItem(id1), i4 = createItem(id1), i5 = createItem(id1), i6 = createItem(id1);

        ShaMap map = new ShaMap();
        map.addLeaf(id1, tnTRANSACTION_MD, i1);
        map.addLeaf(id2, tnTRANSACTION_MD, i2);
        map.addLeaf(id3, tnTRANSACTION_MD, i3);
        map.addLeaf(id4, tnTRANSACTION_MD, i4);
        map.addLeaf(id5, tnTRANSACTION_MD, i5);
        map.addLeaf(id6, tnTRANSACTION_MD, i6);

        // Test leaves
        assertTrue(map.branches[0] instanceof ShaMapLeafNode);

        assertTrue(map.branches[1] instanceof ShaMapLeafNode);

        assertTrue(map.branches[3] instanceof ShaMapLeafNode);

        assertTrue(map.branches[1] instanceof ShaMapLeafNode);

        assertTrue(map.branches[2] instanceof ShaMapInnerNode);

        assertTrue(((ShaMapInnerNode) map.branches[2]).branches[1] instanceof ShaMapInnerNode);
        assertTrue(map.branches[0] instanceof ShaMapLeafNode);

    }

    private void ensureUnique(Hash256... hashes) {
        HashSet<String> s = new HashSet<String>();
        int n = 0;

        for (Hash256 hash : hashes) {
            n += 1;
            assertTrue("The " + n + "th hash is a duplicate", s.add(hash.toString()));
        }
    }

    private ShaMapLeafNode.Item createItem(final Hash256 id1) {
        return new ShaMapLeafNode.Item() {
            @Override
            public byte[] bytes() {
                return id1.bytes();
            }
        };
    }

    @Test
    public void testLedgerHashing() throws Exception {

        Hash256 tx1_hash = hash("232E91912789EA1419679A4AA920C22CFC7C6B601751D6CBE89898C26D7F4394");
        byte[] tx1 = Hex
                .decode("120007220000000024000195F964400000170A53AC2065D5460561EC9DE000000000000000000000000000494C53000000000092D705968936C419CE614BF264B5EEB1CEA47FF468400000000000000A7321028472865AF4CB32AA285834B57576B7290AA8C31B459047DB27E16F418D6A71667447304502202ABE08D5E78D1E74A4C18F2714F64E87B8BD57444AFA5733109EB3C077077520022100DB335EE97386E4C0591CAC024D50E9230D8F171EEB901B5E5E4BD6D1E0AEF98C811439408A69F0895E62149CFCC006FB89FA7D1E6E5D");
        byte[] tx1Meta = Hex
                .decode("201C00000000F8E311006F563596CE72C902BAFAAB56CC486ACAF9B4AFC67CF7CADBB81A4AA9CBDC8C5CB1AAE824000195F934000000000000000E501062A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F00064400000170A53AC2065D5460561EC9DE000000000000000000000000000494C53000000000092D705968936C419CE614BF264B5EEB1CEA47FF4811439408A69F0895E62149CFCC006FB89FA7D1E6E5DE1E1E31100645662A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000E8365C14BE8A20D7F0005862A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F0000311000000000000000000000000494C530000000000041192D705968936C419CE614BF264B5EEB1CEA47FF4E1E1E511006456AB03F8AA02FFA4635E7CE2850416AEC5542910A2B4DBE93C318FEB08375E0DB5E7220000000032000000000000000058801C5AFB5862D4666D0DF8E5BE1385DC9B421ED09A4269542A07BC0267584B64821439408A69F0895E62149CFCC006FB89FA7D1E6E5DE1E1E511006125003136FA55DE15F43F4A73C4F6CB1C334D9E47BDE84467C0902796BB81D4924885D1C11E6D56CF23A37E39A571A0F22EC3E97EB0169936B520C3088963F16C5EE4AC59130B1BE624000195F92D000000086240000018E16CCA08E1E7220000000024000195FA2D000000096240000018E16CC9FE811439408A69F0895E62149CFCC006FB89FA7D1E6E5DE1E1F1031000");

        Hash256 tx2_hash = hash("A197ECCF23E55193CBE292F7A373F0DE0F521D4DCAE32484E20EC634C1ACE528");
        byte[] tx2 = Hex
                .decode("12000822000000002400113FCF201900113F3268400000000000000A73210256C64F0378DCCCB4E0224B36F7ED1E5586455FF105F760245ADB35A8B03A25FD7447304502200A8BED7B8955F45633BA4E9212CE386C397E32ACFF6ECE08EB74B5C86200C606022100EF62131FF50B288244D9AB6B3D18BACD44924D2BAEEF55E1B3232B7E033A27918114E0E893E991B2142E74486F7D3331CF711EA84213");
        byte[] tx2Meta = Hex
                .decode("201C00000001F8E511006125003136FA55610A3178D0A69167DF32E28990FD60D50F5610A5CF5C832CBF0C7FCC0913516B5656091AD066271ED03B106812AD376D48F126803665E3ECBFDBBB7A3FFEB474B2E62400113FCF2D000000456240000000768913E4E1E722000000002400113FD02D000000446240000000768913DA8114E0E893E991B2142E74486F7D3331CF711EA84213E1E1E5110064565943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919AE7220000000036561993D688DA919A585943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919A01110000000000000000000000004C54430000000000021192D705968936C419CE614BF264B5EEB1CEA47FF403110000000000000000000000004254430000000000041192D705968936C419CE614BF264B5EEB1CEA47FF4E1E1E411006F5678812E6E2AB80D5F291F8033D7BC23F0A6E4EA80C998BFF38E80E2A09D2C4D93E722000000002400113F32250031361633000000000000000034000000000000329255C7D1671589B1B4AB1071E38299B8338632DAD19A7D0F8D28388F40845AF0BCC550105943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919A64D4C7A75562493C000000000000000000000000004C5443000000000092D705968936C419CE614BF264B5EEB1CEA47FF465D44AA183A77ECF80000000000000000000000000425443000000000092D705968936C419CE614BF264B5EEB1CEA47FF48114E0E893E991B2142E74486F7D3331CF711EA84213E1E1E511006456F78A0FFA69890F27C2A79C495E1CEB187EE8E677E3FDFA5AD0B8FCFC6E644E38E72200000000310000000000003293320000000000000000582114A41BB356843CE99B2858892C8F1FEF634B09F09AF2EB3E8C9AA7FD0E3A1A8214E0E893E991B2142E74486F7D3331CF711EA84213E1E1F1031000");

        ShaMapLeafNode.Item n1 = createItem(tx1, tx1Meta);
        ShaMapLeafNode.Item n2 = createItem(tx2, tx2Meta);

        String node = "C111120007220000000024000195F964400000170A53AC2065D5460561EC9DE000000000000000000000000000494C53000000000092D705968936C419CE614BF264B5EEB1CEA47FF468400000000000000A7321028472865AF4CB32AA285834B57576B7290AA8C31B459047DB27E16F418D6A71667447304502202ABE08D5E78D1E74A4C18F2714F64E87B8BD57444AFA5733109EB3C077077520022100DB335EE97386E4C0591CAC024D50E9230D8F171EEB901B5E5E4BD6D1E0AEF98C811439408A69F0895E62149CFCC006FB89FA7D1E6E5DC26E201C00000000F8E311006F563596CE72C902BAFAAB56CC486ACAF9B4AFC67CF7CADBB81A4AA9CBDC8C5CB1AAE824000195F934000000000000000E501062A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F00064400000170A53AC2065D5460561EC9DE000000000000000000000000000494C53000000000092D705968936C419CE614BF264B5EEB1CEA47FF4811439408A69F0895E62149CFCC006FB89FA7D1E6E5DE1E1E31100645662A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000E8365C14BE8A20D7F0005862A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F0000311000000000000000000000000494C530000000000041192D705968936C419CE614BF264B5EEB1CEA47FF4E1E1E511006456AB03F8AA02FFA4635E7CE2850416AEC5542910A2B4DBE93C318FEB08375E0DB5E7220000000032000000000000000058801C5AFB5862D4666D0DF8E5BE1385DC9B421ED09A4269542A07BC0267584B64821439408A69F0895E62149CFCC006FB89FA7D1E6E5DE1E1E511006125003136FA55DE15F43F4A73C4F6CB1C334D9E47BDE84467C0902796BB81D4924885D1C11E6D56CF23A37E39A571A0F22EC3E97EB0169936B520C3088963F16C5EE4AC59130B1BE624000195F92D000000086240000018E16CCA08E1E7220000000024000195FA2D000000096240000018E16CC9FE811439408A69F0895E62149CFCC006FB89FA7D1E6E5DE1E1F1031000";
        String node2 = "9E12000822000000002400113FCF201900113F3268400000000000000A73210256C64F0378DCCCB4E0224B36F7ED1E5586455FF105F760245ADB35A8B03A25FD7447304502200A8BED7B8955F45633BA4E9212CE386C397E32ACFF6ECE08EB74B5C86200C606022100EF62131FF50B288244D9AB6B3D18BACD44924D2BAEEF55E1B3232B7E033A27918114E0E893E991B2142E74486F7D3331CF711EA84213C304201C00000001F8E511006125003136FA55610A3178D0A69167DF32E28990FD60D50F5610A5CF5C832CBF0C7FCC0913516B5656091AD066271ED03B106812AD376D48F126803665E3ECBFDBBB7A3FFEB474B2E62400113FCF2D000000456240000000768913E4E1E722000000002400113FD02D000000446240000000768913DA8114E0E893E991B2142E74486F7D3331CF711EA84213E1E1E5110064565943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919AE7220000000036561993D688DA919A585943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919A01110000000000000000000000004C54430000000000021192D705968936C419CE614BF264B5EEB1CEA47FF403110000000000000000000000004254430000000000041192D705968936C419CE614BF264B5EEB1CEA47FF4E1E1E411006F5678812E6E2AB80D5F291F8033D7BC23F0A6E4EA80C998BFF38E80E2A09D2C4D93E722000000002400113F32250031361633000000000000000034000000000000329255C7D1671589B1B4AB1071E38299B8338632DAD19A7D0F8D28388F40845AF0BCC550105943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919A64D4C7A75562493C000000000000000000000000004C5443000000000092D705968936C419CE614BF264B5EEB1CEA47FF465D44AA183A77ECF80000000000000000000000000425443000000000092D705968936C419CE614BF264B5EEB1CEA47FF48114E0E893E991B2142E74486F7D3331CF711EA84213E1E1E511006456F78A0FFA69890F27C2A79C495E1CEB187EE8E677E3FDFA5AD0B8FCFC6E644E38E72200000000310000000000003293320000000000000000582114A41BB356843CE99B2858892C8F1FEF634B09F09AF2EB3E8C9AA7FD0E3A1A8214E0E893E991B2142E74486F7D3331CF711EA84213E1E1F1031000";

        assertEquals(node, B16.toString(n1.bytes()).toUpperCase());
        assertEquals(node2, B16.toString(n2.bytes()).toUpperCase());

        ShaMap ledger = new ShaMap();
        ledger.addLeaf(tx1_hash, tnTRANSACTION_MD, n1);
        ledger.addLeaf(tx2_hash, tnTRANSACTION_MD, n2);

        String tnh = "7597469704639256442E505C2291DEDF8AEC835C974BC98545D490F462343178";
        Hash256 transaction_hash = hash(tnh);

        assertTrue(transaction_hash.equals(ledger.hash()));
    }

    private ShaMapLeafNode.Item createItem(byte[] tx, byte[] meta) {
        BinarySerializer s = new BinarySerializer();
        s.addLengthEncoded(tx);
        s.addLengthEncoded(meta);
        final byte[] bytes = s.bytes();
        return new ShaMapLeafNode.Item() {
            @Override
            public byte[] bytes() {
                return bytes;
            }
        };
    }

    private Hash256 hash(String tnh) {
        return Hash256.translate.fromString(tnh);
    }


    String s = "71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D";
    byte[] prefix = B16.decode(s);

    Random randomer = new Random();
    long hashes = 0;
    public boolean CREATE_BOOKBASES = true;
    public int BOOK_EVERY = 5;
    public int NUM_PREFIXES = 25;

    ArrayList<byte[]> prefixes = new ArrayList<byte[]>();
    {
        for (int i = 0; i < NUM_PREFIXES; i++) {
            byte[] b = new byte[24];
            b[0] |= ((i % 16) << 4); // A little trick to ensure we hit every top level inner node children of root
            randomer.nextBytes(b);
            prefixes.add(b);
        }
    }

    private Hash256 randomHash() {
        byte[] b;

        b = new byte[32];
        randomer.nextBytes(b);

//        Hash256.HalfSha512 halfSha512 = new Hash256.HalfSha512();
//        halfSha512.update(b);
        return new Hash256(b);
    }

    private Hash256 prefixed32() {
        byte[] prefix1 = randomPrefix();

        byte[] b;
        b = new byte[8];
        randomer.nextBytes(b);

        byte[] f = new byte[32];

        System.arraycopy(prefix1, 0, f, 0, prefix1.length);
        System.arraycopy(b, 0, f, prefix1.length, b.length);
        b = f;
        return new Hash256(b);
    }

    ShaMapInnerNode.Counter<Integer> prefixIndexes = new ShaMapInnerNode.Counter<Integer>();

    private byte[] randomPrefix() {
//        prefixes.get()
        /*int randomKeyIndex = randomer.nextInt(whichRandom.size() - 1);
                    Hash256 keyToUpdate = whichRandom.get(randomKeyIndex);*/
        int randomIndex = randomer.nextInt(prefixes.size() - 1);
        prefixIndexes.count(randomIndex);
        return prefixes.get(randomIndex);
    }

//    @Test
    public void testWatFace() throws Exception {
        ShaMap ledger = new ShaMap();
        int numLedgers = 10000;

        int newNodesPerLedger = 25;
        int modifiedNodesPerLedger = 100;
        ArrayList<Hash256> randomNodes = new ArrayList<Hash256>();
        ArrayList<Hash256> bookBases = new ArrayList<Hash256>();

        int totalLeavesAdded = 0;
        int totalLeavesAddedOrModified = 0;
        int ledgersDone = 0;
        double pta;

        for (int i = 0; i < numLedgers; i++) {
            pta = now();
            int n = 0;
            while (n++ < newNodesPerLedger) {
                // she gonna blow up if she is not unique anyway
                Hash256 key;
                if (bookKey()) {
                    key = prefixed32();
                    bookBases.add(key);
                } else {
                    key = randomHash();
                    randomNodes.add(key);
                }

                addKeyAsValueItem(ledger, key);
                totalLeavesAdded++;
            }
            double nsForAdding = now() - pta;

            pta = now();
            if (ledgersDone > 1 && modifiedNodesPerLedger <= randomNodes.size()) {
                for (int j = 0; j < modifiedNodesPerLedger; j++) {
                    ArrayList<Hash256> whichRandom;
                    if (bookKey()) {
                        whichRandom = bookBases;
                    } else {
                        whichRandom = randomNodes;
                    }
                    int randomKeyIndex = randomer.nextInt(whichRandom.size() - 1);
                    Hash256 keyToUpdate = whichRandom.get(randomKeyIndex);
                    // invalidating ;)
                    ShaMapLeafNode leaf = ledger.getLeafForUpdating(keyToUpdate);
                    // so the hash is actually different ;0
                    leaf.blob = createItem(randomHash());
                }
            }
            double nsForModding = now() - pta;

            totalLeavesAddedOrModified += modifiedNodesPerLedger + newNodesPerLedger;

//            ledger.compressTree();
            pta = now();
            ledger.hash();
//            ledger.uncompressTree();

            double nsForHashing = now() - pta;

            ledgersDone++;

            if (ledgersDone % 25 == 0) {
                double uniqueInnerNodes = ledger.totalUniqueInnerNodesOverTime();
                long l1 = ledger.unusedBranchesOverTime();
                double ratioUnusedToUsedInInnerNodesOverTime = l1 / (uniqueInnerNodes * 16);

                long totalInnerNodes = ledger.totalInnerNodes();
                long totalSlots = ledger.totalSlots();
                long unusedSlots = ledger.unusedSlots();
                long usedSlots = totalSlots - ledger.unusedSlots();

                System.out.println("Ledgers passed: " + ledgersDone);
                System.out.println("Total leaf nodes in latest ledger: " + totalLeavesAdded);
                System.out.println("Leaf nodes added per ledger: " + newNodesPerLedger);
                System.out.println("Leaf nodes updated per ledger: " + modifiedNodesPerLedger);
                System.out.println("Ms to add " + newNodesPerLedger + " nodes to ledger: " + nsForAdding / 1e6);
                System.out.println("Ms to update " + modifiedNodesPerLedger + " nodes to ledger: " + nsForModding / 1e6);
                System.out.println("Ms to hash ledger: " + nsForHashing / 1e6);
                System.out.println("Total inner nodes in ledger: " + totalInnerNodes);
                System.out.println("Total unique inner nodes over all ledgers: " + uniqueInnerNodes);
                System.out.println("Total branches in ledger: " + totalSlots);
                System.out.println("Total unused branches in ledger: " + unusedSlots);
                System.out.println("Total unique leaf instances: " + totalLeavesAddedOrModified);
                System.out.println("Unique inner nodes over all ledgers: " + uniqueInnerNodes);
                System.out.println("Ratio of unused/used branches in inner nodes over all ledgers: " + ratioUnusedToUsedInInnerNodesOverTime);
                System.out.println("Ratio of unused/used branches in ledger: " + ((double) unusedSlots) / usedSlots);
                System.out.println("Unique inner nodes per total leaf instances in node store: " + uniqueInnerNodes / totalLeavesAddedOrModified);
                System.out.println("Unique inner nodes per total leaf nodes in ledger: " + uniqueInnerNodes / totalLeavesAdded);
                System.out.println();
            }
        }

        System.out.println("Random   keys used: " + randomNodes.size());
        System.out.println("Prefixed keys used: " + bookBases.size());

        System.out.println("Prefix Histogram");
        for (Map.Entry<Integer, Integer> count : prefixIndexes.entrySet()) {
            System.out.printf("%s : %s%n", B16.toString(prefixes.get(count.getKey())), count.getValue());
        }


        System.out.println();
        System.out.println("Unique inner node, branch usage histogram");
        ShaMapInnerNode.Counter<Integer> integerCounter = ledger.slotHistogram(null);
        for (Map.Entry<Integer, Integer> count : integerCounter.entrySet()) {
            System.out.printf("%s : %s%n", count.getKey(), count.getValue());
        }


//        Hash256 hash = ledger.hash();
//        System.out.println(hash);
    }

    public boolean bookKey() {
        return CREATE_BOOKBASES && ++hashes % BOOK_EVERY == 0;
    }

    public double now() {
        return System.nanoTime();
    }

    public void addKeyAsValueItem(ShaMap ledger, Hash256 key) {
        ledger.addLeaf(key, ShaMapNode.NodeType.tnTRANSACTION_MD, createItem(key));
    }

    @Test
    public void dumpRandomInnerNodeRepresentation() throws Exception {
        for (int i = 0; i < 16; i++) {
            byte[] r32 = new byte[32];
            randomer.nextBytes(r32);
            if (false && randomer.nextInt(1) == 0) {
                System.out.println(ShaMapInnerNode.ZERO_256);
            } else {
                System.out.println(new Hash256(r32));
            }
        }
    }

    @Test
    public void testOfferDepth() throws Exception {
        String indexes = "[\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D0100000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D0200000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D0300000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D0400000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D0500000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D0600000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D0700000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D0800000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D0900000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D1000000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D1100000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D1200000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D1300000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D1400000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D1500000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D1600000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D1700000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D1800000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D1900000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D2000000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D2100000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D2200000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D2300000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D2400000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D2500000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D2600000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D2700000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D2800000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D2900000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D3000000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D3100000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D3200000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D3300000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D3400000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D3500000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D3600000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D3700000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D3800000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D3900000000000000\"," +
                "\"71633D7DE1B6AEB32F87F1A73258B13FC8CC32942D53A66D4000000000000000\"]";
        JSONArray array = new JSONArray(indexes);
        Vector256 indexVector = Vector256.translate.fromJSONArray(array);

        ShaMap map = new ShaMap();
        for (Hash256 hash256 : indexVector) {
            addKeyAsValueItem(map, hash256);
            System.out.println(hash256);
        }
        System.out.println(map.totalInnerNodes());
        System.out.println(indexVector.size());
    }
}
