package org.noear.solon.cloud.extend.local.impl.job;

import org.noear.solon.Utils;
import org.noear.solon.cloud.extend.local.impl.job.cron.CronExpressionPlus;
import org.noear.solon.cloud.extend.local.impl.job.cron.CronUtils;

import java.text.ParseException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务管理器
 *
 * @author noear
 * @since 1.6
 */
public class JobManager {
    private static Map<String, JobEntity> jobEntityMap = new HashMap<>();

    /**
     * 是否支持并发执行
     * 为true时，只要定时时间到了就直接执行，同一个定时任务，同一时间可能有多个线程同时执行
     * 为false时，定时时间到了之后，如果当前正在执行，会直接跳过，直到下一次定时任务
     * 通过 JobManager.setParallel 方法设置，默认为true
     */
    private static Boolean parallel = true;

    /**
     * 执行状态判断
     */
    private static Map<Integer, Boolean> execFlagMap = new ConcurrentHashMap<>();

    /**
     * 设置定时任务执行时是否支持并发执行
     * @param parallelFlag
     */
    public static void setParallel(Boolean parallelFlag){
        parallel = parallelFlag;
    }

    /**
     * 执行单次任务
     * 根据是否控制并发属性，控制是否支持并行执行
     * @param runnable
     */
    public static void executeJob(Runnable runnable){
        if(parallel){
            //支持并发执行时，直接执行
            runnable.run();
        }else{
            int jobHashKey = System.identityHashCode(runnable);
            if(Boolean.TRUE.equals(execFlagMap.get(jobHashKey))){
                //当前正在执行中，直接跳过此次之行
                return;
            }
            try{
                execFlagMap.put(jobHashKey, Boolean.TRUE);
                //真正执行
                runnable.run();
            }finally {
                execFlagMap.remove(jobHashKey);
            }
        }
    }
    private static boolean isStarted = false;

    /**
     * 添加计划任务
     *
     * @param name     任务名称
     * @param cron     cron 表达式
     * @param runnable 运行函数
     */
    public static void add(String name, String description, String cron, Runnable runnable) throws ParseException {
        CronExpressionPlus cronX = CronUtils.get(cron);
        addDo(name, new JobEntity(name,description, cronX, runnable));
    }

    /**
     * 添加计划任务
     *
     * @param name     任务名称
     * @param cron     cron 表达式
     * @param zone     时区(+08)
     * @param runnable 运行函数
     */
    public static void add(String name, String description, String cron, String zone,  Runnable runnable) throws ParseException {
        CronExpressionPlus cronX = CronUtils.get(cron);

        if (Utils.isNotEmpty(zone)) {
            cronX.setTimeZone(TimeZone.getTimeZone(ZoneId.of(zone)));
        }

        addDo(name, new JobEntity(name, description, cronX, runnable));
    }

    /**
     * 添加计划任务
     *
     * @param name      任务名称
     * @param fixedRate 固定间隔毫秒数
     * @param runnable  运行函数
     */
    public static void add(String name, String description, long fixedRate, Runnable runnable) {
        addDo(name, new JobEntity(name,description, fixedRate,  runnable));
    }

    /**
     * 添加计划任务
     *
     * @param name      任务名称
     * @param jobEntity 任务实体
     */
    private static void addDo(String name, JobEntity jobEntity) {
        jobEntityMap.putIfAbsent(name, jobEntity);

        if (isStarted) {
            //如果已开始，则直接开始调度
            jobEntity.start();
        }
    }

    /**
     * 检查计划任务是否存在
     *
     * @param name 任务名称
     */
    public static boolean contains(String name) {
        return jobEntityMap.containsKey(name);
    }

    /**
     * 获取任务描述信息
     * 任务不存在或者不存在描述信息时返回null
     * @param name 任务名称
     */
    public static String getDescription(String name) {
        if(contains(name)){
            return jobEntityMap.get(name).getDescription();
        }
        return null;
    }


    /**
     * 任务数量
     */
    public static int count() {
        return jobEntityMap.size();
    }

    /**
     * 移除计划任务
     *
     * @param name 任务名称
     */
    public static void remove(String name) {
        JobEntity jobEntity = jobEntityMap.get(name);
        if (jobEntity != null) {
            jobEntity.cancel();
            jobEntityMap.remove(name);
        }
    }

    public static void reset(String name, long fixedRate) {
        JobEntity jobEntity = jobEntityMap.get(name);

        if (jobEntity != null) {
            jobEntity.reset(null, fixedRate);
        }
    }

    public static void reset(String name, String cron) throws ParseException {
        JobEntity jobEntity = jobEntityMap.get(name);

        if (jobEntity != null) {
            CronExpressionPlus cronX = CronUtils.get(cron);
            jobEntity.reset(cronX, 0);
        }
    }

    /**
     * 获取执行函数
     */
    public static Runnable getRunnable(String name) {
        JobEntity jobEntity = jobEntityMap.get(name);
        if (jobEntity != null) {
            return jobEntity.runnable;
        } else {
            return null;
        }
    }

    /**
     * 开启
     */
    public static void start() {
        for (JobEntity job : jobEntityMap.values()) {
            job.start();
        }
        isStarted = true;
    }

    /**
     * 停止
     */
    public static void stop() {
        for (JobEntity job : jobEntityMap.values()) {
            job.cancel();
        }
        isStarted = false;
    }
}