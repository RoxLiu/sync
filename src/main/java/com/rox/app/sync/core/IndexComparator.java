package com.rox.app.sync.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rox on 2014/7/4.
 * 快照比较类。
 */
public class IndexComparator {
    public List<RedoLog> compare(Map<String, Index> oldMapping, Map<String, Index> nowMapping) {
        //clone the mapping because it will be modified in this method.
        Map<String, Index> tmpIndexes = new HashMap<>();
        tmpIndexes.putAll(oldMapping);

        List<RedoLog> logs = new ArrayList<>();

        int i = 0;
        for(Index index : nowMapping.values()) {
            Index oldIndex = tmpIndexes.remove(index.getLocalPath());

            if(oldIndex == null) {
                logs.add(toAddRedoLog(index));
            } else if(!oldIndex.getDigest().equals(index.getDigest())) {
                logs.add(toModifyRedoLog(oldIndex, index));
            }
        }

        for(Index index : tmpIndexes.values()) {
            logs.add(toDeleteRedoLog(index));
        }

        return logs;
    }

    private RedoLog toAddRedoLog(Index index) {
        RedoLog log = new RedoLog();
        log.setType(RedoLog.TYPE_A);
        log.setTarget(index.getLocalPath());
        log.setNow(index.getDigest());
        log.setLastModifiedTime(index.getLastModifiedTime());

        return log;
    }

    private RedoLog toModifyRedoLog(Index old, Index now) {
        RedoLog log = new RedoLog();
        log.setType(RedoLog.TYPE_M);
        log.setTarget(old.getLocalPath());

        if(old.getLastModifiedTime() > now.getLastModifiedTime()) {
            log.setNow(old.getDigest());
            log.setOld(now.getDigest());
            log.setLastModifiedTime(old.getLastModifiedTime());
        } else {
            log.setNow(now.getDigest());
            log.setOld(old.getDigest());
            log.setLastModifiedTime(now.getLastModifiedTime());
        }

        return log;
    }

    private RedoLog toDeleteRedoLog(Index index) {
        RedoLog log = new RedoLog();
        log.setType(RedoLog.TYPE_D);
        log.setTarget(index.getLocalPath());
        log.setNow(index.getDigest());
        log.setLastModifiedTime(index.getLastModifiedTime());

        return log;
    }
}
