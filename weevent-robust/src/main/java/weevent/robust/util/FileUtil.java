package weevent.robust.util;

import com.webank.weevent.sdk.BrokerException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;


/**
 *  This is a file reading tool class
 * @author puremilkfan
 * @version  since 1.1
 * @date 2019/8/5
 */

@Slf4j
public class FileUtil implements AutoCloseable {

    /**
     *  Read the file to get the file content
     * @param file
     * @return
     */
    public static String readTxt(File file) throws BrokerException,IOException {
        StringBuffer content = new StringBuffer();
        if(!file.isFile() || !file.exists()){
            file.createNewFile();
        }
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
             BufferedReader br = new BufferedReader(isr))
         {

            String lineTxt;
            while ((lineTxt = br.readLine()) != null) {
                content.append(lineTxt);
            }
            br.close();
            return content.toString();
        } catch (Exception e) {
            log.error("File read error!");
            throw  new BrokerException("file does not exist!");
        }
    }

    /**
     * filePath is the file path,
     * content needs to be written,
     * flag is true for append, false for overwrite
     *
     * @param filePath
     * @param content
     * @param flag
     */
    public static void writeStringToFile(String filePath, String content, boolean flag) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (OutputStream os = new FileOutputStream(file, flag)) {
            byte[] b = content.getBytes();
            os.write(b);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception{
        log.info("resource is close");
    }
}