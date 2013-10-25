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

TODO:
  - Api client choice of websocket transport
  - Api client choice of bouncycastle or spongycastle(android)
  - Binary parsing
  - Documentation
  - General cleanup of code / api surface
  

Example:
  
  ```java
  public class CommandLineClient {
      public static void LOG(String fmt, Object... args) {
          System.out.printf(fmt + "\n", args);
      }
      public static void main(String[] args) throws Exception {
          // Will be less ugly
          Client.quiet = true;

          // Construct with chosen transport implementing interface
          Client client = new Client(new JavaWebSocketTransportImpl());
          client.connect("wss://s1.ripple.com");

          // We can retrieve the keys to make transactions as stored by the 
          // official client
          JSONObject blob = PayWard.getBlob("niq1", "xxxxxx");
          String masterSeed = blob.getString("master_seed");
          Account account = client.accountFromSeed(masterSeed);

          makePayment(account, "rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH", "1");
      }

      // Note destination can be any (valid account repr) `Object`, here we
      // are passing in a String, likewise for the amount, which is one `drop`. 
      private static void makePayment(Account account, Object destination, Object amt) {
          TransactionManager tm = account.transactionManager();
          Transaction tx = tm.payment();

          tx.put(AccountID.Destination, destination);
          tx.put(Amount.Amount, amt);

          tx.once(Transaction.OnSubmitSuccess.class, new Transaction.OnSubmitSuccess() {
              @Override
              public void called(Response response) {
                  LOG("Submit response: %s", response.engineResult());
              }
          });

          tx.once(Transaction.OnTransactionValidated.class, new Transaction.OnTransactionValidated() {
              @Override
              public void called(TransactionResult result) {
                  LOG("Transaction finalized on ledger: %s", result.ledgerIndex);
                  try {
                      LOG("Transaction message:\n%s", result.message.toString(4));
                  } catch (JSONException e) {
                      throw new RuntimeException(e);
                  }

              }
          });
          tm.queue(tx);
      }
    // ...
  }
  
  ```
