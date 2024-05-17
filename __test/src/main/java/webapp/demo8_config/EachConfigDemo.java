package webapp.demo8_config;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;

@Component
public class EachConfigDemo {
    @Inject("${each.a}")
    String eachA;

    @Inject("${each.b}")
    String eachB;

    @Inject("${each.c}")
    String eachC;

    @Inject("${each.d}")
    String eachD;

    @Inject("${each.e}")
    String eachE;

    @Inject("${GLOBAL_EACH}")
    String globalEach;

    @Init
    public void init() {
        System.out.println("each.a: " + eachA);
        System.out.println("each.b: " + eachB);
        System.out.println("each.c: " + eachC);
        System.out.println("each.d: " + eachD);
        System.out.println("each.e: " + eachE);
        System.out.println("GLOBAL_EACH: " + globalEach);

        System.out.println("Solon.cfg() each.a: " + Solon.cfg().get("each.a"));
    }
}
