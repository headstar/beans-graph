package org.headstar.beangraph;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;

import java.util.List;

public class BeanGraphResult {

    private final UnmodifiableDirectedGraph<BeanGraphVertex, DefaultEdge> dependencies;
    private final List<List<BeanGraphVertex>> cycles;

    public BeanGraphResult(UnmodifiableDirectedGraph<BeanGraphVertex, DefaultEdge> dependencies, List<List<BeanGraphVertex>> cycles) {
        this.dependencies = dependencies;
        this.cycles = cycles;
    }

    public List<List<BeanGraphVertex>> getCycles() {
        return cycles;
    }

    public UnmodifiableDirectedGraph<BeanGraphVertex, DefaultEdge> getDependencies() {
        return dependencies;
    }
}
