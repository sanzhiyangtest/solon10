package org.noear.solon.serialization.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.noear.solon.Utils;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.serialization.ContextSerializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Fastjson 字符串序列化
 *
 * @author noear
 * @since 1.5
 * @since 2.8
 */
public class FastjsonStringSerializer implements ContextSerializer<String> {
    private static final String label = "/json";

    private SerializeConfig serializeConfig;
    private int serializerFeatures = JSON.DEFAULT_GENERATE_FEATURE;
    private ParserConfig deserializeConfig;
    private int deserializeFeatures = JSON.DEFAULT_PARSER_FEATURE;

    public SerializeConfig getSerializeConfig() {
        if (serializeConfig == null) {
            serializeConfig = new SerializeConfig();
        }

        return serializeConfig;
    }

    public void cfgSerializerFeatures(boolean isReset, boolean isAdd, SerializerFeature... features) {
        if (isReset) {
            serializerFeatures = JSON.DEFAULT_GENERATE_FEATURE;
        }

        for (SerializerFeature feature : features) {
            if (isAdd) {
                serializerFeatures |= feature.getMask();
            } else {
                serializerFeatures &= ~feature.getMask();
            }
        }
    }

    public ParserConfig getDeserializeConfig() {
        if (deserializeConfig == null) {
            deserializeConfig = new ParserConfig();
        }

        return deserializeConfig;
    }

    public void cfgDeserializeFeatures(boolean isReset, boolean isAdd, Feature... features) {
        if (isReset) {
            deserializeFeatures = JSON.DEFAULT_GENERATE_FEATURE;
        }

        for (Feature feature : features) {
            if (isAdd) {
                deserializeFeatures |= feature.getMask();
            } else {
                deserializeFeatures &= ~feature.getMask();
            }
        }

    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public boolean matched(Context ctx, String mime) {
        if (mime == null) {
            return false;
        } else {
            return mime.contains(label);
        }
    }

    @Override
    public String name() {
        return "fastjson-json";
    }

    @Override
    public String serialize(Object obj) throws IOException {
        if (serializeConfig == null) {
            return JSON.toJSONString(obj, serializerFeatures);
        } else {
            return JSON.toJSONString(obj, serializeConfig, new SerializeFilter[0], null, serializerFeatures);
        }
    }

    @Override
    public Object deserialize(String data, Type toType) throws IOException {
        if (toType == null) {
            if (deserializeConfig == null) {
                return JSON.parse(data, deserializeFeatures);
            } else {
                return JSON.parse(data, deserializeConfig, deserializeFeatures);
            }
        } else {
            if (deserializeConfig == null) {
                return JSON.parseObject(data, toType, deserializeFeatures);
            } else {
                return JSON.parseObject(data, toType, deserializeConfig, deserializeFeatures);
            }
        }
    }

    @Override
    public void serializeToBody(Context ctx, Object data) throws IOException {
        ctx.contentType(getContentType());

        if (data instanceof ModelAndView) {
            ctx.output(serialize(((ModelAndView) data).model()));
        } else {
            ctx.output(serialize(data));
        }
    }

    @Override
    public Object deserializeFromBody(Context ctx) throws IOException {
        String data = ctx.bodyNew();

        if (Utils.isNotEmpty(data)) {
            if (deserializeConfig == null) {
                return JSON.parse(data, deserializeFeatures);
            } else {
                return JSON.parse(data, deserializeConfig, deserializeFeatures);
            }
        } else {
            return null;
        }
    }

    public Object deserializeFromBody234(Context ctx) throws IOException {
        String data = ctx.bodyNew();

        if (Utils.isNotEmpty(data)) {
            if (deserializeConfig == null) {
                return JSON.parse(data, deserializeFeatures);
            } else {
                return JSON.parse(data, deserializeConfig, deserializeFeatures);
            }
        } else {
            return null;
        }
    }
}
