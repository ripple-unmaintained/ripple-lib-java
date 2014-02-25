# SerializedTypes Overview

Ripple uses a node store, where objects are keyed by a 32byte hash value.

These keys are created by hashing a binary representation of the whole object
prefixed with a name-spacing sequence of bytes, unique to each class of object.
As such, a method of consistently producing a binary sequence from a given
object was required.

Objects have fields, which are a pairing of a type and a name. Names are simply
ordinals used to look up preassigned names in a per type table. The type ordinal,
similarly, is used to lookup a given class.

In ripple-lib-java the type equates to a `SerializedType`, with types such as 32
bit unsigned integer, variable length byte strings and even arrays of objects.
The fields have an ordinal quality, and can be deterministically sorted,
important for consistent binary representation.

There are also container types: An STObject, short for `SerializedType Object`,
is an associative container of fields to other SerializedTypes (even an STObject
itself). An STArray is an array of STObjects with a single Field, mapped to an
STObject, containing an arbitrary amount of Field -> SerializedType pairs. (See
notes below on differences to C++ implementation)

## Types

Following is a survey of some of the classes, sorted somewhat topologically.

The following json representation of metadata related to a finalized transaction
can be used as a concrete example and will be referenced later.

```json
{"AffectedNodes": [{"CreatedNode": {"LedgerEntryType": "Offer",
                                    "LedgerIndex": "3596CE72C902BAFAAB56CC486ACAF9B4AFC67CF7CADBB81A4AA9CBDC8C5CB1AA",
                                    "NewFields": {"Account": "raD5qJMAShLeHZXf9wjUmo6vRK4arj9cF3",
                                                  "BookDirectory": "62A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000",
                                                  "OwnerNode": "000000000000000E",
                                                  "Sequence": 103929,
                                                  "TakerGets": {"currency": "ILS",
                                                                "issuer": "rNPRNzBB92BVpAhhZr4iXDTveCgV5Pofm9",
                                                                "value": "1694.768"},
                                                  "TakerPays": "98957503520"}}},
                   {"CreatedNode": {"LedgerEntryType": "DirectoryNode",
                                    "LedgerIndex": "62A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000",
                                    "NewFields": {"ExchangeRate": "5C14BE8A20D7F000",
                                                  "RootIndex": "62A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000",
                                                  "TakerGetsCurrency": "000000000000000000000000494C530000000000",
                                                  "TakerGetsIssuer": "92D705968936C419CE614BF264B5EEB1CEA47FF4"}}},
                   {"ModifiedNode": {"FinalFields": {"Flags": 0,
                                                     "IndexPrevious": "0000000000000000",
                                                     "Owner": "raD5qJMAShLeHZXf9wjUmo6vRK4arj9cF3",
                                                     "RootIndex": "801C5AFB5862D4666D0DF8E5BE1385DC9B421ED09A4269542A07BC0267584B64"},
                                     "LedgerEntryType": "DirectoryNode",
                                     "LedgerIndex": "AB03F8AA02FFA4635E7CE2850416AEC5542910A2B4DBE93C318FEB08375E0DB5"}},
                   {"ModifiedNode": {"FinalFields": {"Account": "raD5qJMAShLeHZXf9wjUmo6vRK4arj9cF3",
                                                     "Balance": "106861218302",
                                                     "Flags": 0,
                                                     "OwnerCount": 9,
                                                     "Sequence": 103930},
                                     "LedgerEntryType": "AccountRoot",
                                     "LedgerIndex": "CF23A37E39A571A0F22EC3E97EB0169936B520C3088963F16C5EE4AC59130B1B",
                                     "PreviousFields": {"Balance": "106861218312",
                                                        "OwnerCount": 8,
                                                        "Sequence": 103929},
                                     "PreviousTxnID": "DE15F43F4A73C4F6CB1C334D9E47BDE84467C0902796BB81D4924885D1C11E6D",
                                     "PreviousTxnLgrSeq": 3225338}}],
 "TransactionIndex": 0,
 "TransactionResult": "tesSUCCESS"}
 ```

* Note that ALL field names are, by convention, upper case (in fact field names (index, hash) may be
lower cased but aren't serialized)

* The `AffectedNodes` is an STArray. As stated, the immediate
children each contain only a single key (or [Field](src/main/java/com/ripple/core/fields/Field.java#L96-L97))

  * CreatedNode
  * ModifiedNode

Moving on.

```
com
└── ripple
    ├── core
    │   │
    │   ├── enums
    │   │   ├── LedgerEntryType
    │   │   ├── TransactionEngineResult
    │   │   └── TransactionType
```

* In the json above look at the [TransactionResult](src/main/java/com/ripple/core/fields/Field.java#L112) field.
  Note that it has a Type of of UINT8, yet clearly it's represented in json as a string.

  These `field symbolics` are enumerated [here](src/main/java/com/ripple/core/enums) and an
  abstract interface for dealing with them [here](src/main/java/com/ripple/core/fields/FieldSymbolics.java#L9)

#### com.ripple.core.fields.Type

  This is a simple Java enum(eration) of the various types. eg.

    UINT32(2)

  This definition implies giving the static ordinal `2` to the UINT32 type.

#### com.ripple.core.fields.Field

  Consider the following definition of a field

    QualityIn(20, Type.UINT32)

  As stated before a Field has name and type ordinals, but it also has an
  implied symbolic string representation (as seen used in the json above)

  The string name can be looked up via a `code`, which is an integer created by
  shifting the type ordinal 16 bits to the left and ORing it with the name.

  See: [com.ripple.core.fields.Field#fromCode](src/main/java/com/ripple/core/fields/Field.java)

#### com.ripple.core.fields.FieldSymbolics

  We've already seen the use of FieldSymbolics for

  * TransactionResult
  * LedgerEntryType
  * TransactionType

#### com.ripple.core.fields.HasField

  This is simply an interface for returning a Field. We know that a Field
  implies a Type and a name and there's a set amount of them. For each concrete
  class implementation of a given Type, we create a XXXfield class that
  implements HasField

  eg.

  ```java
  protected abstract static class STArrayField implements HasField{}
  public static STArrayField starrayField(final Field f) {
      return new STArrayField(){ @Override public Field getField() {return f;}};
  }
  ```

  Then we can create static members on the concrete class

  ```java
  static public STArrayField AffectedNodes = starrayField(Field.AffectedNodes);
  static public STArrayField Signatures = starrayField(Field.Signatures);
  static public STArrayField Template = starrayField(Field.Template);
  ```

  Later this is used create an api that looks as so

  ```java
  if (transactionType() == TransactionType.Payment && meta.has(STArray.AffectedNodes)) {
      STArray affected = meta.get(STArray.AffectedNodes);
      for (STObject node : affected) {
          if (node.has(STObject.CreatedNode)) {
              STObject created = node.get(STObject.CreatedNode);
  ```

  This is implemented by overloading get()

  ```java
  public STArray get(STArray.STArrayField f) {
      return (STArray) fields.get(f.getField());
  }
  ```

### com.ripple.core.serialized

#### com.ripple.core.serialized.SerializedType

```java
public interface SerializedType {
    Object toJSON();
    byte[] toBytes();
    String toHex();
    void toBytesSink(BytesSink to);
}
```

#### com.ripple.core.serialized.BytesList

A dynamic array of byte[]. Used by TypeTranslators to avoid needless 
copying (see fromParser(paser, hint)).

#### com.ripple.core.serialized.Markers

Definitions of STObject and STArray, binary stream, end markers.

#### com.ripple.core.serialized.BinaryParser

Responsible for decoding Fields and VL encoded structures.

#### com.ripple.core.serialized.TypeTranslator

Handles converting a SerializedType instances to/from json, binary and other non
SerializedType values.

Has methods like fromHex, fromBytes, which delegate to fromParser.

#### com.ripple.core.serialized.BinarySerializer

Responsible for encoding Fields/SerializeType into binary.

## Notes

* In the C++ implementation of serialized objects, an STObject can, itself, be
  assigned a Field, which is stored outside of the associative structure.

  This is problematic when storing as json. Consider this pseudocode.

    ```python
    >>> so = STObject()
    >>> so.name = "FieldName"
    >>> so["FieldOfDreams"] = "A Kevin Costner Movie"
    >>> sa = STArray([so])
    ```

  How could `sa` be declared as json?

    ```json
    >>> [{"FieldName" : {"FieldOfDreams": "A Kevin Costner Movie"}}]
    ```

  This is in fact how rippled works. There is no 1:1 mapping of STObject to {}

  In ripple-lib(-java)? there is, and single key children of STArrays are enforced.

* Ripple uses 32 byte hashes for object indexes, taking half of a SHA512 hash,
  which is ~%33 faster than SHA256.

* Simply using google protocol buffers was considered inadequate [link](https://github.com/ripple/rippled/blob/ee51968820fc41c5aeadf2067bfdae54ff21fa66/BinaryFormats.txt#L16)

```
└── com
    └── ripple
        ├── config
        │   └── Config.java
        ├── core
        │   ├── binary
        │   │   ├── BinaryReader.java
        │   │   └── BinaryWriter.java
        │   ├── coretypes
        │   │   ├── AccountID.java
        │   │   ├── Amount.java
        │   │   ├── Currency.java
        │   │   ├── hash
        │   │   │   ├── Hash128.java
        │   │   │   ├── Hash160.java
        │   │   │   ├── Hash256.java
        │   │   │   ├── Hash.java
        │   │   │   └── prefixes
        │   │   │       ├── HashPrefix.java
        │   │   │       ├── LedgerSpace.java
        │   │   │       └── Prefix.java
        │   │   ├── Issue.java
        │   │   ├── PathSet.java
        │   │   ├── Quality.java
        │   │   ├── RippleDate.java
        │   │   ├── STArray.java
        │   │   ├── STObject.java
        │   │   ├── uint
        │   │   │   ├── UInt16.java
        │   │   │   ├── UInt32.java
        │   │   │   ├── UInt64.java
        │   │   │   ├── UInt8.java
        │   │   │   └── UInt.java
        │   │   ├── VariableLength.java
        │   │   └── Vector256.java
        │   ├── enums
        │   │   ├── LedgerEntryType.java
        │   │   ├── TransactionEngineResult.java
        │   │   └── TransactionType.java
        │   ├── fields
        │   │   ├── Field.java
        │   │   ├── FieldSymbolics.java
        │   │   ├── HasField.java
        │   │   ├── TypedFields.java
        │   │   └── Type.java
        │   ├── formats
        │   │   ├── Format.java
        │   │   ├── SLEFormat.java
        │   │   └── TxFormat.java
        │   ├── runtime
        │   │   └── Value.java
        │   ├── serialized
        │   │   ├── BinaryParser.java
        │   │   ├── BinarySerializer.java
        │   │   ├── BytesList.java
        │   │   ├── BytesSink.java
        │   │   ├── SerializedType.java
        │   │   └── TypeTranslator.java
        │   └── types
        │       ├── known
        │       │   ├── sle
        │       │   │   ├── entries
        │       │   │   │   ├── AccountRoot.java
        │       │   │   │   ├── DirectoryNode.java
        │       │   │   │   ├── Offer.java
        │       │   │   │   └── RippleState.java
        │       │   │   ├── LedgerEntry.java
        │       │   │   └── ThreadedLedgerEntry.java
        │       │   └── tx
        │       │       ├── result
        │       │       │   ├── AffectedNode.java
        │       │       │   ├── TransactionMeta.java
        │       │       │   └── TransactionResult.java
        │       │       ├── signed
        │       │       │   └── SignedTransaction.java
        │       │       ├── Transaction.java
        │       │       └── txns
        │       │           ├── AccountSet.java
        │       │           ├── OfferCancel.java
        │       │           ├── OfferCreate.java
        │       │           ├── Payment.java
        │       │           └── TrustSet.java
        │       ├── ledger
        │       │   └── Ledger.java
        │       └── shamap
        │           ├── NodeStore.java
        │           ├── README.md
        │           ├── ShaMapInnerNode.java
        │           ├── ShaMap.java
        │           ├── ShaMapLeafNode.java
        │           └── ShaMapNode.java
        ├── crypto
        │   ├── ecdsa
        │   │   ├── ECDSASignature.java
        │   │   ├── IKeyPair.java
        │   │   ├── KeyPair.java
        │   │   ├── SECP256K1.java
        │   │   └── Seed.java
        │   └── sjcljson
        │       ├── JSEscape.java
        │       └── JSONEncrypt.java
        ├── encodings
        │   ├── B58IdentiferCodecs.java
        │   ├── base58
        │   │   ├── B58.java
        │   │   └── EncodingFormatException.java
        │   └── common
        │       ├── B16.java
        │       └── B64.java
        └── utils
            └── Utils.java
```