package com.an0ym.lsposedcloak;

import java.util.Random;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class CameraSerialHook {
    private static final Random RNG = new Random();
    private static final String FAKE = buildFake();

    public static void install(XC_LoadPackage.LoadPackageParam lp) {
        try {
            Class<?> cls = XposedHelpers.findClassIfExists("android.hardware.camera2.CameraCharacteristics", lp.classLoader);
            if (cls == null) return;
            XposedBridge.hookAllMethods(cls, "get", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (param.args.length < 1 || param.args[0] == null) return;
                    String keyName;
                    try { keyName = (String) XposedHelpers.callMethod(param.args[0], "getName"); } catch (Throwable t) { return; }
                    if (keyName != null && keyName.toLowerCase().contains("serial") && param.getResult() instanceof String) {
                        param.setResult(FAKE);
                    }
                }
            });
            Logger.i("CameraSerialHook installed");
        } catch (Throwable t) { Logger.e("CameraSerialHook", t); }
    }
    private static String buildFake() {
        StringBuilder sb = new StringBuilder(16);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 16; i++) sb.append(chars.charAt(RNG.nextInt(chars.length())));
        return sb.toString();
    }
}
