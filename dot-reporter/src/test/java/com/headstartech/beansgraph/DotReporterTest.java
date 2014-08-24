package com.headstartech.beansgraph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.testng.annotations.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.testng.Assert.assertFalse;

/**
 * Created by per on 8/16/14.
 */
public class DotReporterTest {

    @Test
    public void testReporter() {
        // given
        AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
        appContext.register(TestConfigurer.class);
        appContext.register(Foo1.class);
        appContext.register(Foo2.class);
        appContext.register(Foo3.class);
        appContext.register(Foo4.class);


        // when
        appContext.refresh();

        // then
        TestConfigurer testConfigurer = (TestConfigurer) appContext.getBean("testConfigurer");
        assertFalse(testConfigurer.getStringWriter().toString().isEmpty());
    }

    @EnableBeansGraph
    @Configuration("testConfigurer")
    private static class TestConfigurer implements BeansGraphConfigurer {

        private StringWriter stringWriter;

        public TestConfigurer() {
        }

        @Override
        public void configureReporters(BeansGraphProducer graphSource) {
            stringWriter = new StringWriter();
            DotReporter.forSource(graphSource)
                    .toOutput(new PrintWriter(stringWriter))
                    .build();

        }

        public StringWriter getStringWriter() {
            return stringWriter;
        }
    }

    @Component
    private static class Foo1 {
        @Autowired
        Foo2 foo2;
    }

    @Component
    private static class Foo2 {
        @Autowired
        Foo3 foo3;
    }


    @Component
    private static class Foo3 {
        @Autowired
        Foo1 foo1;
    }


    @Component
    private static class Foo4 {
        @Autowired
        Foo1 foo1;

        @Autowired
        Foo2 foo2;
    }

}
