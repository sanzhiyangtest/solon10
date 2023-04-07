package org.noear.solon.cloud.tracing.integration;

import io.opentracing.Span;
import io.opentracing.Tracer;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.AopContext;
import org.noear.solon.core.aspect.Invocation;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.scheduling.async.IAsyncInvocationRunnableFactory;

import java.lang.annotation.Annotation;

/**
 * @author orangej
 * @since 2023/4/7
 */
public class AsyncInvocationTracing {

    public static void enableTracing() {
        if (!isAsyncEnabled()) {
            return;
        }

        AopContext context = Solon.context();
        if (context == null) {
            return;
        }

        // 注入 TracingAsyncInvocationFactory，替换默认的 DefaultAsyncInvocationRunnableFactory
        context.wrapAndPut(IAsyncInvocationRunnableFactory.class, new TracingAsyncInvocationFactory());
    }

    /**
     * 判断是否开启了异步
     */
    static boolean isAsyncEnabled() {
        Annotation[] annoArr = Solon.app().source().getAnnotations();
        for (Annotation anno : annoArr) {
            if (anno.annotationType().getName().endsWith("EnableAsync")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 带日志跟踪的 IAsyncInvocationRunnableFactory
     */
    public static class TracingAsyncInvocationFactory implements IAsyncInvocationRunnableFactory {

        private Tracer tracer;

        public TracingAsyncInvocationFactory() {
            //跟踪器注入
            Solon.context().getBeanAsync(Tracer.class, bean -> {
                tracer = bean;
            });
        }

        @Override
        public Runnable create(Invocation inv) {
            Tracer.SpanBuilder spanBuilder = tracer.buildSpan(inv.method().getMethod().getName());
            final Span span = spanBuilder.start();

            return () -> {
                try {
                    TracingMDCUtils.inject(span);

                    inv.invoke();
                } catch (Throwable e) {
                    span.log(Utils.throwableToString(e));
                    EventBus.pushTry(e);
                } finally {
                    span.finish();
                    TracingMDCUtils.clear();
                }
            };
        }
    }
}
