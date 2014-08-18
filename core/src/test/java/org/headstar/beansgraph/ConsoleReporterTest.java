package org.headstar.beansgraph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.testng.annotations.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

/**
 * Created by per on 8/15/14.
 */
public class ConsoleReporterTest {

    @Test
    public void testReporterBasic() {
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

    @Test
    public void testReporterWithFilter() {
        // given
        AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
        appContext.register(TestConfigurerWithFilter.class);
        appContext.register(Foo1.class);
        appContext.register(Foo2.class);
        appContext.register(Foo3.class);
        appContext.register(Foo4.class);

        // when
        appContext.refresh();

        // then
        TestConfigurerWithFilter testConfigurer = (TestConfigurerWithFilter) appContext.getBean("testConfigurer");
        String output = testConfigurer.getStringWriter().toString();
        assertTrue(output.contains("org.headstar"));
        assertFalse(output.contains("org.springframework.context.annotation.internalAutowiredAnnotationProcessor"));
    }

    @EnableBeansGraph
    @Configuration("testConfigurer")
    private static class TestConfigurer implements BeanGraphConfigurer {

        private StringWriter stringWriter;

        public TestConfigurer() {
        }

        @Override
        public void configureReporters(BeansGraphProducer graphSource) {
            stringWriter = new StringWriter();
            ConsoleReporter.forSource(graphSource)
                    .withOutput(new PrintWriter(stringWriter))
                    .build();
        }

        public StringWriter getStringWriter() {
            return stringWriter;
        }
    }

    @EnableBeansGraph
    @Configuration("testConfigurer")
    private static class TestConfigurerWithFilter implements BeanGraphConfigurer {

        private StringWriter stringWriter;

        public TestConfigurerWithFilter() {
        }

        @Override
        public void configureReporters(BeansGraphProducer graphSource) {
            stringWriter = new StringWriter();
            ConsoleReporter.forSource(graphSource)
                    .withOutput(new PrintWriter(stringWriter))
                    .withClassNamePattern(Pattern.compile("org\\.headstar\\..*"))
                    .build();
        }

        public StringWriter getStringWriter() {
            return stringWriter;
        }
    }

    @Component("foo1")
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
