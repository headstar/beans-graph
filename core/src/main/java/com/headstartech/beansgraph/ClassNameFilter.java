package com.headstartech.beansgraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Filters beans on the fully qualified class name. If the class name starts with given prefixes the filter returns <code>true</code>, otherwise <code>false</code>.
 *
 * @author Per Johansson
 * @since 1.1
 */
public class ClassNameFilter implements BeanFilter {

    Collection<String> prefixes = new ArrayList<String>();

    public ClassNameFilter(String... names) {
        prefixes.addAll(Arrays.asList(names));
    }


    @Override
    public boolean matches(Bean bean) {
        for(String s : prefixes) {
            if(bean.getClassName() != null && bean.getClassName().startsWith(s)) {
                return true;
            }
        }
        return false;
    }
}
