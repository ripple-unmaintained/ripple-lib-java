package org.ripple.bouncycastle.openpgp.operator;

import org.ripple.bouncycastle.bcpg.PublicKeyPacket;
import org.ripple.bouncycastle.openpgp.PGPException;

public interface KeyFingerPrintCalculator
{
    byte[] calculateFingerprint(PublicKeyPacket publicPk)
        throws PGPException;
}
