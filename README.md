ripple-lib-java
===============

Java version of ripple-lib (alpha work in progress)

Currently looking for java/android developers to help evolve this library/api.

Please open an issue with any questions/suggestions.

The goal for this is to be an implementation of ripple-types, binary
serialization, with a websocket library agnostic implementation of a client,
which will track changes to accounts balances/offers/trusts, that can be used as
the basis for various clients/wallets.

Current status:

  - Working implementations of sjcl.json aes/ccm for payward blob retrieval
  - Working implementations of binary serialization
  - Crude implementation of a high level client
  - Test suite
  - Android example
    - Using class loader patch to use predexe Bouncy Castle 1.4.9
      - MUCH faster builds, don't need to dex or merge each time
      - No need to fork a squishy/boingy castle and maintain two crypto providers
  - CLI example
  - Api client choice of websocket transport

TODO:
  - Publisher contexts
    - thread execution context
    - easily unbind handlers in onDestroy/onSemanticallyEtc

  - Class path patcher for android needs testing on 2.x
  - Binary parsing
  - Documentation
  - General cleanup of code / api surface

Examples:

  See in examples/ folder
