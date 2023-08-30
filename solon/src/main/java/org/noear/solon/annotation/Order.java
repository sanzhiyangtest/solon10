package org.noear.solon.annotation;

import java.lang.annotation.*;
import java.lang.reflect.AnnotatedElement;

import java.util.Comparator;

/**
 *
 * 赋予排序的能力
 * <code><p>
 * &#064;Order(-1) <p>
 * public @interface ProxyAnno {
 * <p>
 * }
 * <code>
 *
 * @author kamosama
 * @apiNote 注解在AnnotatedElement(可以被注解的元素上)或Annotation实例或其他实例的Class上，赋予排序的能力（部分支持）
 * @since 2.4.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE})
@Documented
public @interface Order {
    /**
     * 序号
     *
     * @author kamosama
     * @apiNote 顺序排序的序号
     */
    int value() default 0;

    /**
     * 默认Order注解比较器
     */
    Comparator<Object> ORDER_COMPARATOR = Comparator.comparingInt(o -> {
        Order order;
        if (o instanceof AnnotatedElement) {
            order = ((AnnotatedElement) o).getAnnotation(Order.class);
        } else if (o instanceof Annotation) {
            order = ((Annotation) o).annotationType().getAnnotation(Order.class);
        } else {
            order = o.getClass().getAnnotation(Order.class);
        }
        return order != null ? order.value() : 0;
    });
}
