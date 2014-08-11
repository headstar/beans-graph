package org.headstar.beangraph;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;

import java.util.List;

public class DependencyGraphResult {

    private final UnmodifiableDirectedGraph<BeanVertex, DefaultEdge> dependencies;
    private final List<List<BeanVertex>> cycles;

    public DependencyGraphResult(UnmodifiableDirectedGraph<BeanVertex, DefaultEdge> dependencies, List<List<BeanVertex>> cycles) {
        this.dependencies = dependencies;
        this.cycles = cycles;
    }

    public List<List<BeanVertex>> getCycles() {
        return cycles;
    }

    public UnmodifiableDirectedGraph<BeanVertex, DefaultEdge> getDependencies() {
        return dependencies;
    }
}
