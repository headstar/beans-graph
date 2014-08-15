package org.headstar.beansgraph;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Produces the bean graph on every ContextRefreshedEvent and calls and registered listeners.
 *
 * @see BeansGraphListener
 * @author Per Johansson
 * @since 1.0
 */
@Component
public class BeansGraphProducer implements ApplicationListener<ContextRefreshedEvent> {

    private static Logger log = LoggerFactory.getLogger(BeansGraphProducer.class);

    private List<BeansGraphListener> listeners = new ArrayList<BeansGraphListener>();

    public void addListener(BeansGraphListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();

        UnmodifiableDirectedGraph<BeansGraphVertex, DefaultEdge> dependencyGraph = createDependencyGraph(applicationContext);
        JohnsonSimpleCycles<BeansGraphVertex, DefaultEdge> cyclesFinder = new JohnsonSimpleCycles<BeansGraphVertex, DefaultEdge>(dependencyGraph);
        List<List<BeansGraphVertex>> cycles = cyclesFinder.findSimpleCycles();

        BeansGraphResult result = new BeansGraphResult(dependencyGraph, Collections.unmodifiableList(cycles));
        for (BeansGraphListener listener : listeners) {
            listener.onBeanGraphResult(applicationContext, result);
        }
    }

    private UnmodifiableDirectedGraph<BeansGraphVertex, DefaultEdge> createDependencyGraph(ApplicationContext context) {
        DirectedGraph<BeansGraphVertex, DefaultEdge> graph = new DefaultDirectedGraph<BeansGraphVertex, DefaultEdge>(DefaultEdge.class);
        if (!(context instanceof AbstractApplicationContext)) {
            return new UnmodifiableDirectedGraph<BeansGraphVertex, DefaultEdge>(graph);
        }

        ConfigurableListableBeanFactory factory = ((AbstractApplicationContext) context).getBeanFactory();

        Queue<BeansGraphVertex> queue = new ArrayDeque<BeansGraphVertex>();
        for (String beanName : factory.getBeanDefinitionNames()) {
            queue.add(new BeansGraphVertex(beanName));
        }
        while (!queue.isEmpty()) {
            BeansGraphVertex b = queue.remove();
            graph.addVertex(b);
            for (String dependency : getDependenciesForBean(factory, b.getName())) {
                BeansGraphVertex dep = new BeansGraphVertex(dependency);
                if (!graph.containsVertex(dep)) {
                    graph.addVertex(dep);
                    queue.add(dep);
                }
                graph.addEdge(b, dep);
            }
        }

        return new UnmodifiableDirectedGraph<BeansGraphVertex, DefaultEdge>(graph);
    }

    private Collection<String> getDependenciesForBean(final ConfigurableListableBeanFactory factory, final String sourceBeanName) {
        final List<String> res = new ArrayList<String>();
        res.addAll(Arrays.asList(factory.getDependenciesForBean(sourceBeanName)));

        if(factory.containsBeanDefinition(sourceBeanName)) {
            final Object bean = factory.getBean(sourceBeanName);
            final Class<?> clazz = AopUtils.getTargetClass(bean);
            ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
                        public void doWith(Method method) {
                            if (method.isAnnotationPresent(ManuallyWired.class)) {
                                ManuallyWired manuallyWired = method.getAnnotation(ManuallyWired.class);
                                for (String beanName : manuallyWired.beanNames()) {
                                    if(beanName != null && !beanName.isEmpty()) {
                                        res.add(beanName);
                                    }
                                }
                            }
                        }
                    }
            );
        }

        return res;
    }
}
