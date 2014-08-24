package com.headstartech.beansgraph;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A reporter which outputs the result to a {@link PrintWriter}, like {@code System.out}.
 *
 * @author Per Johansson
 * @since 1.0
 */
public class ConsoleReporter implements BeansGraphListener {

    private final static int WIDTH = 140;

    public static Builder forSource(BeansGraphProducer source) {
        return new Builder(source);
    }

    public static class Builder {
        private final BeansGraphProducer source;
        private boolean ignoreCyclesOfLengthOne;
        private PrintWriter out;
        private BeanFilter filter;

        private Builder(BeansGraphProducer source) {
            this.source = source;
            withIgnoreCyclesOfLengthOne(true);
            withOutput(new PrintWriter(System.out));
        }

        public Builder withIgnoreCyclesOfLengthOne(boolean ignoreCyclesOfLengthOne) {
            this.ignoreCyclesOfLengthOne = ignoreCyclesOfLengthOne;
            return this;
        }

        public Builder withOutput(PrintWriter out) {
            this.out = out;
            return this;
        }

        public Builder filter(BeanFilter filter) {
            this.filter = filter;
            return this;
        }

        public ConsoleReporter build() {
            return new ConsoleReporter(source, out, ignoreCyclesOfLengthOne, filter);
        }
    }

    private final boolean ignoreCyclesOfLengthOne;
    private final PrintWriter out;
    private final BeanFilter filter;

    private ConsoleReporter(BeansGraphProducer source, PrintWriter out, boolean ignoreCyclesOfLengthOne, BeanFilter filter) {
        this.ignoreCyclesOfLengthOne = ignoreCyclesOfLengthOne;
        this.out = out;
        this.filter = filter;
        source.addListener(this);
    }

    @Override
    public void onBeanGraphResult(ApplicationContext applicationContext, BeansGraphResult result) {
        printBeanDependencies(applicationContext, result);
        out.println();
        printCycles(applicationContext, result);
    }

    private void printCycles(ApplicationContext applicationContext, BeansGraphResult result) {
        printSeparator();
        out.println("Circular dependencies in context " + StringUtils.quote(applicationContext.getDisplayName()));
        printSeparator();
        for (List<Bean> cycle : result.getCycles()) {
            if(ignoreCyclesOfLengthOne && cycle.size() == 1) {
                continue;
            }
            out.println("[" + formatVertices(cycle) + "]");
        }
        printSeparator();
        out.flush();
    }

    private void printBeanDependencies(ApplicationContext applicationContext, BeansGraphResult result) {
        printSeparator();
        out.println("Dependencies in context " + StringUtils.quote(applicationContext.getDisplayName()));
        printSeparator();
        Set<Bean> vertices = result.getDependencyGraph().vertexSet();
        UnmodifiableDirectedGraph<Bean, DefaultEdge> graph =  result.getDependencyGraph();
        for(Bean v : getOrderedVertexSet(vertices)) {
            if(filter == null || filter.matches(v)) {
                Collection<Bean> dependencies = getOrderedVertexSet(collectTargetVertices(graph, v));
                Collection<Bean> dependents = getOrderedVertexSet(collectSourceVertices(graph, v));
                out.format("%s: ->[%s], <-[%s]", v.getName(), formatVertices(dependencies), formatVertices(dependents));
                out.println();
            }
        }
        printSeparator();
        out.flush();
    }

    private Set<Bean> collectTargetVertices(UnmodifiableDirectedGraph<Bean, DefaultEdge> graph , Bean v) {
        Set<Bean> res = new HashSet<Bean>();
        for(DefaultEdge e : graph.outgoingEdgesOf(v)) {
            res.add(graph.getEdgeTarget(e));
        }
        return res;
    }

    private Set<Bean> collectSourceVertices(UnmodifiableDirectedGraph<Bean, DefaultEdge> graph , Bean v) {
        Set<Bean> res = new HashSet<Bean>();
        for(DefaultEdge e : graph.incomingEdgesOf(v)) {
            res.add(graph.getEdgeSource(e));
        }
        return res;
    }


    private String formatVertices(Collection<Bean> vertices) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Bean v : vertices) {
            if (!first) {
                sb.append(",");
            }
            sb.append(v.getName());
            first = false;
        }
        return sb.toString();
    }

    private void printSeparator() {
        for(int i=0; i<WIDTH; ++i) {
            out.print("-");
        }
        out.println();
    }

    private Set<Bean> getOrderedVertexSet(Set<Bean> vertices) {
        TreeSet<Bean> res = new TreeSet<Bean>(new BeanVertexComparator());
        res.addAll(vertices);
        return res;
    }

    private static class BeanVertexComparator implements Comparator<Bean> {

        @Override
        public int compare(Bean o1, Bean o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
