package com.an0ym.lsposedcloak;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class MockLocationHook {
    public static void install(XC_LoadPackage.LoadPackageParam lp) {
        try {
            Class<?> cls = XposedHelpers.findClass("android.location.Location", lp.classLoader);
            XposedBridge.hookAllMethods(cls, "isFromMockProvider", new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) { param.setResult(false); }
            });
            XposedBridge.hookAllMethods(cls, "isMock", new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) { param.setResult(false); }
            });
            Logger.i("MockLocationHook installed");
        } catch (Throwable t) { Logger.e("MockLocationHook", t); }
    }
}
