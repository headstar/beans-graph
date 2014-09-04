##Spring beans graph 
[![Build Status](https://travis-ci.org/headstar/beans-graph.svg?branch=master)](https://travis-ci.org/headstar/beans-graph) [![Coverage Status](https://img.shields.io/coveralls/headstar/beans-graph.svg)](https://coveralls.io/r/headstar/beans-graph?branch=master)

The `beans-graph` library creates a graph of the bean dependencies in your Spring application. The dependencies are collected at runtime. The library will help you:

* Find cyclic dependencies in your application
* Get an overview of your application's bean dependencies

###Maven

Current version is 1.2.0.

```xml
<dependency>
    <groupId>com.headstartech.beansgraph</groupId>
    <artifactId>beansgraph-core</artifactId>
    <version>1.2.0.RELEASE</version>
</dependency>
```

### Basic usage


Java annotation configuration:

```java
import com.headstartech.beansgraph.BeansGraphProducer;
import com.headstartech.beansgraph.ConsoleReporter;
import com.headstartech.beansgraph.EnableBeansGraph;
import org.springframework.stereotype.Component;

@EnableBeansGraph
@Component
public class ConfigureBeanGraphReporters implements BeansGraphConfigurer {

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
        .filter(new ClassNameFilter("com.foo"))
        .build();
}
```
### Dot reporter

To be able to generate an image with the dependencies, configure the dot reporter (http://en.wikipedia.org/wiki/DOT_%28graph_description_language%29).

Add dependency:
```xml
<dependency>
    <groupId>com.headstartech.beansgraph</groupId>
    <artifactId>beansgraph-dot-reporter</artifactId>
    <version>1.2.0.RELEASE</version>
</dependency>
```

Configure:
```java
 @Override
 public void configureReporters(BeansGraphProducer producer) {
        DotReporter.forSource(producer)
            .filter(new ClassNameFilter("org.foo.bar"))
            .toOutput(new PrintWriter(new File("/tmp/dep.dot")))
            .build();
}
```

###License

Published under Apache Software License 2.0, see LICENSE
