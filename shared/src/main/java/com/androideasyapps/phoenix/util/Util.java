package com.androideasyapps.phoenix.util;

/**
 * Created by seans on 21/12/14.
 */
public class Util {
    public static String nullIfEmpty(String s) {
        if (s==null||s.trim().length()==0) return null;
        return s;
    }

    public static int parseInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (Throwable t) {
            return 0;
        }
    }

    public static <T> T firstNonNull(T... s) {
        if (s==null||s.length==0) return null;
        for (T str: s) {
            if (str!=null && String.valueOf(str).trim().length()>0) return str;
        }
        return null;
    }

}
