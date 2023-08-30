package autoproxy;

import org.noear.solon.annotation.Order;

import java.lang.annotation.*;
/**
 * 测试方法自动代理注解
 *
 * @author kamosama
 * @since 2.4.5
 * @apiNote 设置为Order(-1)比默认方法先执行
 * */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Order(-1)
public @interface ProxyAnno {
}
