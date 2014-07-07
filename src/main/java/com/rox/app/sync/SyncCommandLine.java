package com.rox.app.sync;

import com.jcloud.jss.JingdongStorageService;
import com.rox.app.sync.core.SyncManager;
import com.rox.app.sync.store.JdStorageService;
import com.rox.app.sync.store.StorageService;
import com.rox.app.tools.ini.IniConfigurationFactory;
import com.rox.app.tools.ini.Property;
import com.rox.app.tools.ini.Section;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 *
 */
public class SyncCommandLine
{
   public static void main( String[] args ) {
       BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
       String line;
       try {
           while((line = reader.readLine()) != null) {
               switch (line) {
                   case "sync":
                       new Sync().start();
                       break;
                   case "clean":
                       break;
               }
           }
       } catch (IOException e) {
           e.printStackTrace();
       }
    }
}