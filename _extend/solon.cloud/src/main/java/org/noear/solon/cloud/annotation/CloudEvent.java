package org.noear.solon.cloud.annotation;

import org.noear.solon.annotation.Alias;

import java.lang.annotation.*;

/**
 * 云端事件订阅
 *
 * @author noear, iYarnFog
 * @since 1.2
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CloudEvent {
    /**
     * 主题，支持${xxx}配置
     * */
    @Alias("topic")
    String value() default "";

    /**
     * 主题，支持${xxx}配置
     * */
    @Alias("value")
    String topic() default "";

    /**
     * 分组，支持${xxx}配置
     * */
    String group() default "";

    /**
     * 订阅级别
     * */
    EventLevel level() default EventLevel.cluster;

    /**
     * 通道：用于同时支持多个消息框架
     * */
    String channel() default "";

    /**
     * 消息实体, 用于自动填充Group、Channel等信息
     * 注意: Topic如果为空会覆盖为 Class.getName() ！！！
     * 注意: 该选项会覆盖上述所有信息！！！
     **/
    Class<?> entity() default Object.class;

}
