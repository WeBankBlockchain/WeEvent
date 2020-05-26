package com.webank.weevent.file.ftpclient;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FtpClientService {
    private static final String PATH_SEPARATOR = "/";

    public FTPClient ftpClient = new FTPClient();
    private String defaultDir = "";

    // connect to ftp service
    public void connect(String hostname, int port, String username, String password) throws BrokerException {
        try {
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.connect(hostname, port);

            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                if (ftpClient.login(username, password)) {
                    this.defaultDir = ftpClient.printWorkingDirectory();
                } else {
                    log.error("login to ftp failed, please checkout username and password!");
                    throw new BrokerException(ErrorCode.FTP_INVALID_USERNAME_PASSWD);
                }
            } else {
                log.error("login to ftp failed, unknown error.");
                throw new BrokerException(ErrorCode.FTP_LOGIN_FAILED);
            }
        } catch (IOException e) {
            throw new BrokerException(e.getMessage());
        }
    }


    /**
     * disconnect to ftp server
     *
     * @throws IOException IOException
     */
    public void disconnect() throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
        }
    }

    /**
     * @param fileDir FTP file directory
     * @return list of file and directory in fileDir
     * @throws IOException IOException
     */
    public String[] getFileList(String fileDir) throws IOException {
        ftpClient.enterLocalPassiveMode();

        FTPFile[] ftpFiles = ftpClient.listFiles(fileDir);

        String[] files = null;
        if (ftpFiles != null) {
            files = new String[ftpFiles.length];
            for (int i = 0; i < ftpFiles.length; i++) {
                files[i] = ftpFiles[i].getName();
            }
        }
        this.ftpClient.disconnect();
        return files;
    }


    /**
     * upload a file or directory in ftp default directory
     *
     * @param uploadFile file or directory
     * @return true-success, false-fail
     * @throws BrokerException BrokerException
     */
    public boolean upLoadFile(File uploadFile) throws BrokerException {
        if (!this.ftpClient.isConnected() || !this.ftpClient.isAvailable()) {
            log.error("ftp client not connected to a server");
            throw new BrokerException(ErrorCode.FTP_CLIENT_NOT_CONNECT_TO_SERVER);
        }
        if (uploadFile == null || !uploadFile.exists()) {
            log.error("upload file not exist");
            throw new BrokerException(ErrorCode.FTP_NOT_EXIST_PATH);
        }

        try {
            this.ftpClient.setDataTimeout(300 * 1000);
            this.ftpClient.enterLocalPassiveMode();
            this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            if (!uploadFile.isDirectory()) {
                FileInputStream inputStream = new FileInputStream(uploadFile);
                this.ftpClient.storeFile(uploadFile.getName(), inputStream);
                inputStream.close();
                log.info("file upload success, {}", uploadFile.getName());
            } else {
                // upload directory
                boolean makeDir = this.ftpClient.makeDirectory(uploadFile.getName());
                if (!makeDir) {
                    log.error("make ftp directory failed, {}", uploadFile.getName());
                    throw new BrokerException(ErrorCode.FTP_MAKE_DIR_FAILED);
                }
                // change working directory
                boolean changeDir = this.ftpClient.changeWorkingDirectory(uploadFile.getName());
                if (!changeDir) {
                    log.error("change working directory failed, {}", uploadFile.getName());
                    throw new BrokerException(ErrorCode.FTP_CHANGE_WORKING_DIR_FAILED);
                }

                File[] listFiles = uploadFile.listFiles();
                for (File file : listFiles) {
                    File eachFile = file;
                    // is directory, upload recursively
                    if (eachFile.isDirectory()) {
                        upLoadFile(eachFile);
                         // subdirectory upload finished, change work directory
                        boolean changeToParentDir = this.ftpClient.changeToParentDirectory();
                        if (!changeToParentDir) {
                            log.error("change to parent directory failed");
                            throw new BrokerException(ErrorCode.FTP_CHANGE_WORKING_DIR_FAILED);
                        }
                    } else {
                        FileInputStream fileInputStream = new FileInputStream(eachFile);
                        boolean storeRet = this.ftpClient.storeFile(eachFile.getName(), fileInputStream);
                        if (!storeRet) {
                            log.error("store file to ftp server failed, {}", eachFile.getName());
                            throw new BrokerException(ErrorCode.FTP_UPLOAD_FILE_FAILED);
                        }
                        fileInputStream.close();
                    }
                }
                this.ftpClient.disconnect();
                log.info("directory upload success, {}", uploadFile.getName());
            }
        } catch (IOException e) {
            throw new BrokerException(e.getMessage());
        }
        return true;
    }

    /**
     * upload file or directory to specify directory, if remote path not exist, create it
     *
     * @param remotePath ftp server directory
     * @param uploadFile local file or directory
     * @throws BrokerException
     */
    public void upLoadFile(String remotePath, File uploadFile) throws BrokerException {
        if (!this.ftpClient.isConnected() || !this.ftpClient.isAvailable()) {
            log.error("ftp client not connected to a server");
            throw new BrokerException(ErrorCode.FTP_CLIENT_NOT_CONNECT_TO_SERVER);
        }
        if (uploadFile == null || !uploadFile.exists()) {
            log.error("upload file not exist");
            throw new BrokerException(ErrorCode.FTP_NOT_EXIST_PATH);
        }
        if (StringUtils.isBlank(remotePath)) {
            log.error("remote path invalid, {}", remotePath);
            throw new BrokerException(ErrorCode.FTP_INVALID_REMOTE_PATH);
        }

        try {
            this.ftpClient.setDataTimeout(300 * 1000);
            this.ftpClient.enterLocalPassiveMode();
            this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // create directory
            boolean makeDir = this.ftpClient.makeDirectory(remotePath);
            if (!makeDir) {
                log.error("make ftp directory failed, {}", remotePath);
                throw new BrokerException(ErrorCode.FTP_MAKE_DIR_FAILED);
            }
            // change word directory
            boolean changeDir = this.ftpClient.changeWorkingDirectory(remotePath);
            if (!changeDir) {
                log.error("change working directory failed, {}", remotePath);
                throw new BrokerException(ErrorCode.FTP_CHANGE_WORKING_DIR_FAILED);
            }

            this.upLoadFile(uploadFile);

        } catch (IOException e) {
            throw new BrokerException(e.getMessage());
        }
    }

    /**
     * download a single file
     *
     * @param remoteFilePath file path in ftp server
     * @param localPath local path for download file
     * @throws IOException
     * @throws BrokerException
     */
    public void downLoadFile(String remoteFilePath, String localPath) throws BrokerException {
        if (!this.ftpClient.isConnected() || !this.ftpClient.isAvailable()) {
            log.error("ftp client not connected to a server");
            throw new BrokerException(ErrorCode.FTP_CLIENT_NOT_CONNECT_TO_SERVER);
        }
        if (StringUtils.isBlank(remoteFilePath)) {
            log.error("remote file path invalid, {}", remoteFilePath);
            throw new BrokerException(ErrorCode.FTP_INVALID_REMOTE_PATH);
        }
        if (StringUtils.isBlank(localPath)) {
            log.error("local file path invalid, {}", localPath);
            throw new BrokerException(ErrorCode.FTP_NOT_EXIST_PATH);
        }

        try {
            this.ftpClient.enterLocalPassiveMode();
            this.ftpClient.setDataTimeout(300 * 1000);
            this.ftpClient.enterLocalPassiveMode();
            this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // when upload directory, and change working directory to default failed, relative directory is illegal.
            boolean changeDir = this.ftpClient.changeWorkingDirectory(this.defaultDir);
            if (!changeDir) {
                log.error("change working directory failed, {}", this.defaultDir);
                throw new BrokerException(ErrorCode.FTP_CHANGE_WORKING_DIR_FAILED);
            }

            FTPFile[] ftpFiles = ftpClient.listFiles(remoteFilePath);
            FTPFile ftpFile = null;
            if (ftpFiles.length != 0) {
                ftpFile = ftpFiles[0];
            }

            if (ftpFile != null && ftpFile.isFile()) {
                // create local file path(remoteFilePath maybe a multi-layer directory)
                File localFile = new File(localPath, remoteFilePath);
                if (!localFile.getParentFile().exists()) {
                    boolean retMkdir = localFile.getParentFile().mkdirs();
                    if (!retMkdir) {
                        log.error("make local parent dir failed, {}", localFile.getParentFile().getName());
                        throw new BrokerException(ErrorCode.FTP_MAKE_DIR_FAILED);
                    }
                }

                // change ftpClient work path
                OutputStream outputStream = new FileOutputStream(localFile);
                String workDir = remoteFilePath.substring(0, remoteFilePath.lastIndexOf(PATH_SEPARATOR));
                if (StringUtils.isBlank(workDir)) {
                    workDir = PATH_SEPARATOR;
                }

                changeDir = ftpClient.changeWorkingDirectory(workDir);
                if (!changeDir) {
                    log.error("change working directory failed, {}", workDir);
                    throw new BrokerException(ErrorCode.FTP_CHANGE_WORKING_DIR_FAILED);
                }
                boolean retrieve = ftpClient.retrieveFile(ftpFile.getName(), outputStream);
                if (!retrieve) {
                    log.error("retrieve file from ftp server failed, {}", ftpFile.getName());
                    throw new BrokerException(ErrorCode.FTP_RETRIEVE_FILE_FAILED);
                }

                outputStream.flush();
                outputStream.close();
                log.info("file download success, {}", ftpFile.getName());
            }
        } catch (IOException e) {
            throw new BrokerException(e.getMessage());
        }
    }


    /**
     * download a directory
     *
     * @param remoteDirPath  download directory
     * @param localPath local path
     * @throws BrokerException BrokerException
     */
    public void downLoadDirectory(String remoteDirPath, String localPath) throws BrokerException {
        if (!this.ftpClient.isConnected() || !this.ftpClient.isAvailable()) {
            log.error("ftp client not connected to a server");
            throw new BrokerException(ErrorCode.FTP_CLIENT_NOT_CONNECT_TO_SERVER);
        }
        if (StringUtils.isBlank(remoteDirPath)) {
            log.error("remote dir path invalid, {}", remoteDirPath);
            throw new BrokerException(ErrorCode.FTP_INVALID_REMOTE_PATH);
        }
        if (StringUtils.isBlank(localPath)) {
            log.error("local file path invalid, {}", localPath);
            throw new BrokerException(ErrorCode.FTP_NOT_EXIST_PATH);
        }

        try {
            this.ftpClient.enterLocalPassiveMode();
            this.ftpClient.setDataTimeout(300 * 1000);
            this.ftpClient.enterLocalPassiveMode();
            this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            List<String> filePath = new ArrayList<>();
            filePath = this.getAllFilePath(remoteDirPath, filePath, true);

            for (String path : filePath) {
                downLoadFile(path, localPath);
            }
        } catch (IOException e) {
            throw new BrokerException(e.getMessage());
        }
    }

    /**
     * get all file in the path
     *
     * @param remoteDirPath ftp server file path
     * @param filePathList param
     * @param first first invoke flag
     * @return all file list
     * @throws BrokerException
     */
    private List<String> getAllFilePath(String remoteDirPath, List<String> filePathList, boolean first) throws BrokerException {
        try {
            if (first) {
                boolean changeDir = ftpClient.changeWorkingDirectory(remoteDirPath);
                if (!changeDir) {
                    log.error("change working directory failed, {}", remoteDirPath);
                    throw new BrokerException(ErrorCode.FTP_CHANGE_WORKING_DIR_FAILED);
                }
            } else {
                String nextPath = remoteDirPath.substring(remoteDirPath.lastIndexOf('/')+1);
                boolean changeDir = ftpClient.changeWorkingDirectory(nextPath);
                if (!changeDir) {
                    log.error("change working directory failed, {}", nextPath);
                    throw new BrokerException(ErrorCode.FTP_CHANGE_WORKING_DIR_FAILED);
                }
            }

            FTPFile[] ftpFiles = ftpClient.listFiles();
            if (ftpFiles != null && ftpFiles.length > 0) {
                for (FTPFile file : ftpFiles) {
                    String path = remoteDirPath + "/" + file.getName();
                    if (file.isFile()) {
                        filePathList.add(path);
                    } else {
                        getAllFilePath(path, filePathList, false);
                    }
                }
            }
        } catch (IOException e) {
            throw new BrokerException(e.getMessage());
        }
        return filePathList;
    }
}
