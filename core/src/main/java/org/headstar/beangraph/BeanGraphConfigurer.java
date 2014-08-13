package org.headstar.beangraph;

/**
 * Defines callback methods to customize the Java-based configuration
 * for Bean Graph enabled via {@link org.headstar.beangraph.EnableBeanGraph @EnableBeanGraph}.
 *
 * @see org.headstar.beangraph.EnableBeanGraph
 * @see org.headstar.beangraph.EnableBeanGraph
 * @author Per Johansson
 * @since 1.0
 */
public interface BeanGraphConfigurer {

    void configureReporters(BeanGraphProducer graphSource);

}
