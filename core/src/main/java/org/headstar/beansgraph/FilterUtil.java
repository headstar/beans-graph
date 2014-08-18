package org.headstar.beansgraph;

import java.util.regex.Pattern;

/**
 * Created by per on 8/18/14.
 */
public class FilterUtil {

    private FilterUtil() {}

    public static boolean beanClassMatches(BeansGraphVertex v, Pattern p) {
        return v.getBeanClassName() == null || p == null || p.matcher(v.getBeanClassName()).matches();
    }
}
