package com.rox.app.sync.store;

import java.io.File;
import java.io.InputStream;

/**
 * Created by Rox on 2014/6/29.
 * 定义存储接口
 */
public interface StorageService {
    public boolean exist(String path);

    public void read(String file, File localFile);

    public void write(String path, File file);

    public void mv(String file);

    public void rm(String file);
}
