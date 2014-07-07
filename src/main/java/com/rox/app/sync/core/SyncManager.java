package com.rox.app.sync.core;

import com.rox.app.sync.DigestUtils;
import com.rox.app.sync.store.StorageService;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rox on 2014/7/2.
 * 同步执行类
 */
public class SyncManager {
    private static Logger log = Logger.getLogger(SyncManager.class);
    StorageService storageService;
    String localPath;
    String remotePath;

    //
    IndexBuilder indexBuilder = new IndexBuilder();
    LockBuilder lockBuilder = new LockBuilder();
    RedoLogBuilder redoLogBuilder = new RedoLogBuilder();

    //
    Map<String, Index> oldIndexes;
    RemoteTransactionManager remoteTransactionManager;

    public SyncManager(String local, String remote) {
        this.localPath = local;
        this.remotePath = remote;
    }

    public StorageService getStorageService() {
        return storageService;
    }

    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void execute() {
        beforeSynchronize();

        List<RedoLog> localLogs = composeLocalRedoLogs();
        List<RedoLog> remoteLogs = composeRemoteRedoLogs();

        synchronize(localLogs, remoteLogs);
        //
        afterSynchronize();
    }

    /**
     * 初始化本地的缓存配置
     */
    private void checkLocalCache() throws RuntimeException{
        try {
            log.info("check whether the local cache file exist: " + localPath);
            mkDir(localPath + "/.sync");
            mkFile(localPath + "/.sync/head");
            mkFile(localPath + "/.sync/index");
            mkDir(localPath + "/.sync/conflict");
            mkDir(localPath + "/.sync/remote");
        } catch (IOException e) {
            e.printStackTrace();
            rmDir(localPath + "/.sync");

            throw new RuntimeException("Can't initialize the cache folder: " + e.getLocalizedMessage());
        }
    }

    private List<RedoLog> composeLocalRedoLogs() {
        log.info("generate the redo log according to the snapshot.");
        oldIndexes = indexBuilder.parse(localPath + "/.sync/index");
        Map<String, Index> newIndexes = takeSnapshot();

        IndexComparator indexComparator = new IndexComparator();
        //本次比较产生的操作。

        return indexComparator.compare(oldIndexes, newIndexes);
    }

    private void synchronize(List<RedoLog> redoLogs, List<RedoLog> remoteLogs) {
        Map<String, RedoLog> localMapping = trimAndPutIntoMapping(redoLogs);
        Map<String, RedoLog> remoteMapping = trimAndPutIntoMapping(remoteLogs);

        //conflict
        log.info("find the conflict files between the local and cloud.");
        for(RedoLog log : redoLogs) {
            String key = log.getTarget();
            if (localMapping.containsKey(key) && remoteMapping.containsKey(key)) {
                RedoLog local = localMapping.get(key);
                RedoLog remote = remoteMapping.get(key);

                if(local.getVersion() > remote.getVersion()) {
                    remoteMapping.remove(key);
                } else {
                    localMapping.remove(key);
                }
            }
        }

        log.info("begin to update the local files...");
        for(RedoLog log : remoteMapping.values()) {
            try {
                executeInLocal(log);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        log.info("begin to update the cloud files...");
        for(RedoLog log : localMapping.values()) {
            try {
                executeInRemote(log);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, RedoLog> trimAndPutIntoMapping(List<RedoLog> list) {
        Map<String, RedoLog> mapping = new HashMap<>();
        for(RedoLog log : list) {
            if(!mapping.containsKey(log.getTarget())) {
                mapping.put(log.getTarget(), log);
            } else {
                if(mapping.get(log.getTarget()).getVersion() < log.getVersion()) {
                    mapping.put(log.getTarget(), log);
                }
            }
        }

        return mapping;
    }

    private void executeInLocal(RedoLog redoLog) {
        switch (redoLog.getType()) {
            case RedoLog.TYPE_A:
            case RedoLog.TYPE_M:
                log.info("download the file from cloud: " + redoLog.getTarget());
                downloadFile(redoLog.getNow(), redoLog.getTarget());

                Index index = RemoteTransactionManager.toIndex(redoLog);
                oldIndexes.put(index.getLocalPath(), index);
                break;
            case RedoLog.TYPE_D:
                log.info("remove the file from local: " + redoLog.getTarget());
                deleteLocalFile(redoLog.getTarget());
                oldIndexes.remove(redoLog.getTarget());
                break;
            default:
                System.out.println("Invalid Redo Log: " + redoLog);
        }
    }

    private void executeInRemote(RedoLog redoLog) {
        switch (redoLog.getType()) {
            case RedoLog.TYPE_A: {
                log.info("upload the file to cloud: " + redoLog.getTarget());
                uploadFile(redoLog.getNow(), redoLog.getTarget());
                //add the redo log.
                remoteTransactionManager.addRedoLog(redoLog);
                Index index = RemoteTransactionManager.toIndex(redoLog);
                oldIndexes.put(index.getLocalPath(), index);
                break;
            }
            case RedoLog.TYPE_M: {
                log.info("upload the file to cloud: " + redoLog.getTarget());
                deleteRemoteFile(redoLog.getOld());
                uploadFile(redoLog.getNow(), redoLog.getTarget());
                //add the redo log.
                remoteTransactionManager.addRedoLog(redoLog);
                Index index = RemoteTransactionManager.toIndex(redoLog);
                oldIndexes.put(index.getLocalPath(), index);
                break;
            }
            case RedoLog.TYPE_D: {
                log.info("remove the file from cloud: " + redoLog.getTarget());
                deleteRemoteFile(redoLog.getNow());
                //add the redo log.
                remoteTransactionManager.addRedoLog(redoLog);
                oldIndexes.remove(redoLog.getTarget());
                break;
            }
            default:
                System.out.println("Invalid Redo Log: " + redoLog);
        }
    }

    private  Map<String, Index> takeSnapshot() {
        Map<String, Index> indexes = new HashMap<>();

        File root = new File(localPath);
        createIndex(root, indexes);

        return indexes;
    }

    protected void createIndex(File file, Map<String, Index> indexes) {
        //ignore the .tmp file, which is temp file during the download.
        if(file.isFile() && !file.getName().endsWith(".tmp")) {
            Index index = createFileIndex(file);
            indexes.put(index.getLocalPath(), index);
        } else if(file.isDirectory() && !file.getName().equals(".sync")) {
            File[] children = file.listFiles();

            assert children != null;
            for(File child : children) {
                createIndex(child, indexes);
            }
        }
    }

    protected Index createFileIndex(File file) {
        Index index = new Index();

        index.setLocalPath(file.getAbsolutePath().substring(localPath.length() + 1));
        index.setDigest(DigestUtils.digest(index.getLocalPath() + file.lastModified()));
        index.setLastModifiedTime(file.lastModified());

        return index;
    }

    private List<RedoLog> composeRemoteRedoLogs() {
        log.info("retrieve the redo log from cloud.");
        OptimisticLock lock = lockBuilder.parse(localPath + "/.sync/head");

        return redoLogBuilder.parse(localPath + "/.sync/remote/redo", lock.getOld());
    }

    private void beforeSynchronize() {
        checkLocalCache();
        downloadCacheFiles();

        remoteTransactionManager = new RemoteTransactionManager(localPath);
        remoteTransactionManager.begin();
    }

    private void downloadCacheFiles() {
        try {
            log.info("download the remote index from cloud.");
            if(storageService.exist("index")) {
                storageService.read("index", new File(localPath + "/.sync/remote/index"));
            } else {
                File file =  new File(localPath + "/.sync/remote/index");
                file.delete();
                file.createNewFile();
            }

            log.info("download the remote head from cloud.");
            if(storageService.exist("head")) {
                storageService.read("head", new File(localPath + "/.sync/remote/head"));
            } else {
                File file =  new File(localPath + "/.sync/remote/head");
                file.delete();
                file.createNewFile();
            }

            log.info("download the remote redo from cloud.");
            if(storageService.exist("redo")) {
                storageService.read("redo", new File(localPath + "/.sync/remote/redo"));
            } else {
                File file =  new File(localPath + "/.sync/remote/redo");
                file.delete();
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afterSynchronize() {
        OptimisticLock lock = null;
        try {
            //将云操作写入到本地文件。
            log.info("commit the change to remote cache files.");
            lock = remoteTransactionManager.commit();

            //将本地缓存的云操作文件上传到云。
            log.info("upload the cache files to cloud.");
            uploadCacheFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //生成最新的本地快照
            log.info("create the new snapshot for next synchronization.");
            indexBuilder.write(localPath + "/.sync/index", oldIndexes);
            lockBuilder.write(localPath + "/.sync/head", lock);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadCacheFiles() {
        storageService.write("index", new File(localPath + "/.sync/remote/index"));
        storageService.write("head", new File(localPath + "/.sync/remote/head"));
        storageService.write("redo", new File(localPath + "/.sync/remote/redo"));
    }

    private void downloadFile(String remote, String local) {
        try {
            File tmp = new File(localPath + "/" + local + ".tmp");
            tmp.getParentFile().mkdirs();
            tmp.createNewFile();
            storageService.read(remote, tmp);

            File file = new File(localPath + "/" + local);
            deleteLocalFile(file);

            tmp.renameTo(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void deleteLocalFile(String path) {
        deleteLocalFile(new File(localPath + "/" + path));
    }

    private void deleteLocalFile(File file) {
        if(file.exists() && file.isFile()) {
            file.delete();
        }
    }

    private void uploadFile(String remote, String local) {
        storageService.write(remote, new File(localPath + "/" + local));
    }

    private void deleteRemoteFile(String file) {
        storageService.rm(file);
    }

    private boolean mkDir(String path) {
        File dir = new File(path);
        if(!dir.exists() || dir.isFile()) {
            dir.mkdir();
            return true;
        }

        return false;
    }

    private void rmDir(String path) {
        File dir = new File(path);
        if(dir.exists() && dir.isDirectory()) {
            dir.delete();
        }
    }

    private boolean mkFile(String path) throws IOException{
        File file = new File(path);
        return (!file.exists() || file.isDirectory()) && file.createNewFile();
    }
}
