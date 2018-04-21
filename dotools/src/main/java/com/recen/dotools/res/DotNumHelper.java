package com.recen.dotools.res;

/**
 * Created by Recen on 2018/4/21.
 */

public class DotNumHelper {
    /**
     * 获取数值的位数，例如9返回1，99返回2，999返回3
     *
     * @param number 要计算位数的数值，必须>0
     * @return 数值的位数，若传的参数小于等于0，则返回0
     */
    public static int getNumberDigits(int number) {
        if (number <= 0) return 0;
        return (int) (Math.log10(number) + 1);
    }
}
