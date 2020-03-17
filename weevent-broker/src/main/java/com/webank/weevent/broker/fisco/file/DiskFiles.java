package com.webank.weevent.broker.fisco.file;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.FileChunksMeta;
import com.webank.weevent.client.JsonHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * Files stored in local disk.
 *
 * @author matthewliu
 * @since 2020/02/18
 */
@Slf4j
public class DiskFiles {
    public final String MetaFileSuffix = ".json";
    private final String path;

    public DiskFiles(String path) {
        File localPath = new File(path);
        if (!localPath.exists()) {
            boolean result = localPath.mkdirs();
            if (result) {
                log.info("create local file path, {}", path);
            }
        }

        log.info("local file path: {}, {} -> {}", path, localPath.getFreeSpace(), localPath.getTotalSpace());
        this.path = path;
    }

    private String genLocalFileName(String fileId) {
        return this.path + "/" + fileId;
    }

    private String genLocalMetaFileName(String fileId) {
        return this.genLocalFileName(fileId) + MetaFileSuffix;
    }

    // save FileChunksMeta in UTF-8 format, fileName may be contain chinese character
    public FileChunksMeta loadFileMeta(String fileId) throws BrokerException {
        String localMetaFile = this.genLocalMetaFileName(fileId);
        File fileMeta = new File(localMetaFile);
        if (!fileMeta.exists()) {
            log.error("not exist local meta file, {}", localMetaFile);
            throw new BrokerException(ErrorCode.FILE_NOT_EXIST);
        }
        return this.loadFileMeta(fileMeta);
    }

    public FileChunksMeta loadFileMeta(File fileMeta) throws BrokerException {
        if (fileMeta.length() > 1024 * 1024) {
            log.error("local meta file is exceed 1M, skip it");
            throw new BrokerException(ErrorCode.FILE_READ_EXCEPTION);
        }

        try (FileInputStream fileInputStream = new FileInputStream(fileMeta);
             InputStreamReader reader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8)) {
            char[] data = new char[(int) fileMeta.length()];
            int readSize = reader.read(data);
            // the read size is length of UTF-8 char, less then fileMeta.length()
            if (readSize <= 0) {
                log.error("read local meta file failed");
                throw new BrokerException(ErrorCode.FILE_READ_EXCEPTION);
            }
            return JsonHelper.json2Object(String.valueOf(data), FileChunksMeta.class);
        } catch (IOException | BrokerException e) {
            log.error("read local meta file exception", e);
            throw new BrokerException(ErrorCode.FILE_READ_EXCEPTION);
        }
    }

    public void saveFileMeta(FileChunksMeta fileChunksMeta) throws BrokerException {
        log.info("save FileChunksMeta, filled chunk: {} -> chunkNum: {}", fileChunksMeta.getChunkStatus().cardinality(), fileChunksMeta.getChunkNum());

        String localMetaFile = this.genLocalMetaFileName(fileChunksMeta.getFileId());
        try (FileOutputStream fileOutputStream = new FileOutputStream(localMetaFile);
             OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
            String data = JsonHelper.object2Json(fileChunksMeta);
            writer.write(data);
        } catch (IOException | BrokerException e) {
            log.error("write local meta file exception", e);
            throw new BrokerException(ErrorCode.FILE_WRITE_EXCEPTION);
        }
    }

    public void createFixedLengthFile(String fileId, long size) throws BrokerException {
        // ensure path exist and disk space
        File path = new File(this.path);
        if (!path.exists()) {
            log.error("not exist local file path, {}", this.path);
            throw new BrokerException(ErrorCode.FILE_NOT_EXIST_PATH);
        }
        if (path.getFreeSpace() < size + 1024 * 1024) {
            log.error("not enough disk space, {} -> free {}", size, path.getFreeSpace());
            throw new BrokerException(ErrorCode.FILE_NOT_ENOUGH_SPACE);
        }

        String localFile = this.genLocalFileName(fileId);
        log.info("create local file for receiving file, {} size: {}", localFile, size);
        try (RandomAccessFile file = new RandomAccessFile(localFile, "rw")) {
            file.setLength(size);
        } catch (IOException e) {
            log.error("create fixed length file failed", e);
            throw new BrokerException(ErrorCode.FILE_WRITE_EXCEPTION);
        }
    }

    public void writeChunkData(String fileId, int chunkIndex, byte[] chunkData) throws BrokerException {
        // believe FileChunksMeta in local file
        FileChunksMeta fileChunksMeta = this.loadFileMeta(fileId);
        if (chunkIndex >= fileChunksMeta.getChunkNum()) {
            log.error("invalid chunk meta data, skip");
            throw new BrokerException(ErrorCode.FILE_INVALID_CHUNK);
        }
        if (chunkData.length > fileChunksMeta.getChunkSize()) {
            log.error("invalid chunk meta data, skip");
            throw new BrokerException(ErrorCode.FILE_INVALID_CHUNK);
        }

        String localFile = this.genLocalFileName(fileId);
        log.info("write data in local file, {}@{} size: {}", localFile, chunkIndex, chunkData.length);
        try (RandomAccessFile f = new RandomAccessFile(localFile, "rw")) {
            f.seek((long) chunkIndex * fileChunksMeta.getChunkSize());
            f.write(chunkData);
        } catch (FileNotFoundException e) {
            log.error("not exist local file", e);
            throw new BrokerException(ErrorCode.FILE_NOT_EXIST);
        } catch (IOException e) {
            log.error("write local file failed", e);
            throw new BrokerException(ErrorCode.FILE_WRITE_EXCEPTION);
        }

        String localMetaFile = this.genLocalMetaFileName(fileId);
        log.info("update FileChunksMeta in local file, {}@{}", localMetaFile, chunkIndex);
        fileChunksMeta.getChunkStatus().set(chunkIndex);
        saveFileMeta(fileChunksMeta);
    }

    public byte[] readChunkData(String fileId, int chunkIndex) throws BrokerException {
        // load FileChunksMeta from local file, FileChunksMeta in memory is closed within amop chanel
        FileChunksMeta fileChunksMeta = this.loadFileMeta(fileId);
        if (chunkIndex >= fileChunksMeta.getChunkNum()) {
            log.error("invalid chunk meta data, skip");
            throw new BrokerException(ErrorCode.FILE_INVALID_CHUNK);
        }
        if (!fileId.equals(fileChunksMeta.getFileId())) {
            log.error("invalid chunk meta data, skip");
            throw new BrokerException(ErrorCode.FILE_INVALID_CHUNK);
        }

        // read data from local file
        String localFile = this.genLocalFileName(fileId);
        try (RandomAccessFile f = new RandomAccessFile(localFile, "r")) {
            int size = fileChunksMeta.getChunkSize();
            if (chunkIndex == fileChunksMeta.getChunkNum() - 1) {
                size = (int) (fileChunksMeta.getFileSize() % fileChunksMeta.getChunkSize());
            }
            byte[] data = new byte[size];
            f.seek((long) chunkIndex * fileChunksMeta.getChunkSize());
            int readSize = f.read(data);
            if (size != readSize) {
                log.error("read data from local file failed");
                throw new BrokerException(ErrorCode.FILE_READ_EXCEPTION);
            }
            return data;
        } catch (FileNotFoundException e) {
            log.error("not exist local file", e);
            throw new BrokerException(ErrorCode.FILE_NOT_EXIST);
        } catch (IOException e) {
            log.error("read data from local file exception", e);
            throw new BrokerException(ErrorCode.FILE_READ_EXCEPTION);
        }
    }

    private void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            boolean result = file.delete();
            log.info("delete file, result: {} {}", result, fileName);
        }
    }

    public void cleanUp(String fileId) {
        this.deleteFile(this.genLocalFileName(fileId));
        this.deleteFile(this.genLocalMetaFileName(fileId));
    }

    public List<FileChunksMeta> listNotCompleteFiles(boolean all) {
        List<FileChunksMeta> fileChunksMetas = new ArrayList<>();

        File topPath = new File(this.path);
        if (!topPath.exists()) {
            log.error("not exist path: {}", path);
            return fileChunksMetas;
        }

        File[] files = topPath.listFiles((dir, name) -> name.endsWith(MetaFileSuffix));
        if (files != null) {
            for (File file : files) {
                try {
                    FileChunksMeta fileChunksMeta = this.loadFileMeta(file);
                    if (all || !fileChunksMeta.checkChunkFull()) {
                        fileChunksMetas.add(fileChunksMeta);
                    }
                } catch (BrokerException e) {
                    log.error("load file meta failed, skip");
                }
            }
        }

        return fileChunksMetas;
    }
}
