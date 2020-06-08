package com.webank.weevent.governance.utils;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * Utils tools.
 *
 * @author matthewliu
 * @since 2020/02/09
 */
public class Utils {

    private final static String processorServiceId = "weevent-processor";

    private final static String processorServiceUrl = "http://127.0.0.1:7008";

    private final static String brokerServiceUrl = "http://127.0.0.1:7000";


    public static String getUrlFromDiscovery(DiscoveryClient discoveryClient, String serviceId) {
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceId);
        if (serviceInstances.isEmpty()) {
            if (processorServiceId.equals(serviceId)) {
                return processorServiceUrl;
            } else {
                return brokerServiceUrl;
            }
        }
        ServiceInstance serviceInstance = serviceInstances.get(new Random().nextInt(serviceInstances.size()));
        return serviceInstance.getUri().toString();
    }

    /**
     * Date to string
     *
     * @param dateDate date
     * @return dateStr
     */
    public static String dateToStr(Date dateDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(dateDate);
    }

    /**
     * remove directory and file
     *
     * @param localFilePath filePath
     * @return remove result
     */
    public static boolean removeLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);

        if (!localFile.exists()) {
            return false;
        }


        if (localFile.isFile()) {
            return localFile.delete();
        } else {
            for (File file : Objects.requireNonNull(localFile.listFiles())) {
                removeLocalFile(file.getAbsolutePath());
            }
        }

        return localFile.delete();
    }
}