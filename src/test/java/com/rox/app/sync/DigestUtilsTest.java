package com.rox.app.sync;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by Rox on 2014/6/29.
 */
public class DigestUtilsTest {
    @Test
    public void testDigest() {
        String digest = DigestUtils.digest("1111");

        System.out.println(digest);
        Assert.assertNotNull(digest);

        digest = DigestUtils.digest("https://www.google.com.hk/search?q=%E9%87%8D%E5%81%9A%E6%97%A5%E5%BF%97%E6%A0%BC%E5%BC%8F%E8%AE%BE%E8%AE%A1&newwindow=1&safe=strict&es_sm=93&ei=4ryvU6PBHsSulQXW3oG4BA&start=30&sa=N&biw=1366&bih=653");
        System.out.println(digest);
        Assert.assertNotNull(digest);

        digest = DigestUtils.digest("./程序员之路/数据库/数据库系统概念(原书第五版).pdf");
        System.out.println(digest);
        Assert.assertNotNull(digest);
    }
}
