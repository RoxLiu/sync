package com.rox.app.sync;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Rox on 2014/6/29.
 */
public class DigestUtils {

    public static String digest(String str) {
        try {
            byte[] bt= str.getBytes();
            MessageDigest  md = MessageDigest.getInstance("SHA-1");
            md.update(bt);

            return bytes2Hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static String bytes2Hex(byte[]bts) {
        StringBuilder des = new StringBuilder();
        String tmp;
        for (int i=0;i<bts.length;i++) {
            tmp=(Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length()==1) {
                des.append("0");
            }
            des.append(tmp);
        }
        return des.toString();
    }
}
