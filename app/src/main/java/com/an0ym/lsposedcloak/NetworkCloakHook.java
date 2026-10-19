package com.an0ym.lsposedcloak;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class NetworkCloakHook {
    public static void install(XC_LoadPackage.LoadPackageParam lp) {
        hookNetworkCapabilities(lp);
        hookLinkProperties(lp);
    }

    private static void hookNetworkCapabilities(XC_LoadPackage.LoadPackageParam lp) {
        try {
            Class<?> cls = XposedHelpers.findClass("android.net.NetworkCapabilities", lp.classLoader);
            XposedHelpers.findAndHookMethod(cls, "hasTransport", int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if ((int) param.args[0] == 4) param.setResult(false);
                }
            });
            XposedHelpers.findAndHookMethod(cls, "hasCapability", int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if ((int) param.args[0] == 15) param.setResult(true);
                }
            });
            Logger.i("NetworkCloakHook: NetworkCapabilities hooked");
        } catch (Throwable ignored) {}
    }

    private static void hookLinkProperties(XC_LoadPackage.LoadPackageParam lp) {
        try {
            Class<?> cls = XposedHelpers.findClass("android.net.LinkProperties", lp.classLoader);
            XposedBridge.hookAllMethods(cls, "getInterfaceName", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    String iface = (String) param.getResult();
                    if (iface != null && (iface.startsWith("tun") || iface.startsWith("ppp") || iface.startsWith("wg"))) {
                        param.setResult("wlan0");
                    }
                }
            });
            Logger.i("NetworkCloakHook: LinkProperties hooked");
        } catch (Throwable ignored) {}
    }
}
