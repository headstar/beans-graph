package org.headstar.beansgraph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the class imported by {@link EnableBeansGraph @EnableBeanGraph}.
 *
 * @see org.headstar.beansgraph.BeanGraphConfigurer
 * @see BeansGraphProducer
 * @author Per Johansson
 * @since 1.0
 */
@Configuration
public class BeansGraphConfiguration {

    private final List<BeanGraphConfigurer> configurers = new ArrayList<BeanGraphConfigurer>();
    private BeansGraphProducer graphSource;

    @Autowired(required = false)
    public void setDependencyConfigurers(final List<BeanGraphConfigurer> configurers) {
        if (configurers != null) {
            this.configurers.addAll(configurers);
        }
    }

    @Bean
    public BeansGraphProducer beansGraphProducer() {
        if (graphSource == null) {
            graphSource = new BeansGraphProducer();
            for (BeanGraphConfigurer configurer : this.configurers) {
                configurer.configureReporters(graphSource);
            }
        }
        return graphSource;
    }
}
