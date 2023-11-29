package org.noear.solon.sessionstate.local;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author lingkang
 * @created in 2023/11/29
 * 定时存储器（做为Session存储方案），基于文件存储session。<br/>
 * 为了应对本地开发，频繁重启导致session丢失问题，将session存储于本地文件<br/>
 * 相较于Redis、db、自定义存储，此类能快速实现session本地存储、重启不丢失。
 * <hr>
 * 例如window时，它将存储于C:\Users\Administrator\AppData\Local\Temp\solon.sessionstate.local下
 * <a href="https://gitee.com/noear/solon/issues/I8KBRY">https://gitee.com/noear/solon/issues/I8KBRY</a>
 **/
class ScheduledFileStore extends ScheduledStore {
    private int _defaultSeconds;

    File tmpdir = new File(System.getProperty("java.io.tmpdir"));
    File folder = null;

    public ScheduledFileStore(int seconds) {
        super(seconds);
        _defaultSeconds = seconds;
        // window
        String[] sys = System.getProperty("user.dir").split("\\\\");
        if (sys.length <= 1)// linux
            sys = System.getProperty("user.dir").split("/");
        String defaultProjectName = null;
        if (sys.length <= 1)
            defaultProjectName = "default";
        else
            defaultProjectName = sys[sys.length - 1];

        folder = new File(tmpdir.getAbsoluteFile() + File.separator + "solon.sessionstate.local"
                + File.separator + defaultProjectName);

        if (!folder.exists())
            folder.mkdirs();
        System.out.println("会话文件存储位置：" + folder.getAbsolutePath());
    }

    /**
     * 返回会话ID集合
     */
    public Collection<String> keys() {
        String[] list = folder.list();
        if (list == null)
            list = new String[0];
        return Arrays.asList(list);
    }

    public void put(String block, String key, Object obj) {
        File file = getFile(block, key);
        if (file != null) {
            file.delete();
        }
        // projectNme/sessionId/keyName
        File newKey = new File(folder.getAbsolutePath() + File.separator + block + File.separator +
                (System.currentTimeMillis() + _defaultSeconds * 1000) + "_" + key);
        serialize(newKey, obj);
    }

    public void delay(String block) {
        // projectNme/sessionId
        File session = new File(folder.getAbsoluteFile() + File.separator + block);
        if (session.exists()) {
            File[] files = session.listFiles();
            if (files != null) {
                long current = System.currentTimeMillis();
                long expire = current + _defaultSeconds * 1000;
                for (File file : files) {
                    // 若文件到期，直接删除
                    if (current > Long.parseLong(file.getName().substring(0, 13))) {
                        file.delete();
                        continue;
                    }
                    // 不到期的，刷新刷到到期时间
                    File newFile = new File(
                            session.getAbsolutePath() + File.separator +
                                    expire + "_" + file.getName().substring(14)
                    );
                    file.renameTo(newFile);
                }
            }
        }
    }

    public Object get(String block, String key) {
        File file = getFile(block, key);
        if (file == null)
            return null;
        return getValue(file);
    }

    public void remove(String block, String key) {
        File file = getFile(block, key);
        if (file != null)
            file.delete();
    }

    public void clear(String block) {
        File[] files = folder.listFiles();
        if (files != null)
            for (File file : files)
                if (file.getName().equals(block)) {
                    file.delete();
                    break;
                }
    }

    public void clear() {
        File[] files = folder.listFiles();
        if (files != null)
            for (File file : files)
                file.delete();
    }

    /**
     * 文件名称：时间戳_key
     */
    private File getFile(String sessionId, String key) {
        File session = new File(folder.getAbsoluteFile() + File.separator + sessionId);
        if (session.listFiles() != null)
            for (File file : session.listFiles()) {
                if (file.getName().endsWith(key)) {
                    return file;
                }
            }
        return null;
    }

    /**
     * 根据文件反序列化内容
     */
    private Object getValue(File file) {
        if (System.currentTimeMillis() > Long.parseLong(file.getName().substring(0, 13))) {
            file.delete();
            return null;
        }
        return unSerialize(file);
    }

    /**
     * 序列化到文件
     */
    private void serialize(File file, Object value) {
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (
                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 反序列化
     */
    private Object unSerialize(File file) {
        try (
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            return ois.readObject();
        } catch (Exception e) {
            System.out.println("反序列化失败，请检查存储对象是否实现了Serializable接口：" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
