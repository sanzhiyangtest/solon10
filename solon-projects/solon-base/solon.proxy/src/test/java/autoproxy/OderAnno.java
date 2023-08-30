package autoproxy;

import java.lang.annotation.*;
/**
 * 测试方法提取器Order顺序注解
 *
 * @author kamosama
 * @since 2.4.5
 * @apiNote 无Order默认为0，比ProxyAnno注解的方法提取器晚执行，可以拿到代理后的对象
 * */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OderAnno {

}
