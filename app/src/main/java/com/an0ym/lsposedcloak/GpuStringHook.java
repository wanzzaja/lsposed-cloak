package com.an0ym.lsposedcloak;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class GpuStringHook {
    private static final int GL_VENDOR = 0x1F00;
    private static final int GL_RENDERER = 0x1F01;
    private static final String VENDOR_FAKE = "Qualcomm";
    private static final String RENDERER_FAKE = "Adreno (TM) 650";

    public static void install(XC_LoadPackage.LoadPackageParam lp) {
        hook("android.opengl.GLES20", lp);
        hook("android.opengl.GLES30", lp);
    }

    private static void hook(String className, XC_LoadPackage.LoadPackageParam lp) {
        try {
            Class<?> cls = XposedHelpers.findClassIfExists(className, lp.classLoader);
            if (cls == null) return;
            XposedHelpers.findAndHookMethod(cls, "glGetString", int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    int name = (int) param.args[0];
                    if (name == GL_VENDOR) param.setResult(VENDOR_FAKE);
                    else if (name == GL_RENDERER) param.setResult(RENDERER_FAKE);
                }
            });
        } catch (Throwable ignored) {}
    }
}
