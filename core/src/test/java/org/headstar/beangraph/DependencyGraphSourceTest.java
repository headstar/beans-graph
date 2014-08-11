package org.headstar.beangraph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

public class DependencyGraphSourceTest {

    @Test
    public void test1() {
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

        DependencyGraphResult result = testListener.getGraphResult();
        List<List<BeanVertex>> cycles = result.getCycles();
        assertNotNull(cycles);
        assertEquals(1, cycles.size());
        assertEquals(getExpectedCycle(), cycles.get(0));
    }

    @EnableDependencyGraph
    @Configuration("testConfigurer")
    private static class TestConfigurer implements DependencyGraphConfigurer {

        private TestListener testListener;

        public TestConfigurer() {}

        @Override
        public void configureReporters(DependencyGraphSource graphSource) {
            testListener = new TestListener();
            graphSource.addListener(testListener);
        }

        public TestListener getTestListener() {
            return testListener;
        }
    }

    private static class TestListener implements DependencyGraphSourceListener {

        private ApplicationContext applicationContext;
        private DependencyGraphResult graphResult;

        @Override
        public void onDependencyGraph(ApplicationContext applicationContext, DependencyGraphResult result) {
            this.applicationContext = applicationContext;
            this.graphResult = result;
        }

        public ApplicationContext getApplicationContext() {
            return applicationContext;
        }

        public DependencyGraphResult getGraphResult() {
            return graphResult;
        }
    }

    @Component("foo1")
    private static class Foo1 {
       @Autowired Foo2 foo2;
    }

    @Component("foo2")
    private static class Foo2 {
       @Autowired Foo3 foo3;
    }

    @Component("foo3")
    private static class Foo3 {
       @Autowired Foo1 foo1;
    }

    @Component("foo4")
    private static class Foo4 {
        @Autowired Foo1 foo1;
    }

    private List<BeanVertex> getExpectedCycle() {
        List<BeanVertex> res = new ArrayList<BeanVertex>();
        res.add(new BeanVertex("foo3"));
        res.add(new BeanVertex("foo2"));
        res.add(new BeanVertex("foo1"));
        return res;
    }


}
