package com.dukexx.boot.shiro.commons;

import java.io.*;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class SerializeUtils {

    /**
     * 序列化为byte[]
     * @param object
     * @return
     */
    public static byte[] serializable(Object object) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            byte[] bytes=byteArrayOutputStream.toByteArray();
            objectOutputStream.close();
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 反序列化为Object
     * @param bytes
     * @return
     */
    public static Object deSerializable(byte[] bytes) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object o = objectInputStream.readObject();
            objectInputStream.close();
            return o;
        } catch (IOException |ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
