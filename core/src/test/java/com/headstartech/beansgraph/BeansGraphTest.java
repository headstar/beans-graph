package com.headstartech.beansgraph;

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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertTrue;

public class BeansGraphTest {

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

        BeansGraphResult result = testListener.getGraphResult();
        UnmodifiableGraph<Bean, DefaultEdge> graph = result.getDependencyGraph();

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

        BeansGraphResult result = testListener.getGraphResult();
        List<List<Bean>> cycles = result.getCycles();
        assertNotNull(cycles);
        assertEquals(1, cycles.size());
        assertEquals(getExpectedCycle(), cycles.get(0));
    }

    @Test
    public void testVertex() {
        // given
        AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
        appContext.register(TestConfigurer.class);
        appContext.register(Foo5.class);

        // when
        appContext.refresh();

        // then
        TestConfigurer testConfigurer = (TestConfigurer) appContext.getBean("testConfigurer");
        TestListener testListener = testConfigurer.getTestListener();
        assertNotNull(testListener.getGraphResult());

        BeansGraphResult result = testListener.getGraphResult();
        Set<Bean> beans = result.getDependencyGraph().vertexSet();

        boolean foo5Found = false;
        for(Bean bean : beans) {
            if(bean.getName().equals("foo5")) {
                assertEquals(bean.getClassName(), Foo5.class.getCanonicalName());
                foo5Found = true;
            }
        }

        Assert.assertTrue(foo5Found);
    }


    private void assertBeanHasDependencies(UnmodifiableGraph<Bean, DefaultEdge> graph, String source, String... targets) {
        Bean sourceVertex = new Bean(source);

        Set<DefaultEdge> edges = graph.outgoingEdgesOf(sourceVertex);
        Set<Bean> actualTargetVertices = new HashSet<Bean>();
        for(DefaultEdge edge : edges) {
            actualTargetVertices.add(graph.getEdgeTarget(edge));
        }

        for(String target : targets) {
            Bean targetVertex = new Bean(target);
            assertTrue(String.format("%s depends on %s", source, target), actualTargetVertices.contains(targetVertex));
            actualTargetVertices.remove(targetVertex);
        }
        assertEquals(actualTargetVertices, new HashSet<Bean>(), String.format("no unexpected dependencies for %s", source));
    }

    @EnableBeansGraph
    @Configuration("testConfigurer")
    private static class TestConfigurer implements BeansGraphConfigurer {

        private TestListener testListener;

        public TestConfigurer() {
        }

        @Override
        public void configureReporters(BeansGraphProducer graphSource) {
            testListener = new TestListener();
            graphSource.addListener(testListener);
        }

        public TestListener getTestListener() {
            return testListener;
        }
    }

    private static class TestListener implements BeansGraphListener {

        private ApplicationContext applicationContext;
        private BeansGraphResult graphResult;

        @Override
        public void onBeanGraphResult(ApplicationContext applicationContext, BeansGraphResult result) {
            this.applicationContext = applicationContext;
            this.graphResult = result;
        }

        public ApplicationContext getApplicationContext() {
            return applicationContext;
        }

        public BeansGraphResult getGraphResult() {
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

    private List<Bean> getExpectedCycle() {
        List<Bean> res = new ArrayList<Bean>();
        res.add(new Bean("foo3"));
        res.add(new Bean("foo2"));
        res.add(new Bean("foo1"));
        return res;
    }
}
