package demo;

import org.noear.solon.Solon;

/**
 * @author noear 2021/12/21 created
 */
public class DemoApp {
    public static void main(String[] args) {
        Solon.start(DemoApp.class, args, app -> {
            app.get("/", c -> c.redirect("/healthz"));
        });
    }
}
