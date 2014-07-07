package com.rox.app.sync.core;

import java.io.*;

/**
 * Created by Rox on 2014/7/7.
 * 文件读写操作类。
 */
public class FileTemplate {

    public void read(String filePath, ReadLineCallback callback) throws RuntimeException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;

            while((line = reader.readLine()) != null) {
                try {
                    callback.parseLine(line);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void read(String filePath, ReadCallback callback) throws RuntimeException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));

            callback.doRead(reader);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(String filePath, WriteCallback callback) {
        File file = new File(filePath);

        //
        PrintWriter writer = null;
        try {
            if(!file.exists()) {
                boolean created = file.createNewFile();
                if(!created) {
                    System.out.println("The file already exist:" + filePath);
                }
            }

            writer = new PrintWriter(new FileWriter(file));

            callback.doWrite(writer);

            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface ReadLineCallback {
        public void parseLine(String line);
    }

    public interface ReadCallback {
        public void doRead(BufferedReader reader);
    }

    public interface WriteCallback{
        public void doWrite(PrintWriter writer);
    }
}
