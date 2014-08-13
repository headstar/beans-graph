package org.headstar.beangraph;

import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Prints all cyclic dependencies on standard out.
 *
 * @author Per Johansson
 * @since 1.0
 */
public class ConsoleReporter implements BeanGraphListener {

    private final static int WIDTH = 80;

    public static Builder forSource(BeanGraphProducer source) {
        return new Builder(source);
    }

    public static class Builder {
        private final BeanGraphProducer source;
        private boolean ignoreCyclesOfLengthOne;

        private Builder(BeanGraphProducer source) {
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

    ConsoleReporter(BeanGraphProducer source, boolean ignoreCyclesOfLengthOne) {
        this.ignoreCyclesOfLengthOne = ignoreCyclesOfLengthOne;
        source.addListener(this);
    }

    @Override
    public void onBeanGraphResult(ApplicationContext applicationContext, BeanGraphResult result) {
        printSeparator();
        System.out.println("Circular dependencies in context " + StringUtils.quote(applicationContext.getDisplayName()));
        printSeparator();
        int counter = 0;
        for (List<BeanGraphVertex> cycle : result.getCycles()) {
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

    private String formatCycle(List<BeanGraphVertex> cycles) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (BeanGraphVertex v : cycles) {
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
