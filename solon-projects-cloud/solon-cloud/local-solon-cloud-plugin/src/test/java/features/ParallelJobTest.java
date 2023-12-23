package features;

import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;
import org.noear.solon.cloud.extend.local.impl.job.JobManager;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.event.AppPluginLoadEndEvent;
import org.noear.solon.i18n.I18nUtil;

import java.util.Locale;

/**
 * @author noear 2022/11/21 created
 */
@SolonMain
public class ParallelJobTest {
    public static void main(String[] args) throws Exception {
        //设置定时任务为非并行执行
        JobManager.setParallel(false);
        Solon.start(ParallelJobTest.class, args);
    }
}