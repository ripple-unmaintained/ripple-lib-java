Rippled NodeStore
-----------------

To understand a ShaMap first you must know about the NodeStore.

```java
/**

 * This is a toy implementation for illustrative purposes.
 */
public class NodeStore {
    /**
    * In ripple, all data is stored in a simple binary key/value database.
    * The keys are 256 bit binary strings and the values are binary strings of
    * arbitrary length.
    */
    public static interface KeyValueBackend {
        void   put(Hash256 key, byte[] content);
        byte[] get(Hash256 key);
    }

    KeyValueBackend backend;
    public NodeStore(KeyValueBackend backend) {
        this.backend = backend;
    }
    /**
     * All data stored is keyed by the hash of it's contents.
     * Ripple uses the first 256 bits of a sha512 as it's 33 percent
     * faster than using sha256.
     *
     * @return `key` used to store the content
     */
    private Hash256 storeContent(byte[] content) {
        Hash256.HalfSha512 hasher = new Hash256.HalfSha512();
        hasher.update(content);
        Hash256 key = hasher.finish();
        storeHashKeyedContent(key, content);
        return key;
    }

    /**
     * @param hash As ripple uses the `hash` of the contents as the
     *             NodeStore key, `hash` is pervasively used in lieu of
     *             the term `key`.
     */
    private void storeHashKeyedContent(Hash256 hash, byte[] content) {
        // Note: The real nodestore actually prepends some metadata, which doesn't
        // contribute to the hash.
        backend.put(hash, content); // metadata + content
    }

    /**
     * The complement to `set` api, which together form a simple public interface.
     */
    public byte[] get(Hash256 hash) {
        return backend.get(hash);

    }
    /**
     * The complement to `get` api, which together form a simple public interface.
     */
    public Hash256 set(byte[] content) {
        return storeContent(content);
    }
}
```

See also:
* [serialized types](../../README.md)
* [BinaryFormats.txt (historical)](https://github.com/ripple/rippled/blob/07df5f1f81b0ee1ab641d134ba8e940a90f5297e/BinaryFormats.txt#L2-L6)

Excerpt from BinaryFormats.txt (historical): 

  <blockquote>
  All signed or hashed objects must have well-defined binary formats at the
  byte level. These formats do not have to be the same as the network wire
  formats or the forms used for efficient storage or human display. However,
  it must always be possible to precisely re-create, byte for byte, any signed
  or hashed object. Otherwise, the signatures or hashes cannot be validated.
  </blockquote>

Note that currently (2/Feb/2014) the NodeStore stores it in the hashing form.

What is a ShaMap?
-----------------

A ShaMap is a special type of tree, used as a way to index values stored in a
`NodeStore`

Recall that values in the nodestore are keyed by the hash of their contents.

But what about identities that change over time? How can you retrieve a certain
version of something? What could be used as an enduring identifier? The value
must have some component[s] that are static over time. These are fed into a
hashing function to create a 256 bit identifier.

But how is this used? You can only query values by `hash` in the NodeStore. The
hash, as a function of a value would obviously change along with it.

The identifier is used as an `index` into a ShaMap tree, which in ripple, is
representative of a point in time. In fact a shamap can be hashed
deterministically, thus a point in time can be identified by a `hash`. Where is
a ShaMap actually stored? In the NodeStore, of course.

But the NodeStore only stores binary content you protest! But the ShaMap has a
binary representation! So what are the `contents` of a ShaMap, to be hashed to
be stored in the nodestore?

Glad you asked. A tree, has a root, and many children, either more branches, or
terminal leaves. The root, and any of its children that have children
themselves, are classed as `inner nodes`.

In a ShaMap these `inner nodes` each have 16 `slots` for children. The binary
representation of these is simply 16 `hash`es, used to retrieve child nodes from
the NodeStore.

An example of an inner node's `contents`

  Empty slots are represented as 32 0 bytes

  ```
  022CC592F5D4ABC3A63DA2A036CDDC0825B30717C78EF287BEF200056133FDA2
  0000000000000000000000000000000000000000000000000000000000000000
  BEE626551799DDFE65BD2D9A0F0EA24D72C93CFD8E083176718D2B079EC60214
  E1B34F1D9209CB668A50CCEE71C8109D140A6D715D923AEE98E6D53015D8B66B
  4C27A856094CFDE37CD2A0EA93DADB595B10CFEC55F816C987A6AC48D13AF5C0
  2F770714A9EF92792F44AA1537C18F68AFE3FFF157FB9088FFE2BDA695C19B71
  C915CA982310CF41CF1266AA43C3B31ACBF4304D05ADB54A352D942C890763A3
  F29FAD442CE204513BEA555A4192E324407444D946449CEA510C37A9BB982134
  0000000000000000000000000000000000000000000000000000000000000000
  0000000000000000000000000000000000000000000000000000000000000000
  44B3BE10744EA2DA010D530C6AE64E3C3984DA7701EE79A66EA429EC11B87D1D
  0C7BE8569E9F08BADDEB91EBE79E5B98BBF245B067B7B83B4A95430CFEC9F7E8
  BB38EA169DB6A020EA820BD1242DDB6250B397A26015507BA3C7F3C041EA683C
  0000000000000000000000000000000000000000000000000000000000000000
  15B98934D22B5CB7233C42CE8DC8DD0D2328AB91CC574332C45D7160BD31D4AD
  1894E389AE4A63BA99C2D0546A58A976ECDAB14C09B98F532999B464696E29E5
  ```

What about those `index` thingies again? Remember, the `index` can't be used to
query something directly in the NodeStore.

First you need a known point in time, which we learned could be defined by a
`hash` of a ShaMap.

* shamap hash: E4984329FFD3D06C882706C190503412B0AF49A37499BE2DF82251F94D5CC3E6
* value index: DF68EE71EE9141E24B87E630976C1F9071F74AD073BD03578083FDD9098B4BD9

Imagine we have those above. First we query the nodestore with `E49843...`

What do we expect back? A ShaMap hash is the hash of the binary representation
of the root node (which is an `inner node`) of the tree, so we'd expect
something in the form shown earlier, with 16 256 bit hashes.

From the nodestore we retrieve:

  ```
  40494E00
  DA0E8E7247BD8F35D53D3CB9308A1F63F2A1FFC9C6F92F5BC4F8F2AE227CE5B3
  685CDDA83DB325FFF2CB72A3D70BD63E65F77B2811E95A18A49906439D360424
  6450A66258D9CD3FD51D49E4F636F1FA7FA5D7CB8594605DA4539377CFD1366C
  897BFBE62EF1141396D583C9A22408D9608B02F0044382CA717A1670EB09FFD8
  DA482A1708EAFBF2E3FF455215692DB37DFCEA866BADC111BAF2B5B488AF326A
  339535D1FC79BFC345A93F4043333EEDE77F79F9272944A5BC65FFC287C009E7
  7DD55B28D92CA53A80D04BE1EE37723E1BE5C739882281167ADDEB0DFDB82FA0
  7B1E3DC2D03DF0D0F3048BFD159604D92ABCA894A18D74FF9C1AEFF11B7129FB
  C37F30564948140DA6C1F247636747EEF749E6B5F4F52A9BFB9F25066B322F91
  A86DE64AF331FD05F783E2702C748FE21F46C121CA6A4BA164033A8C9AE9A332
  36744569307984372139D3A25B43EB2ABB1485E7BFD87EB9E46F40ED56447C45
  5CA21C8D6D437C978A63D243D67798977DE1E5D09C6CAB2E897DB6A4EA998589
  DB3DC38FB1EA94DA778BEB6A60414DFC96E83C2C8E49283BB139E963D1BC5E98
  7C0B1C90CD3AE3446F65A1F59B36E5C990A7386C1D513E224A59BA404B6ED58C
  5C8BD6B57668FBBAE9BD683F2184EB494B9657A711C9B86D67F14FA3D84023B9
  838777ADAF945A4CB481644B6D0923C807375CF3D7B3DED87268D144D7C09768
  ```

We see the hashes for 16 nodes clear as day, but what is this `40494E00`
prefixed to the front? Converted to `ascii` letters the hex `40494E00` is 
`MIN\x00`, meaning sham)ap i)nner n)ode.

The prefix `namespaces` the content, so different classes of objects which
would otherwise have the same binary representation, will have a different
`hash`. These `hash prefixes` serve another useful purpose, as we'll see later.
(Similarly, there are namespacing prefixes for an `index` (created by
 feeding static components of an identity into a hashing function))

Is our value `index` hash amongst those enumerated? No !!! So what do we do with
it? An index, usually means an ordinal, defining a place in an array. The
`index` is actually an index into 64 arrays. Each nibble in the `index` is an
index into the 16 slots in each inner node.

Consider again the value `index`:

  `DF68EE71EE9141E24B87E630976C1F9071F74AD073BD03578083FDD9098B4BD9`

To use the `index` we take the first nibble, `D` (yes, we go left to right)

The letter `D` in hex has the ordinal value 13, so we take the 14th branch (0
based indexing)

  (If this is unclear, see `annoyingly verbose` ascii art @ bottom of section)

We select the 14th hash

  `7C0B1C90CD3AE3446F65A1F59B36E5C990A7386C1D513E224A59BA404B6ED58C`

From the nodestore we retrieve:

  ```
  40494E00
  25CCD7BE2CB8BC77C832BDB55659E4C5CF9FD9C062164BEE6EB8A92BE93F19FE
  98998C886894A87A4D4E0553D629804086526B8AA4D0856861060843ACAF38A8
  BF2B532A44CC3373283AB9EA499CF7C313488DCF068310868C4F49847041E3EE
  A2C09E311166FF62D669319E3554E8BA2CCDC53F0745CF44CFEFB266F0E50619
  40D4BABAEBF0501B86B45DB8D857EB06AEFD359DD3387D53E4AEE8BDFFC65673
  5805BEF8086F70E34534796C38FB62FFC977298AC3F25A6C13D82200292C7AD5
  BF9CC8B96324F619FFB3AE82CF898C58C4C191ECB902F87BB69634A5E8A25AB6
  537D11D98003E92F5AB45F96E20F2DF89F4766F632A6B2BD911CDEF7F94FF556
  DF2A4B52B0DBBDFE5BE97F116414BC31C2D7AACBA2F1CF801977C523399D950E
  6F09A1DE35B7D9931F6EC38BEE5C75F0D0057B15AB5D5171F9441671EFF4F5AA
  B49303B5D6EB092021C4C0F3E4A1943E49A7CD661E8C77BD99FAA72335B03D47
  08813D9C2D3103BB3D8234F38F25A1834F5D6DB1118F578C9811FD4217C37850
  C425AAAB4B35502B9F14D3F265955BE72CA741AA19876A315D999F7F0FFD1324
  15CAD3899E675402F19546016570252778A61D900D9E54D5A217A348DB245557
  34257698B5A753495A416D0EC8E1B45E438563D12AF66210B8F40A3CD69E84F2
  272D03DC4D1A559FF23DADA65FFFB652E7A727F5D857BEC83C029BC662F79034
  ```

There's that 'MIN\x00` hash prefix again.

In fact, this prefix is how we can deterministically say that this is an
`inner node` and that we can interpret the following bytes as 16 more
`hash`es.

We have descended deeper into the tree, but it seems we need to go deeper. We
are currently at a depth of 2, so to go deeper we need the 2nd nibble.

value index:
  ```
  DF68EE71EE9141E24B87E630976C1F9071F74AD073BD03578083FDD9098B4BD9`
   |
    \
     2nd nibble
  ```

The letter `F` in hex has the ordinal value 15, so we take the 16th branch (0
based indexing)

We select the 16th hash:

  `272D03DC4D1A559FF23DADA65FFFB652E7A727F5D857BEC83C029BC662F79034`

From the nodestore we retrieve:

  ```
  4D4C4E00
  201C00000000F8E311006F563596CE72C902BAFAAB56CC486ACAF9B
  4AFC67CF7CADBB81A4AA9CBDC8C5CB1AAE824000195F93400000000
  0000000E501062A3338CAF2E1BEE510FC33DE1863C56948E962CCE1
  73CA55C14BE8A20D7F00064400000170A53AC2065D5460561EC9DE0
  00000000000000000000000000494C53000000000092D705968936C
  419CE614BF264B5EEB1CEA47FF4811439408A69F0895E62149CFCC0
  06FB89FA7
  DF68EE71EE9141E24B87E630976C1F9071F74AD073BD03578083FDD9098B4BD9
  ```

Well, here's something new. The `hash prefix` is different. This time the
hex decodes as `MLN\x00`, meaning sham)ap l)eaf n)ode.

And what's that at the end? Is that our index? It is!!

Why does it need to be stored? We have only used `DF` to traverse to this
node. Without storing the `index` identifier in the leaf node contents,
there would be no way to be certain that this leaf held the item you wanted.
More importantly, it acts as further name-spacing, to prevent collisions. 
(Technically, you could synthesize the index, by parsing the contents of
the object and recreating it)

Takeaways
---------

* A `hash` keys the nodestore
* An `index` is a path to an item in a ShaMap
* For communication purposes
  - Always use `hash` when referring to a key for the nodestore
  - Always use `index` when referring to a key for a ShaMap

Links
-----

* [ShamapInnerNodeAnalyis](../../../../../../../../../ripple-examples/ripple-cli/src/main/java/com/ripple/cli/shamapanalysis/ShamapInnerNodeAnalysis.java)
* [Rippled Hash Prefix declarations](https://github.com/ripple/ripple-lib-java/blob/master/ripple-core/src/main/java/com/ripple/core/coretypes/hash/prefixes/HashPrefix.java)

Annoyingly verbose ascii art
----------------------------

```
  DF68EE71EE9141E24B87E630976C1F9071F74AD073BD03578083FDD9098B4BD9
  || \_____
  \ \____  \
   \___  \  \
       D  F  6  8   E  E  7  1   E  E  9  1   4  1  E  2   4  B  8  7   E  6
^ <----                 nibble (depth of inner node)                       ---->
|     01 02 03 04  05 06 07 08  09 10 11 12  13 14 15 16  17 18 19 20  21 22 ...

s      !  !  !  !   !  !  !  !   !  !  !  !   !  !  !  !   !  !  !  !   !  !
l
o  00  |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |
t  01  |  |  |  |   |  |  |  1   |  |  |  1   |  1  |  |   |  |  |  |   |  |
   02  |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  2   |  |  |  |   |  |
i  03  |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |
n
   04  |  |  |  |   |  |  |  |   |  |  |  |   4  |  |  |   4  |  |  |   |  |
i  05  |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |
n  06  |  |  6  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  6
n  07  |  |  |  |   |  |  7  |   |  |  |  |   |  |  |  |   |  |  |  7   |  |
e
r  08  |  |  |  8   |  |  |  |   |  |  |  |   |  |  |  |   |  |  8  |   |  |
   09  |  |  |  |   |  |  |  |   |  |  9  |   |  |  |  |   |  |  |  |   |  |
n  10  |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |
o  11  |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  B  |  |   |  |
d
e  12  |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |
   13 [D] |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |
|  14  |  |  |  |   E  E  |  |   E  E  |  |   |  |  *  |   |  |  |  |   E  |
V  15  |  F  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |  |  |   |  |
```
