package com.rox.app.sync.core;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rox on 2014/7/2.
 * 快照文件解析类
 */
public class IndexBuilder {
    public IndexBuilder() {
    }

    public Map<String, Index> parse(String file) {
        final Map<String, Index> indexes = new HashMap<>();

        new FileTemplate().read(file, new FileTemplate.ReadLineCallback() {
            @Override
            public void parseLine(String line) {
                try {
                    String[] split = line.split(":");

                    Index index = new Index();
                    index.setLocalPath(split[0]);
                    index.setDigest(split[1]);
                    index.setLastModifiedTime(Long.parseLong(split[2]));

                    indexes.put(index.getLocalPath(), index);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid format line: " + line);
                    e.printStackTrace();
                }
            }
        });

        return indexes;
    }

    public void write(String filePath, final Map<String, Index> indexes) {
        new FileTemplate().write(filePath, new FileTemplate.WriteCallback() {
            @Override
            public void doWrite(PrintWriter writer) {
                for(Index index : indexes.values()) {
                    writer.println(index);
                }
            }
        });
    }
}
