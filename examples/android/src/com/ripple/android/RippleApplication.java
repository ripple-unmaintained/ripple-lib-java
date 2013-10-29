package com.ripple.android;

import com.ndudfield.android.dexedrine.DexedrineApp;

import java.io.File;

public class RippleApplication extends DexedrineApp {
    @Override
    public void onCreate() {
        super.onCreate();
        hitTheDex();
        callBootStrapperOnOtherSideWithApplication("com.ripple.android.Bootstrap", "bootstrap");
    }

    @Override
    public String getAssetName() {
        return "predexed.jar";
    }

    @Override
    public boolean forceAssetCopy() {
        return false;
    }

    @Override
    protected File manuallyCopiedPredexedLibsPath() {
        return null;
    }
}
