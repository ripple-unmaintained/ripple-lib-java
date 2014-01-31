The Ripple Java Library
===============

Java version of ripple-lib (alpha work in progress)

Currently looking for java/android developers to help evolve this library/api.

Please open an issue with any questions/suggestions.

The goal for this is to be an implementation of ripple-types, binary
serialization, with a websocket library agnostic implementation of a client,
which will track changes to accounts balances/offers/trusts, that can be used as
the basis for various clients/wallets.

### Docs

  - [Serialized Types](ripple-core/README.md)
  - [Shamap](ripple-core/src/main/java/com/ripple/core/types/shamap/README.md)

### Current status

  - sjcl.json aes/ccm for (wallet) blob decrytion
  - binary serialization/parsing/shamap
  - Crude implementation of a high level client
  - Api client choice of websocket transport
  - Test suite for core types
  - Signing / Verification
  - KeyPair creation
  - Android example
  - Inlined version of BouncyCastle 1.49
    - Provider name: "RBC"
    - Package name: org.ripple.bouncycastle
  - CLI example

### TODO
  - Complete test coverage
  - General cleanup/stabilisation of code / api surface
  - Documentation
  - Generate keypair for particular account_id from a master_seed

### Examples

  - See in ripple-examples/ folder
