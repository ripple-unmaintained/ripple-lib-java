package org.ripple.bouncycastle.jcajce;

import java.security.cert.CRL;
import java.util.Collection;

import org.ripple.bouncycastle.util.Selector;
import org.ripple.bouncycastle.util.Store;
import org.ripple.bouncycastle.util.StoreException;

public interface PKIXCRLStore<T extends CRL>
    extends Store<T>
{
    Collection<T> getMatches(Selector<T> selector)
        throws StoreException;
}
