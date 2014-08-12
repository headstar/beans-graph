package org.headstar.beangraph;

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


@Component
public class BeanGraphProducer implements ApplicationListener<ContextRefreshedEvent> {

    private static Logger log = LoggerFactory.getLogger(BeanGraphProducer.class);

    private List<BeanGraphListener> listeners = new ArrayList<BeanGraphListener>();

    public void addListener(BeanGraphListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();

        UnmodifiableDirectedGraph<BeanGraphVertex, DefaultEdge> dependencyGraph = createDependencyGraph(applicationContext);
        JohnsonSimpleCycles<BeanGraphVertex, DefaultEdge> cyclesFinder = new JohnsonSimpleCycles<BeanGraphVertex, DefaultEdge>(dependencyGraph);
        List<List<BeanGraphVertex>> cycles = cyclesFinder.findSimpleCycles();

        BeanGraphResult result = new BeanGraphResult(dependencyGraph, Collections.unmodifiableList(cycles));
        for (BeanGraphListener listener : listeners) {
            listener.onBeanGraphResult(applicationContext, result);
        }
    }

    private UnmodifiableDirectedGraph<BeanGraphVertex, DefaultEdge> createDependencyGraph(ApplicationContext context) {
        DirectedGraph<BeanGraphVertex, DefaultEdge> graph = new DefaultDirectedGraph<BeanGraphVertex, DefaultEdge>(DefaultEdge.class);
        if (!(context instanceof AbstractApplicationContext)) {
            return new UnmodifiableDirectedGraph<BeanGraphVertex, DefaultEdge>(graph);
        }

        ConfigurableListableBeanFactory factory = ((AbstractApplicationContext) context).getBeanFactory();

        Queue<BeanGraphVertex> queue = new ArrayDeque<BeanGraphVertex>();
        for (String beanName : factory.getBeanDefinitionNames()) {
            queue.add(new BeanGraphVertex(beanName));
        }
        while (!queue.isEmpty()) {
            BeanGraphVertex b = queue.remove();
            graph.addVertex(b);
            for (String dependency : getDependenciesForBean(factory, b.getName())) {
                BeanGraphVertex dep = new BeanGraphVertex(dependency);
                if (!graph.containsVertex(dep)) {
                    graph.addVertex(dep);
                    queue.add(dep);
                }
                graph.addEdge(b, dep);
            }
        }

        return new UnmodifiableDirectedGraph<BeanGraphVertex, DefaultEdge>(graph);
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
