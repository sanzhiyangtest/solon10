package demo;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.SolonMain;
import org.noear.solon.cloud.metrics.annotation.MeterCounter;
import org.noear.solon.cloud.metrics.annotation.MeterGauge;
import org.noear.solon.cloud.metrics.annotation.MeterLongTimer;
import org.noear.solon.cloud.metrics.annotation.MeterSummary;
import org.noear.solon.cloud.metrics.annotation.MeterTimer;

/**
 * 不需要配置，直接可用
 */
@Controller
@SolonMain
public class App {
    public static void main(String[] args) {
        Solon.start(App.class, args);
    }

    @Mapping("/test")
    @MeterCounter("demo.test")
    public String test(){
        return "test";
    }

    @Mapping("/hello")
    @MeterTimer("demo.hello")
    public String hello(){
        return "hello";
    }


    @Mapping("/gauge")
    @MeterGauge("demo.MeterGauge")
    public String MeterGauge(){
        return "MeterGauge";
    }


    @Mapping("/summary")
    @MeterSummary("demo.MeterSummary")
    public Integer MeterSummary(){
        return 1;
    }

    @Mapping("/longtime")
    @MeterLongTimer("demo.longtime")
    public String MeterLongTimer() throws InterruptedException {
        Thread.sleep(5000);
        return "MeterSummary";
    }
}
