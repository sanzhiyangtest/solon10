package org.noear.solon.cloud.tracing.integration;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.propagation.B3TextMapCodec;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import org.noear.solon.SolonBuilder;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.cloud.tracing.TracingManager;
import org.noear.solon.scheduling.annotation.EnableAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author orangej
 * @since 2023/4/7
 */
@org.noear.solon.annotation.Configuration
@EnableAsync
@Controller
public class HelloApp {
    private Logger log = LoggerFactory.getLogger(HelloApp.class);

    @Inject
    HelloAsyncService helloAsyncService;

    public static void main(String[] args) {
        new SolonBuilder().onAppInitEnd(e -> {
            TracingManager.enable("");
        }).start(HelloApp.class, args);
    }

    @Bean
    public Tracer tracer() {
        return new io.jaegertracing.Configuration("hello-app")
                .withSampler(new Configuration.SamplerConfiguration())
                .withReporter(new Configuration.ReporterConfiguration())
                .getTracerBuilder()
                .registerExtractor(Format.Builtin.HTTP_HEADERS, new B3TextMapCodec())
                .registerInjector(Format.Builtin.HTTP_HEADERS, new B3TextMapCodec())
                .build();
    }

    @Mapping("/hello")
    public String hello() {
        log.info("hello");
        return MDC.get("X-TraceId");
    }

    @Mapping("/hello/async")
    public void helloAsync() {
        log.info("helloAsync start");

        AtomicBoolean flag = new AtomicBoolean(false);
        helloAsyncService.doSomethingAsync(flag);
        while (!flag.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        log.info("helloAsync done");
    }
}
