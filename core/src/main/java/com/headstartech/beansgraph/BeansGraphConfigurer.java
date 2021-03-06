package com.headstartech.beansgraph;

/**
 * Defines callback methods to customize the Java-based configuration
 * for Bean Graph enabled via {@link EnableBeansGraph @EnableBeanGraph}.
 *
 * @see EnableBeansGraph
 * @see EnableBeansGraph
 * @author Per Johansson
 * @since 1.0
 */
public interface BeansGraphConfigurer {

    void configureReporters(BeansGraphProducer graphSource);

}
