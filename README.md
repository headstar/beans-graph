##Spring beans graph [![Build Status](https://travis-ci.org/headstar/spring-beans-graph.svg?branch=master)](https://travis-ci.org/headstar/spring-beans-graph)

The `spring-beans-graph` library creates a graph of the bean dependencies in your Spring application. The dependencies are collected at runtime. The library will help you:

* Find cyclic dependencies in your application
* Get an overview of your application's bean dependencies

###Maven

Current version is 1.1.0.

```xml
<dependency>
    <groupId>com.headstartech.beansgraph</groupId>
    <artifactId>beans-graph-core</artifactId>
    <version>1.1.0</version>
</dependency>
```

###Usage


Java annotation configuration:

```java
import org.headstar.beansgraph.BeansGraphProducer;
import org.headstar.beansgraph.ConsoleReporter;
import org.headstar.beansgraph.EnableBeansGraph;
import org.springframework.stereotype.Component;

@EnableBeansGraph
@Component
public class ConfigureBeanGraphReporters implements org.headstar.beansgraph.BeanGraphConfigurer {

    @Override
    public void configureReporters(BeansGraphProducer producer) {
        ConsoleReporter.forSource(producer).build();
    }
}
```
The example above enables the beans graph producer (`@EnableBeansGraph`) and configures a reporter which prints the result to the default destination (`System.out`).

Filter on class name:
```java
 @Override
 public void configureReporters(BeansGraphProducer producer) {
        ConsoleReporter.forSource(producer)
        .filter(new ClassNameFilter("org.foo.bar"))
        .build();
}
```


###License

Published under Apache Software License 2.0, see LICENSE
