package com.rox.app.sync.core;

/**
 * Created by Rox on 2014/7/2.
 * 格式：
 * path,digest,size,lastModifiedTime,
 */
public class Index {
    private String localPath;
    private String digest;
    private long lastModifiedTime;

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String toString() {
        return localPath + "," + digest + "," + lastModifiedTime;
    }
}
