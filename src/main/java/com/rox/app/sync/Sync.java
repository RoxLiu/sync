package com.rox.app.sync;

import com.jcloud.jss.JingdongStorageService;
import com.rox.app.sync.core.SyncManager;
import com.rox.app.sync.store.JdStorageService;
import com.rox.app.sync.store.StorageService;
import com.rox.app.tools.ini.IniConfigurationFactory;
import com.rox.app.tools.ini.Property;
import com.rox.app.tools.ini.Section;

import java.util.List;

/**
 *
 */
public class Sync
{
    StorageService storageService;
    IniConfigurationFactory cf;

    public void start() {
        init();

        List<Property> properties = cf.section("folder").getProperties();
        for(Property property : properties) {
            String folder = property.getKey();
            String remote = property.getValue();
            doSync(folder, remote);
        }
    }

    protected void init() {
        cf = new IniConfigurationFactory("./sync.ini");

        String engine = cf.section("storage").property("engine").getValue();
        if(engine == null) {
            System.err.println("Can't find the storage definition in sync.ini: the engine attribute is not defined.");
            System.exit(-1);
        }

        storageService = buildStorageService(cf.section(engine));
    }

    private StorageService buildStorageService(Section section) {
        if("jss".equals(section.getName())) {
            JingdongStorageService jd = new JingdongStorageService(section.property("accessKey").getValue(), section.property("secretKey").getValue());

            JdStorageService storageService = new JdStorageService();
            storageService.setJss(jd);

            return storageService;
        }

        throw new RuntimeException("Not Supported StorageService: " + section);
    }

    protected void doSync(String local, String remote) {
        SyncManager manager = new SyncManager(local, remote);
        manager.setStorageService(storageService);
        ((JdStorageService)storageService).setBucket(remote);

        manager.execute();
    }


    public static void main( String[] args ) {
        Sync sync = new Sync();
        sync.start();
    }
}