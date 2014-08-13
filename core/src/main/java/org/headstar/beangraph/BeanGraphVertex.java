package org.headstar.beangraph;

/**
 * Vertex in the produced bean graph.
 *
 * @author Per Johansson
 * @since 1.0
 */
public class BeanGraphVertex {
    private final String name;

    BeanGraphVertex(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BeanGraphVertex that = (BeanGraphVertex) o;

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
