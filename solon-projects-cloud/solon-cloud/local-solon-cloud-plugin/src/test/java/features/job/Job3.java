package features.job;

import org.noear.solon.cloud.CloudJobHandler;
import org.noear.solon.cloud.annotation.CloudJob;
import org.noear.solon.core.handle.Context;

import java.util.Date;

/**
 * 测试非并发执行的定时任务是否有效
 * 执行时sleep1.5秒 打印输出应该是2秒一次 中间有一次执行会跳过
 */
@CloudJob(name = "job3",cron7x = "1s")
public class Job3 implements CloudJobHandler {
    @Override
    public void handle(Context ctx) throws Throwable {
        System.out.println("云端定时任务：job3:" + new Date());
        Thread.sleep(1500);
    }
}
