package org.headstar.beansgraph;

import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Prints all cyclic dependencies on standard out.
 *
 * @author Per Johansson
 * @since 1.0
 */
public class ConsoleReporter implements BeansGraphListener {

    private final static int WIDTH = 120;

    public static Builder forSource(BeansGraphProducer source) {
        return new Builder(source);
    }

    public static class Builder {
        private final BeansGraphProducer source;
        private boolean ignoreCyclesOfLengthOne;

        private Builder(BeansGraphProducer source) {
            this.source = source;
            withIgnoreCyclesOfLengthOne(true);
        }

        public Builder withIgnoreCyclesOfLengthOne(boolean ignoreCyclesOfLengthOne) {
            this.ignoreCyclesOfLengthOne = ignoreCyclesOfLengthOne;
            return this;
        }

        public ConsoleReporter build() {
            return new ConsoleReporter(source, ignoreCyclesOfLengthOne);
        }
    }

    private final boolean ignoreCyclesOfLengthOne;

    ConsoleReporter(BeansGraphProducer source, boolean ignoreCyclesOfLengthOne) {
        this.ignoreCyclesOfLengthOne = ignoreCyclesOfLengthOne;
        source.addListener(this);
    }

    @Override
    public void onBeanGraphResult(ApplicationContext applicationContext, BeansGraphResult result) {
        printSeparator();
        System.out.println("Circular dependencies in context " + StringUtils.quote(applicationContext.getDisplayName()));
        printSeparator();
        int counter = 0;
        for (List<BeansGraphVertex> cycle : result.getCycles()) {
            if(ignoreCyclesOfLengthOne && cycle.size() == 1) {
                continue;
            }
            ++counter;
            System.out.println("[" + formatCycle(cycle) + "]");
        }
        if(counter > 0) {
            printSeparator();
        }
        System.out.println("Number of circular dependencies found: " + counter);
        printSeparator();
    }

    private String formatCycle(List<BeansGraphVertex> cycles) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (BeansGraphVertex v : cycles) {
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
            System.out.print("-");
        }
        System.out.println();
    }
}
