TransactionManager
------------------

The `TransactionManager` is in charge of submitting transactions to the network
in such a way that is resilient to poor network conditions (busy, dropouts).

There is, in fact, a non 100% success rate for every transaction submission.
This can be due to conditions at the transport layer, or the node submitted to
could be busy, completely dropping a submission, or even outright rejecting it,
due to an insufficient fee for the current load.

Therefore, client libraries will present an API that in the background
automatically handles resubmitting transactions (where it makes sense to do so)

There are 3 phases to submitting a transaction and verifying it has succeeded:

  1. Submission to a ripple node

    This is done by making a `submit` RPC request.

    The transaction has entered the transport layer, and could be resubmitted  by
    3rd parties (helpfully or maliciously)

  2. Transaction provisionally successful

    At this point the transaction has succeeded in being applied to the local
    node's ledger. It is then distributed amongst the network. It may take more
    than one ledger close before it's finalized.

    The rippled node will respond to the (stage 1) `submit` request.

  3. Transaction validated

    The transaction has made it into a validated ledger.

    The local node will send out to subscribers of a given account, notification
    of any transactions that affect it as they are validated by the network.

Locally, from the client perspective it's only possible to really know that the
first step has failed.

It's possible (and has been observed), that at times there will be no response
or notifications from the node the client is connected to, for a transaction
that will ultimately find its way into a validated ledger.

It's very important to realize that `no response` isn't necessarily an indication
of failure.

Each `Transaction` submitted to the network, in binary form, has the following:

  - A transaction signature `TxnSignature` field
    - what is signed is a hash of the transaction contents in binary form
    - this is ecdsa, which has a random component
      - signing the EXACT same content multiple times will result in multiple
        unique signatures.

  - a `hash` (sometimes referred to as a transactionID)
    - dubbed a `hash`, due to it being a hash of the binary representation
      of the transactions.

      - IMPORTANT: the contents include the RANDOM TxnSignature

      - The hash is used to map transaction notification messages
        to submitted transactions in the pending queue.

  - `Fee`

    - This is the amount of drops (1e-6 XRP) destroyed.

    - There is a minimum Fee, which scales with the current load on the node
      submitted to.

So:
  - May not get response/notification of successful submission/validation
  - May need to change the `Fee` and resubmit for a transaction to be applied
  - Transactions are identified by hash of binary repr (incl. Fee and random sig)

Therefore, as a transaction gets submitted multiple times, if the transaction
binary representation content changes, eg. due to a Fee change, it becomes
necessary to look out for ALL submitted transactionIDs to map incoming transaction
notifications to a given Transaction.

To understand why, the `Sequence` field must also be considered:

  - Sequence

    In a new Account, there is an integer field `Sequence`, which is initialized
    with the value `1`.

    Each transaction submitted by the account, must declare a `Sequence` too.
    The transaction sequence must exactly match that stored in the account node.

    Any transaction that is successful increments the Sequence in the account
    node.

    This is to stop replay attacks. Each transaction can be applied only once.

It's possible when multiple clients are submitting transactions for the same
account that there will be contention for Sequence numbers. One client will need
to increment the sequence on a transaction before resubmission for it to succeed.

How can this be detected? As transaction notifications come in, the hash of each
transaction is compared against that of all the pending transactions. If a hash
matches, the transaction is cleared, otherwise if the Sequence matches any of
the pending transactions, it's assumed that the Sequence has been consumed by
another client.

  >>> the hash of each transaction is compared against that of all the pending
  >>> transactions

Actually. it must be compared against **all hashes of any transaction serializations
that entered the transport layer**, not just the most recent one submitted, otherwise
there runs the risk of submitting the same transaction more than once. (The
Sequence will be incremented, as it's assumed that a transaction submitted by
another client consumed the Sequence, when actually it's just a prior submission)

Robust transaction validation
-----------------------------

For various reasons the validated transaction subscription messages could
be missed.

  - Transport layer issues
    - messages missed

  - Simply stalled nodes

Therefore, periodically, when there are pending transactions, the transaction
manger will request a list of transactions from the server, from ledger $n
and forward. $n is determined by looking at the ledger_index of the oldest
submission in the pending queue of transactions.

The same logic that handles clearing transactions from the account subscription
transaction stream is applied to each transaction in the transaction list.

The rpc command `account_tx` is used to get the list of transactions.

Robust transaction invalidation
-------------------------------

You keep submitting until you get a final result, either a response to submit
with a `final` disposition, or a validated transaction (via some combination
of subscription/account_tx).

It must always be kept in mind that once a transaction goes on the wire it can
be played back at any time[1], helpfully or maliciously.

This means, that for failed transactions, the transaction `Sequence` must be
consumed (via a `noop` transaction or Fee claimed tec*), or that an expiry be
placed on the transaction.

In ripple, expiry is done by declaring `LastLedgerSequence` which specifies
the last valid `ledger_index` that a transaction is valid for. In this way,
even if a Sequence is not consumed, the vulnerability to replay is greatly
reduced.

[1] Without declaring `LastLedgerSequence` there is no expiry on transactions.