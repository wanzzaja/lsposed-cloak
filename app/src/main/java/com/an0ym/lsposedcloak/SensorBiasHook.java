package com.an0ym.lsposedcloak;
import java.util.Random;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class SensorBiasHook {
    private static final Random RNG = new Random();
    private static final float B_X = (RNG.nextFloat() - 0.5f) * 0.05f;
    private static final float B_Y = (RNG.nextFloat() - 0.5f) * 0.05f;
    private static final float B_Z = (RNG.nextFloat() - 0.5f) * 0.05f;

    public static void install(XC_LoadPackage.LoadPackageParam lp) {
        try {
            XposedHelpers.findAndHookMethod("android.hardware.SystemSensorManager$SensorEventQueue", lp.classLoader, "dispatchSensorEvent",
                int.class, float[].class, int.class, long.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        float[] values = (float[]) param.args[1];
                        if (values == null || values.length != 3) return;
                        values[0] = values[0] + B_X + (RNG.nextFloat() - 0.5f) * 0.002f;
                        values[1] = values[1] + B_Y + (RNG.nextFloat() - 0.5f) * 0.002f;
                        values[2] = values[2] + B_Z + (RNG.nextFloat() - 0.5f) * 0.002f;
                    }
                });
            Logger.i("SensorBiasHook installed");
        } catch (Throwable t) { Logger.e("SensorBiasHook", t); }
    }
}
