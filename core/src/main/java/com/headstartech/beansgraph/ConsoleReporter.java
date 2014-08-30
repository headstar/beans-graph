package com.headstartech.beansgraph;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.util.*;

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
            ignoreCyclesOfLengthOne(true);
            toOutput(new PrintWriter(System.out));
        }

        /**
         * Ignore object referring to themselves when reporting dependency cycles.
         *
         * @param ignoreCyclesOfLengthOne {@code true} if cycles of length 1 should be ignored
         * @return {@code this}
         */
        public Builder ignoreCyclesOfLengthOne(boolean ignoreCyclesOfLengthOne) {
            this.ignoreCyclesOfLengthOne = ignoreCyclesOfLengthOne;
            return this;
        }

        /**
         * Write to the given {@link java.io.PrintWriter}.
         *
         * @param out a {@link java.io.PrintWriter} instance.
         * @return {@code this}
         */
        public Builder toOutput(PrintWriter out) {
            this.out = out;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link BeanFilter}
         * @return {@code this}
         */
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
        out.println("Circular dependencies in context " + StringUtils.quote(applicationContext.getDisplayName()));
        printSeparator();
        for (List<Bean> cycle : result.getCycles()) {
            if(ignoreCyclesOfLengthOne && cycle.size() == 1) {
                continue;
            }
            out.println("[" + formatVertices(cycle) + "]");
        }
        out.flush();
    }

    private void printBeanDependencies(ApplicationContext applicationContext, BeansGraphResult result) {
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
