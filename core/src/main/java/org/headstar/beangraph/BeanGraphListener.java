package org.headstar.beangraph;

import org.springframework.context.ApplicationContext;

/**
 * Listeners for events from the registry.  Listeners must be thread-safe.
 *
 * @see org.headstar.beangraph.BeanGraphResult
 * @author Per Johansson
 * @since 1.0
 */
public interface BeanGraphListener {

    void onBeanGraphResult(ApplicationContext applicationContext, BeanGraphResult result);
}
