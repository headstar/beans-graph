package org.headstar.beangraph;

import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.List;

public class ConsoleReporter implements BeanGraphListener {

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
        System.out.println("Circular dependencies in context " + StringUtils.quote(applicationContext.getDisplayName()));
        System.out.println("--------------------------------------------------------------");
        boolean foundOne = false;
        for (List<BeanGraphVertex> cycle : result.getCycles()) {
            if(ignoreCyclesOfLengthOne && cycle.size() == 1) {
                continue;
            }
            foundOne = true;
            System.out.println("[" + formatCycle(cycle) + "]");
        }
        if(!foundOne) {
            System.out.println("No circular dependency found in context " + StringUtils.quote(applicationContext.getDisplayName()));
        }
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
}
