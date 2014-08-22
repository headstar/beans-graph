package com.headstartech.beansgraph;

import java.util.regex.Pattern;

/**
 * Created by per on 8/18/14.
 */
public class FilterUtil {

    private FilterUtil() {}

    public static boolean beanClassMatches(Bean v, Pattern p) {
        return v.getClassName() == null || p == null || p.matcher(v.getClassName()).matches();
    }
}
