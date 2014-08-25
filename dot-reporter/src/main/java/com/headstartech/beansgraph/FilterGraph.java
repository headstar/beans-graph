package com.headstartech.beansgraph;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Graph utility
 */
class FilterGraph {

    /**
     * Filter the source graph, return a graph where every vertex matches the given filter.
     *
     * @param sourceGraph
     * @param filter
     * @return
     */
    static Graph<Bean, DefaultEdge> filterGraph(Graph<Bean, DefaultEdge> sourceGraph, BeanFilter filter) {
        if(filter == null) {
            return sourceGraph;
        }
        DirectedGraph<Bean, DefaultEdge> res = new DefaultDirectedGraph<Bean, DefaultEdge>(DefaultEdge.class);
        for(Bean bean : sourceGraph.vertexSet()) {
            if(filter.matches(bean)) {
                res.addVertex(bean);
            }
        }
        for(DefaultEdge edge : sourceGraph.edgeSet()) {
            Bean edgeSource = sourceGraph.getEdgeSource(edge);
            Bean edgeTarget = sourceGraph.getEdgeTarget(edge);
            if(res.containsVertex(edgeSource) && res.containsVertex(edgeTarget)) {
                res.addEdge(edgeSource, edgeTarget);
            }
        }
        return res;
    }
}
