package com.headstartech.beansgraph;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Add this annotation to an {@code @Configuration} class to have the
 * configuration defined in {@link BeansGraphConfiguration} imported.
 *
 *
 * @author Per Johansson
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(BeansGraphConfiguration.class)
public @interface EnableBeansGraph {
}
