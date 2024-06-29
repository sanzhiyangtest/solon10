package org.noear.solon.core.handle;

import org.noear.solon.core.util.IoUtil;

import java.io.*;

/**
 * 文件基类
 *
 * @author noear
 * @since 2.8
 */
public abstract class FileBase {
    /**
     * 内容流
     */
    protected InputStream content;
    /**
     * 内容类型（有些地方会动态构建，所以不能只读）
     */
    protected String contentType;
    /**
     * 内容大小
     */
    protected long contentSize;
    /**
     * 文件名（带扩展名，例：demo.jpg）
     */
    protected String name;

    /**
     * 内容流
     */
    public InputStream getContent() throws IOException {
        return content;
    }


    /**
     * 内容类型
     */
    public String getContentType() {
        return contentType;
    }


    /**
     * 内容大小
     */
    public long getContentSize() throws IOException{
        if (contentSize > 0) {
            return contentSize;
        } else {
            return getContent().available();
        }
    }

    /**
     * 文件名（带扩展名，例：demo.jpg）
     */
    public String getName() {
        return name;
    }

    public FileBase() {

    }

    /**
     * 构造函数
     *
     * @param contentType 内容类型
     * @param contentSize 内容大小
     * @param content     内容流
     * @param name        文件名
     */
    public FileBase(String contentType, long contentSize, InputStream content, String name) {
        this.contentType = contentType;
        this.contentSize = contentSize;
        this.content = content;
        this.name = name;
    }

    /**
     * 将内容流迁移到目标文件
     *
     * @param file 目标文件
     */
    public void transferTo(File file) throws IOException {
        try (FileOutputStream stream = new FileOutputStream(file)) {
            IoUtil.transferTo(getContent(), stream);
        }
    }

    /**
     * 将内容流迁移到目标输出流
     *
     * @param stream 目标输出流
     */
    public void transferTo(OutputStream stream) throws IOException {
        IoUtil.transferTo(getContent(), stream);
    }
}