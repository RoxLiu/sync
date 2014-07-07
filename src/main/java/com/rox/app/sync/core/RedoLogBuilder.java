package com.rox.app.sync.core;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rox on 2014/7/4.
 * 从redo文件中解析重做日志。
 */
public class RedoLogBuilder {

    public List<RedoLog> parse(String path) {
        return parse(path, -1);
    }

    public List<RedoLog> parse(String path, final long version) {
        final List<RedoLog> logs = new ArrayList<>();

        new FileTemplate().read(path, new FileTemplate.ReadLineCallback() {
            @Override
            public void parseLine(String line) {
                try {
                    String[] split = line.split(",");

                    RedoLog log = compose(split);
                    if(log != null) {
                        if(log.getVersion() > version) {
                            logs.add(log);
                        }
                    } else {
                        System.err.println("Failed to convert the row to RedoLog: " + line);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });

        return logs;
    }

    private RedoLog compose(String[] array) {
        RedoLog log = new RedoLog();

        log.setVersion(Long.parseLong(array[0]));
        log.setType(array[1]);
        log.setTarget(array[2]);
        log.setOld(array[3]);
        log.setNow(array[4]);
        log.setLastModifiedTime(Long.parseLong(array[5]));

        return log;
    }

    public void write(String filePath, final List<RedoLog> indexes) {
        new FileTemplate().write(filePath, new FileTemplate.WriteCallback() {
            @Override
            public void doWrite(PrintWriter writer) {
                for(RedoLog log : indexes) {
                    writer.println(log);
                }
            }
        });
    }
}
