package org.headstar.beansgraph;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;

import java.util.List;

/**
 * Keeps the result produced by {@link BeansGraphProducer}.
 *
 * @see BeansGraphProducer
 * @author Per Johansson
 * @since 1.0
 */
public class BeansGraphResult {

    private final UnmodifiableDirectedGraph<BeansGraphVertex, DefaultEdge> dependencyGraph;
    private final List<List<BeansGraphVertex>> cycles;

    public BeansGraphResult(UnmodifiableDirectedGraph<BeansGraphVertex, DefaultEdge> dependencyGraph, List<List<BeansGraphVertex>> cycles) {
        this.dependencyGraph = dependencyGraph;
        this.cycles = cycles;
    }

    public List<List<BeansGraphVertex>> getCycles() {
        return cycles;
    }

    public UnmodifiableDirectedGraph<BeansGraphVertex, DefaultEdge> getDependencyGraph() {
        return dependencyGraph;
    }
}
