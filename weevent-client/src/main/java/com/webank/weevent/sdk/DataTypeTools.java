package com.webank.weevent.sdk;

/**
 * data type tools for client
 *
 * @author v_wbhwliu
 * @since 2019/09/17
 */
public class DataTypeTools {

    public static String topicArrayToString(String[] topics) throws BrokerException {
        // check params
        if (topics == null || topics.length == 0) {
            throw new BrokerException(ErrorCode.TOPIC_LIST_IS_NULL);
        }

        StringBuffer topicStringBuffer = new StringBuffer();
        for (int i = 0; i < topics.length ; i++){
            if (i < topics.length -1){
                topicStringBuffer.append(topics[i].trim()).append(WeEvent.MULTIPLE_TOPIC_SEPARATOR);
            } else {
                topicStringBuffer.append(topics[i].trim());
            }
        }

        return topicStringBuffer.toString();
    }
}
