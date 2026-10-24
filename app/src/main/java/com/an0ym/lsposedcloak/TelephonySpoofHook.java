package com.an0ym.lsposedcloak;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class TelephonySpoofHook {
    private static final Random RNG = new Random();
    private static final int FAKE_LAC = 1234;
    private static final int FAKE_CID = 50000 + RNG.nextInt(5000);

    public static void install(XC_LoadPackage.LoadPackageParam lp) {
        try {
            Class<?> tmClass = XposedHelpers.findClass("android.telephony.TelephonyManager", lp.classLoader);
            XposedBridge.hookAllMethods(tmClass, "getAllCellInfo", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (param.getResult() == null) return;
                    List<CellInfo> infos = (List<CellInfo>) param.getResult();
                    List<CellInfo> spoofed = new ArrayList<>();
                    for (CellInfo info : infos) {
                        if (info instanceof CellInfoLte) spoofed.add(spoofLte((CellInfoLte) info));
                    }
                    if (!spoofed.isEmpty()) param.setResult(spoofed);
                }
            });
            Logger.i("TelephonySpoofHook installed");
        } catch (Throwable t) { Logger.e("TelephonySpoofHook", t); }
    }

    private static CellInfoLte spoofLte(CellInfoLte info) {
        try {
            Field identityField = CellInfoLte.class.getDeclaredField("mCellIdentityLte");
            identityField.setAccessible(true);
            Class<?>[] paramTypes = {int.class, int.class, int.class, int.class, int.class, String.class, String.class, int.class};
            Object[] params = {FAKE_CID, 0, 0, FAKE_LAC, 0, "510", "10", 0}; // MCC 510, MNC 10 (Telkomsel)
            java.lang.reflect.Constructor<CellIdentityLte> ctor = CellIdentityLte.class.getConstructor(paramTypes);
            CellIdentityLte fakeIdentity = ctor.newInstance(params);
            Field newField = CellInfoLte.class.getDeclaredField("mCellIdentityLte");
            newField.setAccessible(true);
            newField.set(info, fakeIdentity);
            return info;
        } catch (Throwable t) { return info; }
    }
}
