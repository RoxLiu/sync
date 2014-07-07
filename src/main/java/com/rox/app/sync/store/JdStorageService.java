package com.rox.app.sync.store;

import com.jcloud.jss.JingdongStorageService;
import com.jcloud.jss.exception.StorageClientException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Rox on 2014/6/29.
 */
public class JdStorageService implements StorageService {
    JingdongStorageService jss;
    String bucket;

    public JingdongStorageService getJss() {
        return jss;
    }

    public void setJss(JingdongStorageService jss) {
        this.jss = jss;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    @Override
    public boolean exist(String path) {
        return jss.bucket(bucket).object(path).exist();
    }

    @Override
    public void read(String file, File localFile) {
        try {
            jss.bucket(bucket).object(file).get().toFile(localFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(String file, File from) {
        String md5 = jss.bucket(bucket).object(file).entity(from).put();
    }

    @Override
    public void mv(String file) {

    }

    @Override
    public void rm(String file) {
        try {
            jss.bucket(bucket).object(file).delete();
        } catch (StorageClientException e) {
            e.printStackTrace();
        }
    }
}
