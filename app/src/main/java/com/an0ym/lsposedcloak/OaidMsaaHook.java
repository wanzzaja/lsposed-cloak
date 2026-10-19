package com.an0ym.lsposedcloak;

import java.util.Random;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class OaidMsaaHook {
    private static final Random RNG = new Random();
    private static final String[] KNOWN_INTERFACES = {
        "com.bun.miitmdid.interfaces.IdSupplier",
        "com.bun.miitmdid.interfaces.IIdSupplier",
        "com.asus.msa.SupplementaryDID.ISupplementaryDID",
    };

    public static void install(XC_LoadPackage.LoadPackageParam lp) {
        boolean hooked = false;
        for (String name : KNOWN_INTERFACES) {
            try {
                Class<?> cls = XposedHelpers.findClassIfExists(name, lp.classLoader);
                if (cls == null) continue;
                hookIdSupplier(cls);
                hooked = true;
                Logger.i("OaidMsaaHook: hooked " + name);
                break;
            } catch (Throwable t) {}
        }
        if (!hooked) Logger.i("OaidMsaaHook: SharedPreferences fallback");
        hookSharedPreferences(lp);
    }

    private static void hookIdSupplier(Class<?> cls) {
        String[] names = {"getOAID", "getVAID", "getAAID", "getUDID"};
        for (String m : names) {
            try {
                XposedBridge.hookAllMethods(cls, m, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) { return randomId32(); }
                });
            } catch (Throwable ignored) {}
        }
    }

    private static void hookSharedPreferences(XC_LoadPackage.LoadPackageParam lp) {
        try {
            Class<?> sp = XposedHelpers.findClassIfExists("android.app.SharedPreferencesImpl", lp.classLoader);
            if (sp == null) return;
            XposedBridge.hookAllMethods(sp, "getString", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (param.args.length < 1 || !(param.args[0] instanceof String)) return;
                    String k = ((String) param.args[0]).toLowerCase();
                    if ((k.contains("oaid") || k.contains("vaid") || k.contains("aaid") || k.contains("msa_")) && param.getResult() != null) {
                        param.setResult(randomId32());
                    }
                }
            });
        } catch (Throwable t) {}
    }

    private static String randomId32() {
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++) sb.append(Character.forDigit(RNG.nextInt(16), 16));
        return sb.toString();
    }
}
