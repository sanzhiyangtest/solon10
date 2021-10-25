package demo;

import org.noear.solon.cloud.CloudEventHandler;
import org.noear.solon.cloud.annotation.CloudEvent;
import org.noear.solon.cloud.model.Event;

public class AdvanceCloudEvent {

    /**
     * topic 会被注入为 demo.AdvanceCloudEvent.OrderPayEvent
     */
    @CloudEvent(group = "service")
    public static class OrderPayEvent {}

    @CloudEvent(entity = OrderPayEvent.class)
    public static class OrderPayListener implements CloudEventHandler {
        @Override
        public boolean handler(Event event) {
            //返回成功
            return true;
        }
    }

}
