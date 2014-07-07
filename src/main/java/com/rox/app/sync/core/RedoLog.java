package com.rox.app.sync.core;

/**
 * Created by Rox on 2014/6/29.
 */
public class RedoLog {
    public static final String TYPE_A = "A";
    public static final String TYPE_M = "M";
    public static final String TYPE_D = "D";

    long version;
    String type;
    String target;
    String old;
    String now;
    private long lastModifiedTime;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getOld() {
        return old;
    }

    public void setOld(String old) {
        this.old = old;
    }

    public String getNow() {
        return now;
    }

    public void setNow(String now) {
        this.now = now;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String toString() {
        return version + ":" + type + ":" + target + ":" + old + ":" + now + ":" + lastModifiedTime;
    }
}
