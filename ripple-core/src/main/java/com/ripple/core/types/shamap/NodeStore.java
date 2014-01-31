package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;

import java.util.Map;
import java.util.TreeMap;

/**
 * In ripple, all data is stored in a simple binary key/value database.
 * The keys are 256 bit binary strings and the values are binary strings of
 * arbitrary length.
 *
 * This is a toy implementation for illustrative purposes.
 */
public class NodeStore {
    // There are various backends available
    Map<Hash256, byte[]> backend;

    public NodeStore() {
        backend = new TreeMap<Hash256, byte[]>();
    }
    /**
     * All data stored is keyed by the hash of it's contents.
     * Ripple uses the first 256 bits of a sha512 as it's 33 percent
     * faster than using sha256.
     *
     * @return `key` used to store the content
     */
    private Hash256 store_content(byte[] content) {
        Hash256.HalfSha512 hasher = new Hash256.HalfSha512();
        hasher.update(content);
        Hash256 key = hasher.finish();
        store_hash_keyed_content(key, content);
        return key;
    }

    /**
     * @param hash As ripple uses the `hash` of the contents as the
     *             NodeStore key, `hash` is pervasively used in lieu of
     *             the term `key`.
     */
    private void store_hash_keyed_content(Hash256 hash, byte[] content) {
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
        return store_content(content);
    }
}