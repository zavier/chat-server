package com.github.zavier.chat.util;

import com.github.zavier.chat.conf.SpringConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringUtil {

    private static ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);;

    public static <T> T getBean(Class<T> cls) {
        return applicationContext.getBean(cls);
    }

    public static ApplicationEventPublisher getEventPublisher() {
        return applicationContext;
    }
}
