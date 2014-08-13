package org.headstar.beangraph;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Add this annotation to an {@code @Configuration} class to have the
 * configuration defined in {@link org.headstar.beangraph.BeanGraphConfiguration} imported.
 *
 *
 * @author Per Johansson
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(BeanGraphConfiguration.class)
public @interface EnableBeanGraph {
}
