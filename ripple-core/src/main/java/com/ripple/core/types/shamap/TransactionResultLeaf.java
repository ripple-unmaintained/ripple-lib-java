package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.HalfSha512;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.serialized.BinarySerializer;
import com.ripple.core.types.known.tx.result.TransactionResult;

public class TransactionResultLeaf extends ShaMapLeafNode {
    TransactionResult result;

    public TransactionResultLeaf(TransactionResult result) {
        type = NodeType.tnTRANSACTION_MD;
        index = result.hash;

        this.result = result;
    }

    @Override
    public Hash256 hash() {
        HalfSha512 hasher = HalfSha512.prefixed256(HashPrefix.txNode);

        BinarySerializer write = new BinarySerializer(hasher);

        write.addLengthEncoded(result.txn);
        write.addLengthEncoded(result.meta);

        index.toBytesSink(hasher);

        return hasher.finish();
    }
}
