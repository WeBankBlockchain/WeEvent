package weevent.robust.util;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

/**
 *  This is a string  tool class
 * @author puremilkfan
 * @version  since 1.1
 * @date 2019/8/5
 */
public class StringUtil {

    public  final static  String HTTP_HEADER = "http://";

    /**
     *  Pass in the string in the correct order and return the full url you need
     * @param params
     * @return StringBuffer
     */
    public static StringBuffer getIntegralUrl(String...params){
        StringBuffer headBuffer = new StringBuffer();
        if(params == null || params.length == 0){
            return  headBuffer;
        }
        for(String param:params){
            headBuffer.append(param);
        }
        return  headBuffer;
    }


    /**
     *  get formatted time string
     * @param format
     * @param date
     * @return
     */
    public static String getFormatTime(String format, Date date){
        String formatTime = DateFormatUtils.format(date, format);
        return  formatTime;
    }
}
