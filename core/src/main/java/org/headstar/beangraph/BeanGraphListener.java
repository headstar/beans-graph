package org.headstar.beangraph;

import org.springframework.context.ApplicationContext;

public interface BeanGraphListener {

    void onBeanGraphResult(ApplicationContext applicationContext, BeanGraphResult result);
}
