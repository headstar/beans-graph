package com.headstartech.beansgraph;

/**
 * Vertex in the produced beans graph.
 *
 * @author Per Johansson
 * @since 1.0
 */
public class Bean {
    private final String name;
    private String className;

    Bean(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bean that = (Bean) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Bean [");
        sb.append("id='").append(name).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
