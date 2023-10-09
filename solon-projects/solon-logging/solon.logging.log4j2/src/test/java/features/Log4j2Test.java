package features;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.solon.Utils;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * @author noear 2021/12/17 created
 */

@ExtendWith(SolonJUnit5Extension.class)
@SolonTest(App.class)
public class Log4j2Test {

    @Test
    public void test1() throws Exception{
        Logger log = LoggerFactory.getLogger(Log4j2Test.class);

        MDC.put("traceId", Utils.guid());

        log.info("你好");
        log.trace("你不好");
        log.debug("试试");

        MDC.put("tag0", "user_1");

        log.info("你好 {}!", "world!");
        log.warn("你不好 {}!", "world!");

        log.error("出错了", new IllegalStateException());

        Thread.sleep(100);
    }
}
