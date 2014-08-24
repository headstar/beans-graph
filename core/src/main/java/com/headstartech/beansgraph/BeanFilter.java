package com.headstartech.beansgraph;

/**
 * Created by per on 8/22/14.
 */
public interface BeanFilter {

    /**
     * Returns {@code true} if the metric matches the filter; {@code false} otherwise.
     *
     * @param name the bean
     * @return {@code true} if the bean matches the filter
     */
    boolean matches(Bean bean);
}
