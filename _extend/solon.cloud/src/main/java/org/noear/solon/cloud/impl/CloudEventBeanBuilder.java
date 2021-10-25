package org.noear.solon.cloud.impl;

import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.cloud.CloudClient;
import org.noear.solon.cloud.CloudEventHandler;
import org.noear.solon.cloud.CloudManager;
import org.noear.solon.cloud.annotation.CloudEvent;
import org.noear.solon.core.BeanBuilder;
import org.noear.solon.core.BeanWrap;

/**
 * @author noear, iYarnFog
 * @since 1.4
 */
public class CloudEventBeanBuilder implements BeanBuilder<CloudEvent> {
    public static final CloudEventBeanBuilder instance = new CloudEventBeanBuilder();

    @Override
    public void doBuild(Class<?> clz, BeanWrap bw, CloudEvent anno) throws Exception {
        if (CloudClient.event() == null) {
            throw new IllegalArgumentException("Missing CloudEventService component");
        }

        String topic = "";

        if (anno.entity() != Object.class) {
            // 默认 Topic 为 事件实体类的 Class Reference
            topic = anno.entity().getName();
            anno = anno.entity().getAnnotation(CloudEvent.class);
            if (anno == null) {
                throw new IllegalArgumentException("Missing CloudEvent annotation on event entity.");
            }
        }

        if (bw.raw() instanceof CloudEventHandler) {
            CloudManager.register(anno, bw.raw());

            if (CloudClient.event() != null) {
                // 如果用户设置了 Topic 就覆盖掉
                if (anno.value().isEmpty() && anno.topic().isEmpty()) {
                    //支持${xxx}配置
                    topic = Solon.cfg().getByParse(Utils.annoAlias(anno.value(), anno.topic()));
                }
                //支持${xxx}配置
                String group = Solon.cfg().getByParse(anno.group());

                //关注事件
                CloudClient.event().attention(anno.level(), anno.channel(), group, topic, bw.raw());
            }
        }
    }

}
