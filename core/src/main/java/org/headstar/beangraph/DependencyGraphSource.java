package org.headstar.beangraph;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;


@Component
public class DependencyGraphSource implements ApplicationListener<ContextRefreshedEvent> {

    private static Logger log = LoggerFactory.getLogger(DependencyGraphSource.class);

    private List<DependencyGraphSourceListener> listeners = new ArrayList<DependencyGraphSourceListener>();

    public void addListener(DependencyGraphSourceListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();

        UnmodifiableDirectedGraph<BeanVertex, DefaultEdge> dependencyGraph = createDependencyGraph(applicationContext);
        JohnsonSimpleCycles<BeanVertex, DefaultEdge> cyclesFinder = new JohnsonSimpleCycles<BeanVertex, DefaultEdge>(dependencyGraph);
        List<List<BeanVertex>> cycles = cyclesFinder.findSimpleCycles();

        DependencyGraphResult result = new DependencyGraphResult(dependencyGraph, Collections.unmodifiableList(cycles));
        for (DependencyGraphSourceListener listener : listeners) {
            listener.onDependencyGraph(applicationContext, result);
        }
    }

    private UnmodifiableDirectedGraph<BeanVertex, DefaultEdge> createDependencyGraph(ApplicationContext context) {
        DirectedGraph<BeanVertex, DefaultEdge> graph = new DefaultDirectedGraph<BeanVertex, DefaultEdge>(DefaultEdge.class);
        if (!(context instanceof AbstractApplicationContext)) {
            return new UnmodifiableDirectedGraph<BeanVertex, DefaultEdge>(graph);
        }

        ConfigurableListableBeanFactory factory = ((AbstractApplicationContext) context).getBeanFactory();

        Queue<BeanVertex> queue = new ArrayDeque<BeanVertex>();
        for (String beanName : factory.getBeanDefinitionNames()) {
            queue.add(new BeanVertex(beanName));
        }
        while (!queue.isEmpty()) {
            BeanVertex b = queue.remove();
            graph.addVertex(b);
            for (String dependency : getDependenciesForBean(factory, b.getName())) {
                BeanVertex dep = new BeanVertex(dependency);
                if (!graph.containsVertex(dep)) {
                    graph.addVertex(dep);
                    queue.add(dep);
                }
                graph.addEdge(b, dep);
            }
        }

        return new UnmodifiableDirectedGraph<BeanVertex, DefaultEdge>(graph);
    }

    private Collection<String> getDependenciesForBean(final ConfigurableListableBeanFactory factory, final String sourceBeanName) {
        final List<String> res = new ArrayList<String>();
        res.addAll(Arrays.asList(factory.getDependenciesForBean(sourceBeanName)));
        Object bean = factory.getBean(sourceBeanName);

        final Class<?> clazz = AopUtils.getTargetClass(bean);
        ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
                    public void doWith(Method method) {
                        if (method.isAnnotationPresent(ManuallyWired.class)) {
                            ManuallyWired manuallyWired = method.getAnnotation(ManuallyWired.class);
                            for(String beanName : manuallyWired.beanNames()) {
                                try {
                                    factory.getBean(beanName);
                                    res.add(beanName);
                                } catch(NoSuchBeanDefinitionException e) {
                                    log.warn("Bean specified by @ManuallyWired not found: sourceBean={}, beanName={}", sourceBeanName, beanName);
                                }
                            }
                        }
                    }
                }
        );

        return res;
    }
}
