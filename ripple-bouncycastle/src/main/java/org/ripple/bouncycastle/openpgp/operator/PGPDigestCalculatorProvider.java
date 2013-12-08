package org.ripple.bouncycastle.openpgp.operator;

import org.ripple.bouncycastle.openpgp.PGPException;

public interface PGPDigestCalculatorProvider
{
    PGPDigestCalculator get(int algorithm)
        throws PGPException;
}
