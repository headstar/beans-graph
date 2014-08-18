##Spring beans graph [![Build Status](https://travis-ci.org/headstar/spring-beans-graph.svg?branch=master)](https://travis-ci.org/headstar/spring-beans-graph)

The spring-beans-graph library creates a graph of Spring bean dependencies in an application. The dependencies are collected at runtime. The library is useful if you want to:

* Find cyclic dependencies
* Get an overview of your application's bean dependencies

###Maven

Current version is 1.0.0.

```xml
<dependency>
    <groupId>com.headstartech.beansgraph</groupId>
    <artifactId>beans-graph-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

###Basic Usage


Java Annotation Config:

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
