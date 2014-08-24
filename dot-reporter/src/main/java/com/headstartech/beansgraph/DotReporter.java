package com.headstartech.beansgraph;

import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.ext.DOTExporter;
import org.springframework.context.ApplicationContext;

import java.io.PrintWriter;
import java.io.Writer;

/**
 *  A reporter which outputs the result to a Dot file, see http://en.wikipedia.org/wiki/DOT_language.
 *
 * @author Per Johansson
 * @since 1.0
 */
public class DotReporter implements BeansGraphListener {

    public static Builder forSource(BeansGraphProducer source) {
        return new Builder(source);
    }

    public static class Builder {
        private final BeansGraphProducer source;
        private Writer out;

        private Builder(BeansGraphProducer source) {
            this.source = source;
            toOutput(new PrintWriter(System.out));
        }


        public Builder toOutput(Writer out) {
            this.out = out;
            return this;
        }

        public DotReporter build() {
            return new DotReporter(source, out);
        }
    }

    private final Writer out;

    private DotReporter(BeansGraphProducer source, Writer out) {
        this.out = out;
        source.addListener(this);
    }


    @Override
    public void onBeanGraphResult(ApplicationContext applicationContext, BeansGraphResult result) {
        DOTExporter<Bean, DefaultEdge> exporter = new DOTExporter<Bean, DefaultEdge>(new BeanVertexIdProvider(),
                new BeanVertexNameProvider(), null);
        exporter.export(out, result.getDependencyGraph());
    }

    private static class BeanVertexNameProvider implements VertexNameProvider<Bean> {

        @Override
        public String getVertexName(Bean vertex) {
            return vertex.getName();
        }
    }

    private static class BeanVertexIdProvider implements VertexNameProvider<Bean> {

        @Override
        public String getVertexName(Bean bean) {
            return bean.getName().replace('.', '_');
        }
    }
}