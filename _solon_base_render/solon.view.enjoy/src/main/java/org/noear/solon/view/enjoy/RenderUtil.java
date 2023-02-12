package org.noear.solon.view.enjoy;

import com.jfinal.template.Engine;
import com.jfinal.template.Template;

import java.util.Map;

public class RenderUtil {
    public static String render(String templateString, Map<String, Object> model) {
        Engine engine = Engine.use();
        return render(engine, templateString, model);
    }

    public static String render(Engine engine, String templateString, Map<String, Object> model) {
        Template template = engine.getTemplateByString(templateString);
        return template.renderToString(model);
    }
}
