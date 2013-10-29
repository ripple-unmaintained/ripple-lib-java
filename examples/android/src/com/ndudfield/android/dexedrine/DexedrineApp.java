package com.ndudfield.android.dexedrine;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Enumeration;

/**
 * @see #hitTheDex()
 * @see #callBootStrapperOnOtherSideWithApplication
 */
abstract public class DexedrineApp extends Application {

    public abstract String getAssetName();
    public abstract boolean forceAssetCopy();

    /**
     * Seeing as this {@link DexedrineApp} is the first class that is loaded by
     * non Dexedrine code any class that is referenced within will use the
     * unpatched class loader. This means any dexedrine loaded libraries and
     * any that depend on them won't be available.
     *
     * Dexedrine's redemption for killing:
     *  {@link android.app.Application#onCreate()}
     *
     * We therefore use reflection, to get access to a static method on another
     * {@link Application} level object.
     *
     * @param    bootStrapClass      The name of the class to look in for
     * @param    bootStrapMethod     The method taking an
     */
    public void callBootStrapperOnOtherSideWithApplication(String bootStrapClass, String bootStrapMethod) {
        try {
            Class<?> cls = getClassLoader().loadClass(bootStrapClass);
            // This method should take an Application class which will already have been loaded
            // by the loader passed in as a parent to the DexedrineLoader.
            // FTW eh?
            Method m = cls.getDeclaredMethod(bootStrapMethod, Application.class);
            // In case you a dumbarse and forgot to set it to public
            m.setAccessible(true);
            // a static method takes null as first parameter
            m.invoke(null, this);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    private final static String ASSET_NAME = "all-libs-dexed.jar";
    ClassLoader mClassLoader = null;

    public static boolean trimCache(Context context) {
        boolean success = false;
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                success = deleteDir(dir);
            }
        } catch (Exception e) {
            Log.d("Dexedrine", "Failed to trimCache due to " + e.toString());
        }

        return success;
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        if (dir != null) {
            Log.d("Dexedrine", String.format("Deleting %s", dir));
            return dir.delete();
        }
        return true;
    }

    @Override
    public ClassLoader getClassLoader() {
        if (mClassLoader != null) {
            log("Someone using DexedrineApp.getClassLoader()");
            return mClassLoader;
        }
        return super.getClassLoader();
    }

    private Field getField(Class<?> cls, String name) {
        for (Field field : cls.getDeclaredFields()) {
            // TODO: this should only be done strictly if needed
            //       this doesn't seem like it has that quality
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    public void hitTheDex() {
        log("DexedrineApp.hitTheDex!! :");
        log("SystemClassLoader %s", ClassLoader.getSystemClassLoader().toString());
        log("The cache folder is %s", getCacheDir());
        log("PackageResourcePath %s", getPackageResourcePath());
        log("getClassLoader().toString() == %s", getClassLoader());
        log("External files dir is %s", getExternalFilesDir(null));

        File dexPath = manuallyCopiedPredexedLibsPath();

        if (dexPath == null) {
            dexPath = getFileStreamPath(getAssetName());
            if (forceAssetCopy() || !dexPath.exists()) {
                copyAsset();
            }
        }

        Date t = new Date();
        boolean dexLoaded = loadDex(dexPath.getAbsolutePath());
        log("dexLoaded %b", dexLoaded);
        Date after = new Date();
        log("dexLoaded took %d ms", after.getTime() - t.getTime());
        patchApkLoader();
        logLoaders();
    }

    protected abstract File manuallyCopiedPredexedLibsPath();

    private boolean copyAsset() {

        if (copyAssetToFileDir(getAssetName())) {
            log("Successfully copyAssetToFileDir. Trimming cache");
            trimCache(this);
            return true;
        } else {
            log("Failed copyAssetToFileDir. Did you move it out to speed up the build eh ;) ?");
            return false;
        }
    }

    private void logLoaders() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        log("DexedrineApp:  Thread.currentThread().getContextClassLoader() is, %s", loader);
        log("DexedrineApp:  getClass().getClassLoader() is, %s", getClass().getClassLoader());
        log("DexedrineApp:  getClassLoader() is, %s", getClassLoader());
    }

    private void patchApkLoader() {
        Field mLoadedApk = getField(Application.class, "mLoadedApk");

        try {
            Object apk = mLoadedApk.get(this);
            Field field_class_loader = getField(apk.getClass(), "mClassLoader");
            log("About to set mClassLoader on the application.mLoadedApk: %s", mClassLoader);
            field_class_loader.set(apk, mClassLoader);
            assert field_class_loader.get(apk) == mClassLoader;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This bad boy just does some good old logging ;)
     *
     * @param fmt  The format string to use all the
     * @param args parameters
     */
    public void log(String fmt, Object... args) {
        Log.d("Dexedrine", String.format(fmt, args));
    }

    /**
     *
     * @param assetName The basename of an asset to look in the assets folder
     * @return true on successfully copying
     */
    private boolean copyAssetToFileDir(String assetName) {
        // Copy the given asset out into a file so that it can be installed.
        // Returns the path to the file.
        byte[] buffer = new byte[8192];
        InputStream is = null;
        FileOutputStream fout = null;
        boolean success = false;
        try {
            is = getAssets().open(assetName);
            log("InputStream from `getAssets().open(%s);` == %s", assetName, is);

            if (is == null) {
                throw new IOException("Couldn't open assetts");
            }

            fout = openFileOutput(assetName, Context.MODE_PRIVATE);
            int n;
            while ((n = is.read(buffer)) >= 0) {
                fout.write(buffer, 0, n);
            }
            success = true;
        } catch (IOException e) {
            log("Failed transferring %s", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ignored) {
                log("in stream.close() IOException %s", ignored.toString());
            }
            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (IOException ignored) {
                log("out stream.close() IOException %s", ignored.toString());
            }
        }
        return success;
    }

    private boolean loadDex(String libPath) {
        try {

            mClassLoader = createMyDexLoader(libPath);
            Thread.currentThread().setContextClassLoader(mClassLoader);
            log("pathLoader %s", mClassLoader);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private BaseDexClassLoader createMyDexLoader(String allLibsJarPath) {
        String apkPath = String.format(getPackageResourcePath());
//        enumerateDexFile(apkPath);
//        enumerateDexFile(allLibsJarPath);
        BaseDexClassLoader loader = new DexedrineLoader(

                // Paths
                new String[]{
                        apkPath,
                        allLibsJarPath,
                         /* Like the ClassPath.getSystemClassLoader()*/
                        "."},

                // Where to dump the odex files
                getCacheDir().getAbsolutePath(),
                // Native C libs
                null,
                ClassLoader.getSystemClassLoader().getParent()
        );
        log("Created loader %s", loader);
        return loader;
    }

    @SuppressWarnings("unused")
    private void enumerateDexFile(String dexFilePath) {
        try {
            DexFile df = DexFile.loadDex(dexFilePath, getCacheDir() + "dex2", 0);
            Enumeration<String> entries = df.entries();
            while (entries.hasMoreElements()) {
                String entry = entries.nextElement();
                log("Found class %s in file %s", entry, dexFilePath);
            }

        } catch (IOException e) {
            log("Problem was %s %s", dexFilePath, e.toString());
            throw new RuntimeException(e);
        }
    }

}