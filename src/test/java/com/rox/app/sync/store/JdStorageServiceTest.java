package com.rox.app.sync.store;

import com.jcloud.jss.JingdongStorageService;
import com.jcloud.jss.domain.ObjectListing;
import com.jcloud.jss.domain.ObjectSummary;
import com.jcloud.jss.service.BucketService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

/**
 * Created by Rox on 2014/6/29.
 */
@RunWith(JUnit4.class)
public class JdStorageServiceTest {
    JingdongStorageService jss;

    @Before
    public void setup() {
        jss = new JingdongStorageService("12c3ad946afa42d4bf1f2fcc22623930", "10f5ed1590094017b6495b493b9d67691H0W8RYk");
    }

    @Test
    public void cleanCloudFiles() {
        BucketService bucket = jss.bucket("rox-libary");
        ObjectListing list = bucket.listObject();
        List<ObjectSummary> fileList = list.getObjectSummaries();

        for(ObjectSummary obj : fileList) {
            bucket.object(obj.getKey()).delete();
        }
    }
}
