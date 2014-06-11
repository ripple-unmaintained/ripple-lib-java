package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.HalfSha512;
import com.ripple.core.coretypes.hash.Hash256;

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
        HalfSha512 hasher = new HalfSha512();
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