package com.rox.app.sync.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Rox on 2014/7/4.
 * 解析版本信息
 */
public class LockBuilder {
    public LockBuilder() {

    }

    public OptimisticLock parse(String head) {
        final OptimisticLock lock = new OptimisticLock();

        new FileTemplate().read(head, new FileTemplate.ReadCallback() {
            @Override
            public void doRead(BufferedReader reader) {
                try {
                    String line = reader.readLine();

                    if(line != null) {
                        lock.setOld(Long.parseLong(line));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return lock;
    }

    public void write(String filePath, final OptimisticLock lock) {
        new FileTemplate().write(filePath, new FileTemplate.WriteCallback() {
            @Override
            public void doWrite(PrintWriter writer) {
                writer.println(lock);
            }
        });
    }

}
