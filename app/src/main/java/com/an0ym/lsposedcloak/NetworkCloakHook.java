package com.an0ym.lsposedcloak;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class NetworkCloakHook {
    public static void install(XC_LoadPackage.LoadPackageParam lp) {
        try {
            Class<?> nc = XposedHelpers.findClass("android.net.NetworkCapabilities", lp.classLoader);
            XposedHelpers.findAndHookMethod(nc, "hasTransport", int.class, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    if ((int) param.args[0] == 4) param.setResult(false); // TRANSPORT_VPN
                }
            });
            XposedHelpers.findAndHookMethod(nc, "hasCapability", int.class, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    if ((int) param.args[0] == 15) param.setResult(true); // NET_CAPABILITY_NOT_VPN
                }
            });
            Class<?> lprop = XposedHelpers.findClass("android.net.LinkProperties", lp.classLoader);
            XposedBridge.hookAllMethods(lprop, "getInterfaceName", new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    String iface = (String) param.getResult();
                    if (iface != null && (iface.startsWith("tun") || iface.startsWith("ppp") || iface.startsWith("wg"))) param.setResult("wlan0");
                }
            });
            Logger.i("NetworkCloakHook installed");
        } catch (Throwable t) { Logger.e("NetworkCloakHook", t); }
    }
}
