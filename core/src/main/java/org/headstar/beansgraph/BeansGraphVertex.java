package org.headstar.beansgraph;

/**
 * Vertex in the produced bean graph.
 *
 * @author Per Johansson
 * @since 1.0
 */
public class BeansGraphVertex {
    private final String name;

    BeansGraphVertex(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BeansGraphVertex that = (BeansGraphVertex) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BeanGraphVertex [");
        sb.append("id='").append(name).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
