package org.noear.solon.cloud.tracing.integration;

import org.noear.solon.annotation.ProxyComponent;
import org.noear.solon.scheduling.annotation.Async;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author orangej
 * @since 2023/4/7
 */
@ProxyComponent
public class HelloAsyncService {

    private Logger log = org.slf4j.LoggerFactory.getLogger(HelloAsyncService.class);

    @Async
    public void doSomethingAsync(AtomicBoolean flag) {
        try {
            log.info("doSomethingAsync");
            Thread.sleep(10);
            flag.set(true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
