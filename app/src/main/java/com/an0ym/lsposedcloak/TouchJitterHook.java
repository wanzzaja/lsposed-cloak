package com.an0ym.lsposedcloak;
import android.view.MotionEvent;
import java.util.Random;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class TouchJitterHook {
    private static final Random RNG = new Random();
    private static final float JITTER_PX = 1.5f;

    public static void install(XC_LoadPackage.LoadPackageParam lp) {
        try {
            Class<?> viewRoot = XposedHelpers.findClassIfExists("android.view.ViewRootImpl", lp.classLoader);
            if (viewRoot != null) {
                XposedBridge.hookAllMethods(viewRoot, "dispatchInputEvent", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (param.args.length > 0 && param.args[0] instanceof MotionEvent) {
                            MotionEvent ev = (MotionEvent) param.args[0];
                            if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
                                float dx = (RNG.nextFloat() - 0.5f) * JITTER_PX;
                                float dy = (RNG.nextFloat() - 0.5f) * JITTER_PX;
                                ev.offsetLocation(dx, dy);
                            }
                        }
                    }
                });
            }
            Logger.i("TouchJitterHook installed");
        } catch (Throwable t) { Logger.e("TouchJitterHook", t); }
    }
}
