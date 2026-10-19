package com.an0ym.lsposedcloak;

import java.io.File;
import java.io.FileNotFoundException;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class SysfsFilterHook {
    private static final String[] HIDDEN_PREFIXES = {
        "/sys/class/camera", "/sys/class/kgsl", "/sys/devices/virtual/kgsl",
        "/sys/class/thermal", "/sys/devices/soc0/serial_number", "/sys/devices/soc0/machine",
    };

    public static void install(XC_LoadPackage.LoadPackageParam lp) {
        try {
            Class<?> fis = XposedHelpers.findClassIfExists("java.io.FileInputStream", lp.classLoader);
            if (fis != null) {
                XposedBridge.hookAllConstructors(fis, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String path = null;
                        if (param.args.length > 0) {
                            if (param.args[0] instanceof String) path = (String) param.args[0];
                            else if (param.args[0] instanceof File) path = ((File) param.args[0]).getAbsolutePath();
                        }
                        if (path != null && isHidden(path)) param.setThrowable(new FileNotFoundException(path));
                    }
                });
            }
            Class<?> fileCls = XposedHelpers.findClassIfExists("java.io.File", lp.classLoader);
            if (fileCls != null) {
                XposedBridge.hookAllMethods(fileCls, "exists", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (isHidden(((File) param.thisObject).getAbsolutePath())) param.setResult(false);
                    }
                });
            }
            Logger.i("SysfsFilterHook installed");
        } catch (Throwable t) { Logger.e("SysfsFilterHook", t); }
    }

    private static boolean isHidden(String path) {
        if (path == null) return false;
        for (String p : HIDDEN_PREFIXES) if (path.startsWith(p)) return true;
        return false;
    }
}
