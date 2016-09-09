package com.headstartech.beansgraph;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
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

        UnmodifiableDirectedGraph<Bean, DefaultEdge> dependencyGraph = createDependencyGraph(applicationContext);
        JohnsonSimpleCycles<Bean, DefaultEdge> cyclesFinder = new JohnsonSimpleCycles<Bean, DefaultEdge>(dependencyGraph);
        List<List<Bean>> cycles = cyclesFinder.findSimpleCycles();

        BeansGraphResult result = new BeansGraphResult(dependencyGraph, Collections.unmodifiableList(cycles));
        for (BeansGraphListener listener : listeners) {
            listener.onBeanGraphResult(applicationContext, result);
        }
    }

    private UnmodifiableDirectedGraph<Bean, DefaultEdge> createDependencyGraph(ApplicationContext context) {
        DirectedGraph<Bean, DefaultEdge> graph = new DefaultDirectedGraph<Bean, DefaultEdge>(DefaultEdge.class);
        if (!(context instanceof AbstractApplicationContext)) {
            log.info("ApplicationContext not instance of {}", AbstractApplicationContext.class.getName());
            return new UnmodifiableDirectedGraph<Bean, DefaultEdge>(graph);
        }

        ConfigurableListableBeanFactory factory = ((AbstractApplicationContext) context).getBeanFactory();

        Queue<Bean> queue = new ArrayDeque<Bean>();
        for (String beanName : factory.getBeanDefinitionNames()) {
            Bean bv = createBeanVertex(factory, beanName);
            if(bv != null) {
                queue.add(bv);
            }
        }
        while (!queue.isEmpty()) {
            Bean bv = queue.remove();
            graph.addVertex(bv);


            for (String dependency : getDependenciesForBean(factory, bv.getName())) {
                Bean depBV = createBeanVertex(factory, dependency);
                if(depBV != null) {
                    if (!graph.containsVertex(depBV)) {
                        graph.addVertex(depBV);
                        queue.add(depBV);
                    }
                    graph.addEdge(bv, depBV);
                }
            }
        }

        return new UnmodifiableDirectedGraph<Bean, DefaultEdge>(graph);
    }

    private Collection<String> getDependenciesForBean(final ConfigurableListableBeanFactory factory, final String sourceBeanName) {
        final List<String> res = new ArrayList<String>();
        res.addAll(Arrays.asList(factory.getDependenciesForBean(sourceBeanName)));

        try {
            Class<?> clazz = getClass(factory, sourceBeanName);
            if(clazz != null) {
                ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
                            public void doWith(Method method) {
                                if (method.isAnnotationPresent(ManuallyWired.class)) {
                                    ManuallyWired manuallyWired = method.getAnnotation(ManuallyWired.class);
                                    for (String beanName : manuallyWired.beanNames()) {
                                        if (beanName != null && !beanName.isEmpty()) {
                                            res.add(beanName);
                                        }
                                    }
                                }
                            }
                        }
                );
            }
        } catch(BeansException e) {
            // do nothing, just log
            log.debug("failed to get bean: bean={}, cause={}", sourceBeanName, e.getMostSpecificCause().getMessage());
        }

        // If the source bean was produced by a FactoryBean, add the FactoryBean as a dependency
        try {
            BeanDefinition bd = factory.getBeanDefinition(sourceBeanName);
            if(bd.getBeanClassName() != null) {
                Class<?> beanClass = Class.forName(bd.getBeanClassName());
                if(FactoryBean.class.isAssignableFrom(beanClass) && !sourceBeanName.equals(bd.getBeanClassName())) {
                    Map<String, ?> m = factory.getBeansOfType(beanClass);
                    if(m.keySet().size() > 1) {
                        log.warn("more than 1 factory bean of type found: type={}", beanClass.getCanonicalName());
                    }
                    res.addAll(m.keySet());
                }
            }
        } catch(NoSuchBeanDefinitionException e) {
            // ignore
        } catch (ClassNotFoundException e) {
            // ignore
        }

        return res;
    }

    private Bean createBeanVertex(final ConfigurableListableBeanFactory factory, String beanName) {
        if(factory.containsBeanDefinition(beanName)) {
            BeanDefinition def = factory.getBeanDefinition(beanName);
            if(def.isAbstract()) {
                log.debug("{} is an abstract bean, skipping", beanName);
                return null;
            }
        }
        Bean res = new Bean(beanName);
        try {
            Class<?> clazz = getClass(factory, beanName);
            if(clazz != null) {
                res.setClassName(clazz.getCanonicalName());
            }
        } catch(BeansException e) {
            // do nothing, just log
            log.debug("failed to get bean: bean={}, cause={}", beanName, e.getMostSpecificCause().getMessage());
        }
        return res;
    }

    private Class<?> getClass(ConfigurableListableBeanFactory factory, String beanName) {
        Object bean = factory.getBean(beanName);
        if(bean == null) {
            return null;
        }
        Class<?> clazz = AopUtils.getTargetClass(bean);
        return clazz;
    }
}
