package autoproxy;

import org.noear.solon.annotation.Component;

/**
 * 测试方法自动代理被代理类
 *
 * @author kamosama
 * @since 2.4.5
 * */
@Component
public class AutoProxyService {
    public AutoProxyService(){
        System.out.println("service init this class is "+getClass().getName());
    }

    @ProxyAnno
    @OderAnno
    public void testPoxy(){
        System.out.println("testPoxy");
    }
}
