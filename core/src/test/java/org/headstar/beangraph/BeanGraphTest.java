package org.headstar.beangraph;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableGraph;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.AssertJUnit.assertTrue;

public class BeanGraphTest {

    @Test
    public void testDependencies() {
        // given
        AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
        appContext.register(TestConfigurer.class);
        appContext.register(Foo1.class);
        appContext.register(Foo2.class);
        appContext.register(Foo3.class);
        appContext.register(Foo4.class);
        appContext.register(Foo5.class);

        // when
        appContext.refresh();

        // then
        TestConfigurer testConfigurer = (TestConfigurer) appContext.getBean("testConfigurer");
        TestListener testListener = testConfigurer.getTestListener();
        assertSame(appContext, testListener.getApplicationContext());
        assertNotNull(testListener.getGraphResult());

        BeanGraphResult result = testListener.getGraphResult();
        UnmodifiableGraph<BeanGraphVertex, DefaultEdge> graph = result.getDependencies();

        assertBeanHasDependencies(graph, "foo1", "foo2");
        assertBeanHasDependencies(graph, "foo2", "foo3");
        assertBeanHasDependencies(graph, "foo3", "foo1");
        assertBeanHasDependencies(graph, "foo4", "foo1", "foo2", "foo3");
        assertBeanHasDependencies(graph, "foo5");
    }

    @Test
    public void testCycle() {
        // given
        AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
        appContext.register(TestConfigurer.class);
        appContext.register(Foo1.class);
        appContext.register(Foo2.class);
        appContext.register(Foo3.class);

        // when
        appContext.refresh();

        // then
        TestConfigurer testConfigurer = (TestConfigurer) appContext.getBean("testConfigurer");
        TestListener testListener = testConfigurer.getTestListener();
        assertSame(appContext, testListener.getApplicationContext());
        assertNotNull(testListener.getGraphResult());

        BeanGraphResult result = testListener.getGraphResult();
        List<List<BeanGraphVertex>> cycles = result.getCycles();
        assertNotNull(cycles);
        assertEquals(1, cycles.size());
        assertEquals(getExpectedCycle(), cycles.get(0));
    }

    private void assertBeanHasDependencies(UnmodifiableGraph<BeanGraphVertex, DefaultEdge> graph, String source, String... targets) {
        BeanGraphVertex sourceVertex = new BeanGraphVertex(source);

        Set<DefaultEdge> edges = graph.outgoingEdgesOf(sourceVertex);
        Set<BeanGraphVertex> actualTargetVertices = new HashSet<BeanGraphVertex>();
        for(DefaultEdge edge : edges) {
            actualTargetVertices.add(graph.getEdgeTarget(edge));
        }

        for(String target : targets) {
            BeanGraphVertex targetVertex = new BeanGraphVertex(target);
            assertTrue(String.format("%s depends on %s", source, target), actualTargetVertices.contains(targetVertex));
            actualTargetVertices.remove(targetVertex);
        }
        assertEquals(actualTargetVertices, new HashSet<BeanGraphVertex>(), String.format("no unexpected dependencies for %s", source));
    }

    @EnableBeanGraph
    @Configuration("testConfigurer")
    private static class TestConfigurer implements BeanGraphConfigurer {

        private TestListener testListener;

        public TestConfigurer() {
        }

        @Override
        public void configureReporters(BeanGraphProducer graphSource) {
            testListener = new TestListener();
            graphSource.addListener(testListener);
        }

        public TestListener getTestListener() {
            return testListener;
        }
    }

    private static class TestListener implements BeanGraphListener {

        private ApplicationContext applicationContext;
        private BeanGraphResult graphResult;

        @Override
        public void onBeanGraphResult(ApplicationContext applicationContext, BeanGraphResult result) {
            this.applicationContext = applicationContext;
            this.graphResult = result;
        }

        public ApplicationContext getApplicationContext() {
            return applicationContext;
        }

        public BeanGraphResult getGraphResult() {
            return graphResult;
        }
    }

    @Component("foo1")
    private static class Foo1 {
        @Autowired
        Foo2 foo2;
    }

    @Component("foo2")
    private static class Foo2 implements ApplicationContextAware {
        private final static String FOO3_BEAN_NAME = "foo3";

        @Autowired
        Foo3 foo3;

        @ManuallyWired(beanNames = {FOO3_BEAN_NAME})
        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            foo3 = (Foo3) applicationContext.getBean(FOO3_BEAN_NAME);
        }

    }

    @Component("foo3")
    private static class Foo3 {
        @Autowired
        Foo1 foo1;
    }

    @Component("foo4")
    private static class Foo4 implements ApplicationContextAware, InitializingBean {

        private final static String FOO2_BEAN_NAME = "foo2";
        private final static String FOO3_BEAN_NAME = "foo3";

        private ApplicationContext applicationContext;

        @Autowired
        Foo1 foo1;

        Foo2 foo2;
        Foo3 foo3;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }

        @ManuallyWired(beanNames = {FOO2_BEAN_NAME, FOO3_BEAN_NAME})
        @Override
        public void afterPropertiesSet() throws Exception {
            foo2 = (Foo2) applicationContext.getBean(FOO2_BEAN_NAME);
            foo3 = (Foo3) applicationContext.getBean(FOO3_BEAN_NAME);
        }
    }

    @Component("foo5")
    private static class Foo5 {
    }

    private List<BeanGraphVertex> getExpectedCycle() {
        List<BeanGraphVertex> res = new ArrayList<BeanGraphVertex>();
        res.add(new BeanGraphVertex("foo3"));
        res.add(new BeanGraphVertex("foo2"));
        res.add(new BeanGraphVertex("foo1"));
        return res;
    }
}
