package weevent.robust.util;

/**
 *  This is a string  tool class
 * @author puremilkfan
 *
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

}
