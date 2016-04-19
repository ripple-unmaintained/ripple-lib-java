package com.ripple.crypto.ecdsa;

import com.ripple.config.Config;
import com.ripple.core.coretypes.AccountID;
import com.ripple.encodings.common.B16;
import org.junit.Test;
import org.ripple.bouncycastle.util.Arrays;

import static org.junit.Assert.*;

public class ECDSASignatureTest {

    @Test
    public void testIsStrictlyCanonical() throws Exception {
//        System.out.println(AccountID.fromAddress("rB7SzHJArBJpHLzUVEmfagrMP58pn1rtEK"));
//        System.out.println(AccountID.fromAddress("rG8ennq4pETrpjzZAT14xn1LXmHafQ2Sn4"));
//        System.out.println(AccountID.fromAddress("r4Uq651bfvd5UiDv9kPZ6ViXcaUBuyEmot"));
        String a1 = "rLGvKDzvykHM7yhZFaDoUhMEGpRHUXwPRF";
//        String a2 =  "r4Uq651bfvd5UiDv9kPZ6ViXcaUBuyEmot";
        String a3 = "sneLyaUvA8MzJ7xXi5S5wcUxgDbae";

        System.out.println("a1: " + AccountID.fromAddress(a1));

        byte[] dec = Config.getB58().decode("sneLyaUvA8MzJ7xXi5S5wcUxgDbae");
        System.out.println(dec.length);

        byte[] seed = Arrays.copyOfRange(dec, 1, 17);
        System.out.println(new Seed(seed).toString());
        System.out.println(AccountID.fromKeyPair(new Seed(seed).keyPair(-1)));

        AccountID.fromAddress(a1);
//        AccountID.fromAddress(a2);
//        Seed.fromBase58(a3);
//        assertTrue(ECDSASignature.isStrictlyCanonical(B16.decode("30450220312B2E0894B81A2E070ACE566C5DFC70CDD18E67D44E2CFEF2EB5495F7DE2DAC022100A1EAA3FFE6AFD6B73D9ADF62022827B270A91FBA7679B94EE80F1FF03523827F")));
//        assertTrue(ECDSASignature.isStrictlyCanonical(B16.decode("304402202A5860A12C15EBB8E91AA83F8E19D85D4AC05B272FC0C4083519339A7A76F2B802200852F9889E1284CF407DC7F73D646E62044C5AB432EAEF3FFF3F6F8EE9A0F24C")));
//        assertTrue(ECDSASignature.isStrictlyCanonical(B16.decode("3045022100B1658C88D1860D9F8BEB25B79B3E5137BBC2C382D08FE7A068FFC6AB8978C8040220644F64B97EA144EE7D5CCB71C2372DD730FA0A659E4C18241A80D6C915350263")));
//        assertTrue(ECDSASignature.isStrictlyCanonical(B16.decode("3046022100F3E541330FF79FFC42EB0491EDE1E47106D94ECFE3CDB2D9DD3BC0E8861F6D45022100EC09D6BD229D929368CE1CE80C8D13A29B3C548A6820A27CD5F92C710B05698F")));
//        assertTrue(ECDSASignature.isStrictlyCanonical(B16.decode("3046022100998ABE378F4119D8BEE9843482C09F0D5CE5C6012921548182454C610C57A269022100C9427148EDCA3B4D398CC62195A68B939B30837116CD258224A1217A85CC85A2")));
    }
}