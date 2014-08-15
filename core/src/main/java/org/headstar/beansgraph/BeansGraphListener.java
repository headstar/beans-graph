package org.headstar.beansgraph;

import org.springframework.context.ApplicationContext;

/**
 * Listeners for events from the registry.  Listeners must be thread-safe.
 *
 * @see BeansGraphResult
 * @author Per Johansson
 * @since 1.0
 */
public interface BeansGraphListener {

    void onBeanGraphResult(ApplicationContext applicationContext, BeansGraphResult result);
}
