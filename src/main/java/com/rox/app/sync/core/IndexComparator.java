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
                logs.add(toModifyRedoLog(index, oldIndex));
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

    private RedoLog toModifyRedoLog(Index one, Index another) {
        RedoLog log = new RedoLog();
        log.setType(RedoLog.TYPE_M);
        log.setTarget(one.getLocalPath());

        if(one.getLastModifiedTime() > another.getLastModifiedTime()) {
            log.setNow(one.getDigest());
            log.setOld(another.getDigest());
            log.setLastModifiedTime(one.getLastModifiedTime());
        } else {
            log.setNow(another.getDigest());
            log.setOld(one.getDigest());
            log.setLastModifiedTime(another.getLastModifiedTime());
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
