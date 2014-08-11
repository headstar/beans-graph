package org.headstar.beangraph;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

public class DependencyGraphSourceTest {

    @Test
    public void test() {
        // given
        AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
        appContext.register(TestConfigurer.class);

        // when
        appContext.refresh();

        // then
        TestConfigurer testConfigurer = (TestConfigurer) appContext.getBean("dependencyGraphSourceTest.TestConfigurer");
        assertSame(appContext, testConfigurer.getTestListener().getApplicationContext());
        assertNotNull(testConfigurer.getTestListener().getGraphResult());
    }

    @EnableDependencyGraph
    @Configuration
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

}
