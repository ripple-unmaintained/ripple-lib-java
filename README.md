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
  - Some thought still needs to be given to how this will work in different
    environments, be it single threaded async, or on android.
  - Android example
    - Using class loader patch to use Bouncy Castle 1.4.9
  - CLI example
  - Api client choice of websocket transport

TODO:
  - Binary parsing
  - Documentation
  - General cleanup of code / api surface

Examples:
  
  see in examples/ folder
