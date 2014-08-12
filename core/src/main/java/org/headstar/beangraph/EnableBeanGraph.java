package org.headstar.beangraph;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(BeanGraphConfiguration.class)
public @interface EnableBeanGraph {
}
