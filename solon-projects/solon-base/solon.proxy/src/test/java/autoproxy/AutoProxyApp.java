package autoproxy;


import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.proxy.BeanProxy;
import org.noear.solon.proxy.ProxyUtil;

import java.lang.reflect.Modifier;

/**
 * 测试方法自动代理启动类
 *
 * @author kamosama
 * @since 2.4.5
 * */
public class AutoProxyApp {
    public static void main(String[] args) {
        SolonApp solonApp = Solon.start(AutoProxyApp.class, args, (app) -> {
            app.context().beanExtractorAdd(OderAnno.class, (bw, method, anno) -> {
                System.out.println("OderAnno函数抽取执行了");
                System.out.println("拿到的类型是："+bw.raw().getClass());
            });
            app.context().beanExtractorAdd(ProxyAnno.class, (bw, method, anno) -> {
                System.out.println("ProxyAnno函数抽取执行了");
                if (!(bw.proxy() != null || bw.proxy() instanceof ProxyUtil)) {
                    if (bw.clz().isInterface()) {
                        throw new IllegalStateException("Interfaces are not supported as proxy components: " + bw.clz().getName());
                    }

                    int modifier = bw.clz().getModifiers();
                    if (Modifier.isFinal(modifier)) {
                        throw new IllegalStateException("Final classes are not supported as proxy components: " + bw.clz().getName());
                    }

                    if (Modifier.isAbstract(modifier)) {
                        throw new IllegalStateException("Abstract classes are not supported as proxy components: " + bw.clz().getName());
                    }

                    if (!Modifier.isPublic(modifier)) {
                        throw new IllegalStateException("Not public classes are not supported as proxy components: " + bw.clz().getName());
                    }
                    bw.proxySet(BeanProxy.getGlobal());
                }

            });
            app.context().beanInterceptorAdd(ProxyAnno.class, inv -> {
                System.out.println("执行拦截逻辑");
                return inv.invoke();
            });

        });
        solonApp.context().getBean(AutoProxyService.class).testPoxy();
    }
}
