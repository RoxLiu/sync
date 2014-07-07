package com.rox.app.sync.core;

import java.util.List;
import java.util.Map;

/**
 * Created by Rox on 2014/7/7.
 */
public class RemoteTransactionManager {
    private String path;

    IndexBuilder indexBuilder = new IndexBuilder();
    RedoLogBuilder redoLogBuilder = new RedoLogBuilder();
    LockBuilder lockBuilder = new LockBuilder();

    Map<String, Index> indexes;
    List<RedoLog> redoLogs;
    OptimisticLock lock;

    transient boolean dirty = false;
    public RemoteTransactionManager(String path){
        setPath(path);

        init();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private void init() {
        indexes = indexBuilder.parse(path + "/.sync/remote/index");
        redoLogs = redoLogBuilder.parse(path + "/.sync/remote/redo");
        lock = lockBuilder.parse(path + "/.sync/remote/head");
    }

    public void begin() {
        lock.increase();
    }

    public void rollback() {
        init();
    }

    public OptimisticLock commit() {
        if(lock.isChanged()) {
            if(dirty) {
                indexBuilder.write(path + "/.sync/remote/index", indexes);
                redoLogBuilder.write(path + "/.sync/remote/redo", redoLogs);
                lockBuilder.write(path + "/.sync/remote/head", lock);
            } else {
                lock.reset();
            }
        } else {
            throw new RuntimeException("The Info of Remote is not changed!");
        }

        return lock;
    }

    public void addRedoLog(RedoLog log) {
        if (!lock.isChanged()) {
            throw new RuntimeException("The Transaction is not begin, the lock is not marked.");
        }

        log.setVersion(lock.getNow());
        redoLogs.add(log);

        switch (log.getType()) {
            case RedoLog.TYPE_A:
            case RedoLog.TYPE_M:
                indexes.put(log.getTarget(), toIndex(log));
                break;
            case RedoLog.TYPE_D:
                indexes.remove(log.getTarget());
                break;
            default:
                System.out.println("Invalid Redo Log: " + log);
        }

        dirty = true;
    }

    public static Index toIndex(RedoLog log) {
        Index index = new Index();
        index.setLocalPath(log.getTarget());
        index.setDigest(log.getNow());
        index.setLastModifiedTime(log.getLastModifiedTime());

        return index;
    }
}
