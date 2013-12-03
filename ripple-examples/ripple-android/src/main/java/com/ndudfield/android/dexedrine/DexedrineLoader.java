package com.ndudfield.android.dexedrine;

import android.util.Log;
import dalvik.system.DexClassLoader;

public class DexedrineLoader extends DexClassLoader {

    /**
     * @param dexPaths array of absolute paths to apks/jars/dex files
     * @param optimizedDirectory abs path to folder (req +w permissions) to dump odex files
     * @param libraryPath abs path to folder containing native extensions
     * @param parentLoader parent ClassLoader
     */
    public DexedrineLoader(String[] dexPaths, String optimizedDirectory, String libraryPath, ClassLoader parentLoader) {
        super(strJoin(dexPaths, ":"),
                optimizedDirectory,
                libraryPath,
                parentLoader
        );
    }

    /**
     * We only overriding this for logging purposes
     *
     * @see ClassLoader#findClass
     *
     * @param name of the class to look for
     * @return the found class or it
     * @throws ClassNotFoundException
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            Log.d("Dexedrine", String.format("DexedrineLoader failed to find %s", name ));
            throw e;
        }
    }

    /**
     * sSep.join(aArr)
     *
     * @param aArr strings to be joined with
     * @param sSep so can
     * @return them all joined
     */
    public static String strJoin(String[] aArr, String sSep) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = aArr.length; i < il; i++) {
            if (i > 0)
                sbStr.append(sSep);
            sbStr.append(aArr[i]);
        }
        return sbStr.toString();
    }
}