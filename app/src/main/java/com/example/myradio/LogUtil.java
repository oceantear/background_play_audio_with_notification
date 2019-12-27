package com.example.myradio;

import android.util.Log;

/**
 * Created by YunKai-Ke(柯紜凱) on 2017/4/5.
 */

public class LogUtil {
    public static final boolean DBG_LOG = BuildConfig.DEBUG;
    public static final boolean LOG_ENABLE = true; // true顯示LOG, false不顯示LOG
    public static final boolean DETAIL_ENABLE = true; // 顯示LOG詳細資訊

    private LogUtil() {
    }

    private static String buildMsg(String msg) {
        StringBuilder buffer = new StringBuilder();

        if (DETAIL_ENABLE) {
            final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];

            buffer.append("[ ");
            buffer.append(Thread.currentThread().getName());
            buffer.append(": ");
            buffer.append(stackTraceElement.getFileName());
            buffer.append(": ");
            buffer.append(stackTraceElement.getLineNumber());
            buffer.append(": ");
            buffer.append(stackTraceElement.getMethodName());
        }

        buffer.append("() ] __");
        buffer.append(msg);
        return buffer.toString();
    }

    public static void v(Class<?> c, String msg) {
        if (LOG_ENABLE) {
            Log.v(c.getName(), buildMsg(msg));
        }
    }

    public static void d(String c, String msg) {
        if (LOG_ENABLE) {
            Log.d(c, buildMsg(msg));
        }
    }

    public static void d(Class<?> c, String msg) {
        if (LOG_ENABLE) {
            Log.d(c.getName(), buildMsg(msg));
        }
    }

    public static void i(Class<?> c, String msg) {
        if (LOG_ENABLE) {
            Log.i(c.getName(), buildMsg(msg));
        }
    }

    public static void i(String c, String msg) {
        if (LOG_ENABLE) {
            Log.i(c, buildMsg(msg));
        }
    }

    public static void w(Class<?> c, String msg) {
        if (LOG_ENABLE) {
            Log.w(c.getName(), buildMsg(msg));
        }
    }

    public static void w(Class<?> c, String msg, Exception e) {
        if (LOG_ENABLE) {
            Log.w(c.getName(), buildMsg(msg), e);
        }
    }

    public static void w(String c, String msg) {
        if (LOG_ENABLE) {
            Log.w(c, buildMsg(msg));
        }
    }

    public static void e(Class<?> c, String msg) {
        if (LOG_ENABLE) {
            Log.e(c.getName(), buildMsg(msg));
        }
    }

    public static void e(String c, String msg) {
        if (LOG_ENABLE) {
            Log.e(c, buildMsg(msg));
        }
    }

    public static void e(Class<?> c, String msg, Exception e) {
        if (LOG_ENABLE) {
            Log.e(c.getName(), buildMsg(msg), e);
        }
    }

    public static void e(String c, String msg, Exception e) {
        if (LOG_ENABLE) {
            Log.e(c, buildMsg(msg), e);
        }
    }
}