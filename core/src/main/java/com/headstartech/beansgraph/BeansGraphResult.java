package com.headstartech.beansgraph;

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

    private final UnmodifiableDirectedGraph<Bean, DefaultEdge> dependencyGraph;
    private final List<List<Bean>> cycles;

    public BeansGraphResult(UnmodifiableDirectedGraph<Bean, DefaultEdge> dependencyGraph, List<List<Bean>> cycles) {
        this.dependencyGraph = dependencyGraph;
        this.cycles = cycles;
    }

    public List<List<Bean>> getCycles() {
        return cycles;
    }

    public UnmodifiableDirectedGraph<Bean, DefaultEdge> getDependencyGraph() {
        return dependencyGraph;
    }
}
