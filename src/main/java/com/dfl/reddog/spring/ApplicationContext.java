package com.dfl.reddog.spring;

import com.dfl.reddog.service.Constants;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {

    private Class configClass; // 启动传入的配置类

    // 配置 解析到 beanDefinition
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    // 单例缓存
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    // beanPostProcessor 在生成bd的过程中产生，可以处理所有bean
    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    // 启动、加载配置 生成beanDefinition
    public ApplicationContext(Class configClass) {
        if (configClass == null) {
            throw new NullPointerException("spring start configClass is null");
        }
        this.configClass = configClass;

        parseConfig(); // 解析成beanDefinition
        createBean(); // 生成单例对象, 多例getBean()时才生成
    }

    private void parseConfig() {
        // 扫描
        if (!configClass.isAnnotationPresent(ComponentScan.class)) {
            throw new RuntimeException("spring start configClass is not annotated with @ComponentScan");
        }
        ComponentScan annotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
        String path = annotation.value(); // java.dfl.service
        path = path.replace('.', '/'); // com/dfl/service

        // 获取编译后的.class配置文件目录
        ClassLoader classLoader = ApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(path);
        File file = new File(resource.getFile());

//            System.out.println(file); // C:\Users\Administrator\IdeaProjects\blondeCow\out\production\blondeCow\com\dfl\service
        if (!file.exists()) {
            throw new RuntimeException("file not found");
        }
        File[] files = file.listFiles();
        for (File f : files) {
            String fileName = f.getAbsolutePath();
            if (!fileName.endsWith(".class")) {
                continue;
            }
            String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
            className = className.replace("\\", ".");
//                        System.out.println(className); // java.dfl.service.AppConfig

            Class<?> clazz = null;
            try {
                clazz = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
//                        Object o = clazz.getDeclaredConstructor().newInstance();
            if (!clazz.isAnnotationPresent(Component.class)) {
                continue;
            }
            // 生成BeanDefinition
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setType(clazz);
            if (clazz.isAnnotationPresent(Scope.class)) {
                Scope scope = clazz.getAnnotation(Scope.class);
                String value = scope.value();
                if (Constants.SCOPE_PROTOTYPE.equals(value)) {
                    beanDefinition.setScope(Constants.SCOPE_PROTOTYPE);
                } else {
                    beanDefinition.setScope(Constants.SCOPE_SINGLETON);
                }
            } else {
                beanDefinition.setScope(Constants.SCOPE_SINGLETON); // 默认单例
            }

            Component component = clazz.getAnnotation(Component.class);
            String clazzName = component.value();
            if (clazzName.equals("")) {
                clazzName = clazz.getSimpleName();
                // 处理首字母小写
                clazzName = clazzName.substring(0, 1).toLowerCase() + clazzName.substring(1);
            }
            beanDefinitionMap.put(clazzName, beanDefinition);

            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                try {
                    BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.newInstance();
                    beanPostProcessors.add(beanPostProcessor);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // 生成单例对象
    private void createBean() {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (Constants.SCOPE_SINGLETON.equals(beanDefinition.getScope())) {
                Object bean = doCreateBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    // 生成单例对象
    private Object doCreateBean(String beanName, BeanDefinition beanDefinition) {
        Object bean = instanceBean(beanName, beanDefinition); // 生成对象
        populateBean(beanName, beanDefinition, bean); // 属性填充
        invokeAware(beanName, beanDefinition, bean); // 调用aware接口
        bean = initMethod(beanName, beanDefinition, bean); // 初始化

        return bean;
    }

    // 生成对象
    private Object instanceBean(String beanName, BeanDefinition beanDefinition) {
        Object bean = null;

        Class clazz = beanDefinition.getType();
        try {
            bean = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return bean;
    }

    // 属性填充
    private void populateBean(String beanName, BeanDefinition beanDefinition, Object bean) {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                try {
                    field.set(bean, this.getBean(field.getName()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // aware回调
    private void invokeAware(String beanName, BeanDefinition beanDefinition, Object bean) {
        if (bean instanceof BeanNameAware) {
            ((BeanNameAware) bean).setBeanName(beanName);
        }

        if (bean instanceof PrototypeAware) {
            ((PrototypeAware) bean).setPrototype(beanDefinition.getScope());
        }

    }

    // 初始化方法
    private Object initMethod(String beanName, BeanDefinition beanDefinition, Object bean) {
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            bean = beanPostProcessor.postProcessorBeforeInitialization(bean, beanName);
        }

        if (bean instanceof InitializeBean) {
            ((InitializeBean) bean).afterPropertiesSet();
        }

        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            bean = beanPostProcessor.postProcessorAfterInitialization(bean, beanName);
        }

        return bean;
    }

    // 获取bean
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new RuntimeException("bean " + beanName + " not found");
        }
        String scope = beanDefinition.getScope();
        if (scope.equals(Constants.SCOPE_PROTOTYPE)) {
            // 每次都要创建
            return doCreateBean(beanName, beanDefinition);
        } else {
            Object o = singletonObjects.get(beanName);
            if (o == null) {
                Object bean = doCreateBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
            return o;
        }
    }

}
