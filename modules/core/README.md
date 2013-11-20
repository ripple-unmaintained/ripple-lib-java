# SerializedTypes Overview

Ripple uses a node store, where objects are keyed by a 32byte hash value.

These indexes are created by hashing a binary representation of the whole object
or a unique identifier, prefixed with a name-spacing sequence of bytes, unique
to each class of object.

As such, a method of consistently producing a binary sequence from a given
object was required.

Objects have fields, which are a pairing of a name and a type. Names are simply
ordinals used to look up preassigned names in a table. The type ordinal,
similarly, is used to lookup a given class.

In ripple-lib-java the type equates to a `SerializedType`, with types such as 32
bit unsigned integer, variable length byte strings and even arrays of objects.

The fields have an ordinal quality, and can be deterministically sorted,
important for consistent binary representation.

An STObject, short for `SerializedType Object`, is an associative container of
fields to other SerializedTypes (even an STObject itself)

An STArray is an array of STObjects with a single Field, mapped to an STObject,
containing an arbitrary amount of Field -> SerializedType pairs.

## Notes

* In the C++ implementation of serialized objects, an STObject can, itself, be
  assigned a Field, which is stored outside of the associative structure.

  This is problematic when storing as json. Consider this pseudocode.

    >>> so STObject()
    >>> so.name = "FieldName"
    >>> so["FieldOfDreams"] = "A Kevin Costner Movie"
    >>> sa = STArray([so])

  How could `sa` be declared as json?

    >>> [{"FieldName" : {"FieldOfDreams": "OK"}}]

  This is infact how rippled works. There is no 1:1 mapping of STObject to {}

  In ripple-lib-java there is, and single key children of STArrays are enforced.

* Ripple uses 32 byte hashes for object indexes, taking half of a SHA512 hash,
  which is ~%33 faster than SHA256.

* Simply using google protocol buffers was considered inadequate

# STObject

[1] https://github.com/ripple/rippled/blob/ee51968820fc41c5aeadf2067bfdae54ff21fa66/BinaryFormats.txt#L16

= Topologically Sorted Classes

com
└── ripple
    ├── core
    │   │
    │   ├── fields
    │   │   ├── Type
    │   │   ├── Field
    │   │   ├── FieldSymbolics
    │   │   └── HasField
    │   │
    │   ├── enums
    │   │   ├── LedgerEntryType
    │   │   ├── TransactionEngineResult
    │   │   └── TransactionType

com
└── ripple
    ├── core
    │   │
    │   ├── serialized
    │   │   ├── BytesTree
    │   │   ├── Markers
    │   │   ├── BinarySerializer
    │   │   ├── BinaryParser
    │   │   ├── SerializedType
    │   │   └── TypeTranslator

com
└── ripple
    ├── core
    │   └── types
    │       ├── uint
    │       │   ├── UINT
    │       │   |
    │       │   ├── UInt8
    │       │   ├── UInt16
    │       │   ├── UInt32
    │       │   └── UInt64
    │       │
    │       ├── hash
    │       │   ├── HASH
    │       │   |
    │       │   ├── Hash128
    │       │   ├── Hash160
    │       │   └── Hash256
    │       │
    │       ├── AccountID
    │       ├── Currency
    │       ├── Amount
    │       │
    │       ├── VariableLength
    │       │
    │       ├── PathSet
    │       └── Vector256
    │       ├── STObject
    │       ├── STArray
    │       │
    │       ├── shamap
    │       │   ├── ShaMapInnerNode
    │       │   ├── ShaMap
    │       │   ├── ShaMapLeafNode
    │       │   └── ShaMapNode