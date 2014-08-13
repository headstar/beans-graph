package org.headstar.beangraph;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Add to a bean init method where a manual "wiring" is done. If this annotation isn't used, those dependencies will be missed by the bean graph producer.
 *
 * Example:
 *
 * <pre>
*  <code>
 * private Foo foo;
 *
 * {@literal@}PostConstruct
 * {@literal@}ManuallyWired(beanNames = {"foo"})
 * public void init() {
 *     foo = applicationContext.getBean("foo").
 * }
 * </code>
 * </pre>
 * @see org.headstar.beangraph.BeanGraphProducer
 * @author Per Johansson
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ManuallyWired {

    String[] beanNames() default {};
}
