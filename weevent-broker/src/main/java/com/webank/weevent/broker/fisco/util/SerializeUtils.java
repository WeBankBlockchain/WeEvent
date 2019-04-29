package com.webank.weevent.broker.fisco.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * serialize object for redis to read and write data
 */
@Slf4j
@SuppressWarnings(value = "unchecked")
public class SerializeUtils {
    /**
     * serialize object to bytes
     *
     * @param object object
     * @return buf
     */
    public static <T> byte[] serialize(T object) {
        if (object == null) {
            throw new NullPointerException("Can't serialize null");
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject(object);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("Exception happened while serialize", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * deserialize bytes to object
     *
     * @param in buf
     * @return object
     */
    public static <T> T deserialize(byte[] in) {
        if (in != null) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(in);
                 ObjectInputStream is = new ObjectInputStream(bis)) {


                return (T) is.readObject();
            } catch (IOException | ClassNotFoundException e) {
                log.error("Exception happened while deserialize", e);
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    /**
     * serialize object list to bytes
     *
     * @param objectList object list
     * @return buf
     */
    public static <T> byte[] serializeList(List<T> objectList) {
        if (objectList == null) {
            throw new NullPointerException("Can't serialize null");
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream os = new ObjectOutputStream(bos)) {
            for (T object : objectList) {
                os.writeObject(object);
            }
            os.writeObject(null);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("Exception happened while serialize", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * deserialize bytes to object list
     *
     * @param in buf
     * @return object list
     */
    public static <T> List<T> deserializeList(byte[] in) {
        List<T> objectList = new ArrayList<>();

        if (in != null) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(in);
                 ObjectInputStream is = new ObjectInputStream(bis)) {
                while (true) {
                    T obj = (T) is.readObject();
                    if (obj == null) {
                        break;
                    } else {
                        objectList.add(obj);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                log.error("Exception happened while deserialize", e);
                throw new RuntimeException(e);
            }
        }

        return objectList;
    }

}
