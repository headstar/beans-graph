package com.headstartech.beansgraph;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;


/**
 * Created by per on 8/25/14.
 */
public class FilterGraphTest {

    @Test
    public void testNullFilter() {
        // given
        DirectedGraph<Bean, DefaultEdge> source = new DefaultDirectedGraph<Bean, DefaultEdge>(DefaultEdge.class);

        // when
        Graph<Bean, DefaultEdge> res  = FilterGraph.filterGraph(source, null);
        
        // then
        assertSame(source, res);
    }

    @Test
    public void testSimpleGraph() {
        // given
        Bean bean1 = new Bean("bean1");
        bean1.setClassName("com.foo.xyz");
        Bean bean2 = new Bean("bean2");
        bean2.setClassName("com.foo.xyz");
        Bean bean3 = new Bean("bean3");
        bean3.setClassName("com.bar.xyz");
        Bean bean4 = new Bean("bean4");
        bean4.setClassName("com.bar.xyz");

        DirectedGraph<Bean, DefaultEdge> source = new DefaultDirectedGraph<Bean, DefaultEdge>(DefaultEdge.class);
        source.addVertex(bean1);
        source.addVertex(bean2);
        source.addVertex(bean3);
        source.addVertex(bean4);
        DefaultEdge bean12 = source.addEdge(bean1, bean2);
        source.addEdge(bean2, bean3);
        source.addEdge(bean3, bean4);

        // when
        Graph<Bean, DefaultEdge> res  = FilterGraph.filterGraph(source, new ClassNameFilter("com.foo"));

        // then
        assertEquals(2, res.vertexSet().size());
        assertTrue(res.vertexSet().contains(bean1));
        assertTrue(res.vertexSet().contains(bean2));
        assertEquals(1, res.edgeSet().size());
        DefaultEdge e1 = res.edgeSet().iterator().next();
        assertEquals(bean1, res.getEdgeSource(e1));
        assertEquals(bean2, res.getEdgeTarget(e1));
    }
}
