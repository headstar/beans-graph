package org.headstartech.beansgraph;

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
        private Pattern classNamePattern;

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

        public Builder withClassNamePattern(Pattern classNamePattern) {
            this.classNamePattern = classNamePattern;
            return this;
        }

        public ConsoleReporter build() {
            return new ConsoleReporter(source, out, ignoreCyclesOfLengthOne, classNamePattern);
        }
    }

    private final boolean ignoreCyclesOfLengthOne;
    private final PrintWriter out;
    private final Pattern classNamePattern;

    private ConsoleReporter(BeansGraphProducer source, PrintWriter out, boolean ignoreCyclesOfLengthOne, Pattern classNamePattern) {
        this.ignoreCyclesOfLengthOne = ignoreCyclesOfLengthOne;
        this.out = out;
        this.classNamePattern = classNamePattern;
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
        for (List<BeansGraphVertex> cycle : result.getCycles()) {
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
        Set<BeansGraphVertex> vertices = result.getDependencyGraph().vertexSet();
        UnmodifiableDirectedGraph<BeansGraphVertex, DefaultEdge> graph =  result.getDependencyGraph();
        for(BeansGraphVertex v : getOrderedVertexSet(vertices)) {
            if(FilterUtil.beanClassMatches(v, classNamePattern)) {
                Collection<BeansGraphVertex> dependencies = getOrderedVertexSet(collectTargetVertices(graph, v));
                Collection<BeansGraphVertex> dependents = getOrderedVertexSet(collectSourceVertices(graph, v));
                out.format("%s: ->[%s], <-[%s]", v.getName(), formatVertices(dependencies), formatVertices(dependents));
                out.println();
            }
        }
        printSeparator();
        out.flush();
    }

    private Set<BeansGraphVertex> collectTargetVertices(UnmodifiableDirectedGraph<BeansGraphVertex, DefaultEdge> graph , BeansGraphVertex v) {
        Set<BeansGraphVertex> res = new HashSet<BeansGraphVertex>();
        for(DefaultEdge e : graph.outgoingEdgesOf(v)) {
            res.add(graph.getEdgeTarget(e));
        }
        return res;
    }

    private Set<BeansGraphVertex> collectSourceVertices(UnmodifiableDirectedGraph<BeansGraphVertex, DefaultEdge> graph , BeansGraphVertex v) {
        Set<BeansGraphVertex> res = new HashSet<BeansGraphVertex>();
        for(DefaultEdge e : graph.incomingEdgesOf(v)) {
            res.add(graph.getEdgeSource(e));
        }
        return res;
    }


    private String formatVertices(Collection<BeansGraphVertex> vertices) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (BeansGraphVertex v : vertices) {
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

    private Set<BeansGraphVertex> getOrderedVertexSet(Set<BeansGraphVertex> vertices) {
        TreeSet<BeansGraphVertex> res = new TreeSet<BeansGraphVertex>(new BeanVertexComparator());
        res.addAll(vertices);
        return res;
    }

    private static class BeanVertexComparator implements Comparator<BeansGraphVertex> {

        @Override
        public int compare(BeansGraphVertex o1, BeansGraphVertex o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
