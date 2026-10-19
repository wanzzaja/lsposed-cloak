package com.an0ym.lsposedcloak;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String SHOPEE = "com.shopee.id";
    private static final String TIKTOK = "com.ss.android.ugc.trill";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp) {
        if (lp == null || lp.packageName == null) return;
        if (!lp.packageName.equals(SHOPEE) && !lp.packageName.equals(TIKTOK)) return;

        Logger.i("hooking " + lp.packageName);

        safe(() -> TouchJitterHook.install(lp));
        safe(() -> SensorBiasHook.install(lp));
        safe(() -> NetworkCloakHook.install(lp));
        safe(() -> MockLocationHook.install(lp));
        
        safe(() -> CameraSerialHook.install(lp));
        safe(() -> OaidMsaaHook.install(lp));
        safe(() -> GpuStringHook.install(lp));
        safe(() -> SysfsFilterHook.install(lp));
    }

    private static void safe(Runnable r) {
        try { r.run(); } catch (Throwable t) { Logger.e("hook install fail", t); }
    }
}
