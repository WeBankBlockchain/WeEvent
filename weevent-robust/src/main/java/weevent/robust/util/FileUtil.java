package weevent.robust.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;


/**
 *  This is a file reading tool class
 * @author puremilkfan
 * @date 2019/8/5
 */

@Slf4j
public class FileUtil implements AutoCloseable {

    /**
     * Read the file to get the file content
     * @param file
     * @return String
     */
    public static String readTxt(File file) throws IOException {
        if (!file.isFile() || !file.exists()) {
            file.createNewFile();
        }
        InputStream inputStream = new FileInputStream(file);
        String text = IOUtils.toString(inputStream,"utf8");
        return  text;
    }

    /**
     *
     * @param filePath is the file path,
     * @param content  content needs to be written
     * @param flag is true for append, false for overwrite
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
    public void close() throws Exception {
        log.info("resource is close");
    }
}