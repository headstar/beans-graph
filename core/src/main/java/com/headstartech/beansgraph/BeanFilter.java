package com.headstartech.beansgraph;

/**
 * A filter used to determine whether or not a bean should be reported.
 *
 * @author Per Johansson
 * @since 1.1
 */
public interface BeanFilter {

    /**
     * Returns {@code true} if the metric matches the filter; {@code false} otherwise.
     *
     * @param bean the bean
     * @return {@code true} if the bean matches the filter
     */
    boolean matches(Bean bean);
}
