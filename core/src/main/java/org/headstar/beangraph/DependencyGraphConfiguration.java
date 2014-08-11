package org.headstar.beangraph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DependencyGraphConfiguration {

    private final List<DependencyGraphConfigurer> configurers = new ArrayList<DependencyGraphConfigurer>();
    private DependencyGraphSource graphSource;

    @Autowired(required = false)
    public void setDependencyConfigurers(final List<DependencyGraphConfigurer> configurers) {
        if (configurers != null) {
            this.configurers.addAll(configurers);
        }
    }

    @Bean
    public DependencyGraphSource dependencyGraphSource() {
        if (graphSource == null) {
            graphSource = new DependencyGraphSource();
            for (DependencyGraphConfigurer configurer : this.configurers) {
                configurer.configureReporters(graphSource);
            }
        }
        return graphSource;
    }
}
