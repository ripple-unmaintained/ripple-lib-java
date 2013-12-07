package org.ripple.bouncycastle.openpgp.operator;

import java.io.OutputStream;

public interface PGPDataEncryptor
{
    OutputStream getOutputStream(OutputStream out);

    PGPDigestCalculator getIntegrityCalculator();

    int getBlockSize();
}
