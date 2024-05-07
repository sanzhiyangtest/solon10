package org.noear.solon;

import org.noear.solon.core.AppContext;
import org.noear.solon.core.AppClassLoader;
import org.noear.solon.core.runtime.NativeDetector;
import org.noear.solon.core.NvMap;
import org.noear.solon.core.event.AppPrestopEndEvent;
import org.noear.solon.core.event.AppStopEndEvent;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.util.ConsumerEx;
import org.noear.solon.core.util.LogUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * 应用管理中心
 *
 * <pre><code>
 * @SolonMain
 * public class DemoApp{
 *     public static void main(String[] args){
 *         Solon.start(DemoApp.class, args);
 *     }
 * }
 * </code></pre>
 *
 * @author noear
 * @since 1.0
 * */
public class Solon {
    //全局实例（可能会因为测试而切换）
    private static SolonApp app;
    //全局主实例
    private static SolonApp appMain;
    //全局默认编码
    private static String encoding = "utf-8";

    /**
     * 框架版本号
     */
    public static String version() {
        return "2.8.0-M3";
    }

    /**
     * 全局实例
     */
    public static SolonApp app() {
        return app;
    }

    /**
     * 设置全局实例（仅用内部用，一般用于单测隔离）
     */
    protected static void appSet(SolonApp solonApp) {
        if (solonApp != null) {
            app = solonApp;
        }
    }

    /**
     * 应用配置
     */
    public static SolonProps cfg() {
        if (app == null) {
            return null;
        } else {
            return app.cfg();
        }
    }

    /**
     * 应用上下文
     */
    public static AppContext context() {
        if (app == null) {
            return null;
        } else {
            return app.context();
        }
    }

    /**
     * 全局默认编码
     */
    public static String encoding() {
        return encoding;
    }


    /**
     * 全局默认编码设置
     */
    public static void encodingSet(String charset) {
        //只能在初始化之前设置
        if (app == null && Utils.isNotEmpty(charset)) {
            encoding = charset;
        }
    }

    /**
     * 启动应用（全局只启动一个）
     *
     * @param source 主应用包（用于定制Bean所在包）
     * @param args   启动参数
     */
    public static SolonApp start(Class<?> source, String[] args) {
        return start(source, args, null);
    }

    /**
     * 启动应用（全局只启动一个）
     *
     * @param source     主应用包（用于定制Bean所在包）
     * @param args       启动参数
     * @param initialize 实始化函数
     */
    public static SolonApp start(Class<?> source, String[] args, ConsumerEx<SolonApp> initialize) {
        //1.初始化应用，加载配置
        NvMap argx = NvMap.from(args);
        return start(source, argx, initialize);
    }

    /**
     * 启动应用（全局只启动一个）
     *
     * @param source     主应用包（用于定制Bean所在包）
     * @param argx       启动参数
     * @param initialize 实始化函数
     */
    public static SolonApp start(Class<?> source, NvMap argx, ConsumerEx<SolonApp> initialize) {
        if (appMain != null) {
            app = appMain; //有可能被测试给切走了
            return appMain;
        }

        //设置文件编码
        if (Utils.isNotEmpty(encoding)) {
            System.setProperty("file.encoding", encoding);
        }

        //设置 headless 默认值
        System.getProperties().putIfAbsent("java.awt.headless", "true");

        //确定PID
        String pid = Utils.pid();

        //绑定类加载器（即替换当前线程[即主线程]的类加载器）
        AppClassLoader.bindingThread();

        try {
            //1.创建全局应用及配置
            app = appMain = new SolonApp(source, argx);

            LogUtil.global().info("App: Start loading");//调整了打印时机

            //2.开始
            app.start(initialize);

        } catch (Throwable e) {
            //显示异常信息
            e = Utils.throwableUnwrap(e);
            LogUtil.global().error("Solon start failed: " + e.getMessage(), e);

            //3.停止服务并退出（主要是停止插件）
            if (NativeDetector.isNotAotRuntime()) {
                Solon.stop0(true, 0);
            } else {
                Solon.stop0(false, 0);

                throw new IllegalStateException("Solon start failed", e);
            }
        }


        //4.初始化安全停止
        if (NativeDetector.isNotAotRuntime()) {
            if (app.cfg().stopSafe()) {
                //添加关闭勾子
                Runtime.getRuntime().addShutdownHook(new Thread(() -> Solon.stop0(false, app.cfg().stopDelay())));
            } else {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> Solon.stop0(false, 0)));
            }
        }

        //5.启动完成

        LogUtil.global().info("App: End loading elapsed=" + app.elapsedTimes() + "ms pid=" + pid + " v=" + Solon.version());

        return app;
    }

    /**
     * 停止应用
     */
    public static void stop() {
        stop(app.cfg().stopDelay());
    }

    /**
     * 停止应用
     *
     * @param delay 延迟时间（单位：秒）
     */
    public static void stop(int delay) {
        new Thread(() -> stop0(true, delay)).start();
    }

    /**
     * 停止应用（未完成之前，会一直卡住）
     *
     * @param exit  是否退出进程
     * @param delay 延迟时间（单位：秒）
     */
    public static void stopBlock(boolean exit, int delay) {
        stop0(exit, delay);
    }

    /**
     * 停止应用（未完成之前，会一直卡住）
     *
     * @param exit       是否退出进程
     * @param delay      延迟时间（单位：秒）
     * @param exitStatus 退出状态码
     */
    public static void stopBlock(boolean exit, int delay, int exitStatus) {
        stop0(exit, delay, exitStatus);
    }

    private static void stop0(boolean exit, int delay) {
        stop0(exit, delay, 1);
    }

    private static void stop0(boolean exit, int delay, int exitStatus) {
        if (Solon.app() == null) {
            return;
        }


        if (delay > 0) {
            LogUtil.global().info("App: Security to stop: begin...(1.prestop 2.delay 3.stop)");

            //1.预停止
            Solon.cfg().plugs().forEach(p -> p.prestop());
            Solon.context().prestop();
            EventBus.publishTry(new AppPrestopEndEvent(Solon.app()));
            LogUtil.global().info("App: Security to stop: 1/3 completed");


            //2.延时标停
            LogUtil.global().info("App: Security to stop: delay " + delay + "s...");
            int delay1 = (int) (delay * 0.3);
            int delay2 = delay - delay1;

            //一段暂停
            if (delay1 > 0) {
                sleep0(delay1); //给发现服务留时间
            }

            Solon.app().stopped = true; //http 503，lb 开始切流

            //二段暂停
            if (delay2 > 0) {
                sleep0(delay2); //消化已有请求
            }

            LogUtil.global().info("App: Security to stop: 2/3 completed");

            //3.停止
            Solon.cfg().plugs().forEach(p -> p.stop());
            Solon.context().stop();
            EventBus.publishTry(new AppStopEndEvent(Solon.app()));
            LogUtil.global().info("App: Security to stop: 3/3 completed");
        } else {
            //1.预停止
            Solon.cfg().plugs().forEach(p -> p.prestop());
            Solon.context().prestop();
            EventBus.publishTry(new AppPrestopEndEvent(Solon.app()));

            //2.标停
            Solon.app().stopped = true;
            //3.停止
            Solon.cfg().plugs().forEach(p -> p.stop());
            Solon.context().stop();
            EventBus.publishTry(new AppStopEndEvent(Solon.app()));
        }

        app = null;
        appMain = null;


        //4.直接非正常退出
        if (exit) {
            System.exit(exitStatus);
        }
    }

    private static void sleep0(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (Throwable ignored) {

        }
    }
}