package org.headstar.beangraph;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;
import org.springframework.context.ApplicationContext;

import java.util.List;

public interface DependencyGraphSourceListener {

    void onDependencyGraph(ApplicationContext applicationContext, UnmodifiableDirectedGraph<BeanVertex, DefaultEdge> dependencyGraph, List<List<BeanVertex>> cycles);
}
