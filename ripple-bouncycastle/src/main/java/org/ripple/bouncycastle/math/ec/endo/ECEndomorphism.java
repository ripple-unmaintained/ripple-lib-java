package org.ripple.bouncycastle.math.ec.endo;

import org.ripple.bouncycastle.math.ec.ECPointMap;

public interface ECEndomorphism
{
    ECPointMap getPointMap();

    boolean hasEfficientPointMap();
}
