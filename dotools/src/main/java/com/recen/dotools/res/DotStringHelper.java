package com.recen.dotools.res;

import android.support.annotation.Nullable;

/**
 * Created by Recen on 2018/4/21.
 */

public class DotStringHelper {
    public static boolean isNotNullOrEmpty(@Nullable CharSequence string) {
        return !isNullOrEmpty(string);
    }
    public static boolean isNullOrEmpty(@Nullable CharSequence string) {
        return string == null || string.length() == 0;
    }
}
