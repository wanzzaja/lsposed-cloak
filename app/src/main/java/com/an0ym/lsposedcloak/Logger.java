package com.an0ym.lsposedcloak;
import android.util.Log;
public final class Logger {
    private static final String TAG = "LSPCloak";
    public static void i(String msg) { try { Log.i(TAG, msg); } catch (Throwable ignored) {} }
    public static void e(String msg) { try { Log.e(TAG, msg); } catch (Throwable ignored) {} }
    public static void e(String msg, Throwable t) { try { Log.e(TAG, msg, t); } catch (Throwable ignored) {} }
}
