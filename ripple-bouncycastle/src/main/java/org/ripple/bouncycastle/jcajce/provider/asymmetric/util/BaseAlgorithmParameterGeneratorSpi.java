package org.ripple.bouncycastle.jcajce.provider.asymmetric.util;

import java.security.AlgorithmParameterGeneratorSpi;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.ripple.bouncycastle.jcajce.util.BCJcaJceHelper;
import org.ripple.bouncycastle.jcajce.util.JcaJceHelper;

public abstract class BaseAlgorithmParameterGeneratorSpi
    extends AlgorithmParameterGeneratorSpi
{
    private final JcaJceHelper helper = new BCJcaJceHelper();

    public BaseAlgorithmParameterGeneratorSpi()
    {
    }

    protected final AlgorithmParameters createParametersInstance(String algorithm)
        throws NoSuchAlgorithmException, NoSuchProviderException
    {
        return helper.createAlgorithmParameters(algorithm);
    }
}
