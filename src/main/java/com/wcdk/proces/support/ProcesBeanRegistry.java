package com.wcdk.proces.support;

import com.wcdk.proces.annotation.ProcesBean;
import com.wcdk.proces.dto.WcdkProcesConnectionEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 流程回调 bean 注册中心。
 *
 * @author WCDK
 * @date 2026/7/13
 */
@Component
@RequiredArgsConstructor
public class ProcesBeanRegistry {

    private final ApplicationContext applicationContext;

    private final Map<String, ProcesBeanInvoker> invokerMap = new LinkedHashMap<>();

    @PostConstruct
    public void registerProcesBean() {
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Class<?> beanType = applicationContext.getType(beanName);
            if (beanType == null) {
                continue;
            }
            ReflectionUtils.doWithMethods(beanType, method -> registerMethod(beanName, method),
                    method -> method.isAnnotationPresent(ProcesBean.class));
        }
    }

    public void invoke(String processBeanName, WcdkProcesConnectionEvent event) {
        ProcesBeanInvoker invoker = invokerMap.get(processBeanName);
        if (invoker == null) {
            throw new IllegalArgumentException("未注册对应的流程回调 bean：" + processBeanName);
        }
        invoker.invoke(event);
    }

    private void registerMethod(String beanName, Method method) {
        ProcesBean procesBean = method.getAnnotation(ProcesBean.class);
        String processBeanName = procesBean.value();
        if (!StringUtils.hasText(processBeanName)) {
            throw new IllegalStateException("ProcesBean 注解必须指定 bean 名称，方法：" + method.getName());
        }
        if (method.getParameterCount() > 1) {
            throw new IllegalStateException("ProcesBean 标注的方法最多只能有一个参数，方法：" + method.getName());
        }
        if (method.getParameterCount() == 1
                && !WcdkProcesConnectionEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
            throw new IllegalStateException("ProcesBean 标注的方法参数必须为 WcdkProcesConnectionEvent，方法：" + method.getName());
        }
        if (invokerMap.containsKey(processBeanName)) {
            throw new IllegalStateException("流程回调 bean 名称重复：" + processBeanName);
        }
        invokerMap.put(processBeanName, new ProcesBeanInvoker(applicationContext, beanName, method));
    }

    private static final class ProcesBeanInvoker {

        private final ApplicationContext applicationContext;

        private final String beanName;

        private final Method method;

        private ProcesBeanInvoker(ApplicationContext applicationContext, String beanName, Method method) {
            this.applicationContext = applicationContext;
            this.beanName = beanName;
            this.method = method;
        }

        private void invoke(WcdkProcesConnectionEvent event) {
            try {
                Object bean = applicationContext.getBean(beanName);
                Method targetMethod = AopUtils.getTargetClass(bean).getMethod(method.getName(), method.getParameterTypes());
                ReflectionUtils.makeAccessible(targetMethod);
                if (targetMethod.getParameterCount() == 0) {
                    targetMethod.invoke(bean);
                    return;
                }
                targetMethod.invoke(bean, event);
            } catch (Exception ex) {
                throw new IllegalStateException("执行流程回调 bean 失败：" + method.getName(), ex);
            }
        }
    }
}
