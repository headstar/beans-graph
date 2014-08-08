package org.headstar.beangraph;

public class BeanVertex {
    private final String name;

    BeanVertex(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BeanVertex that = (BeanVertex) o;

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
        final StringBuilder sb = new StringBuilder("Bean [");
        sb.append("id='").append(name).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
