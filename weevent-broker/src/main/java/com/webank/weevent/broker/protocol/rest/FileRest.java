package com.webank.weevent.broker.protocol.rest;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.broker.fisco.file.FileTransportService;
import com.webank.weevent.client.BaseResponse;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.FileChunksMeta;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.core.fisco.util.ParamCheckUtils;
import com.webank.weevent.core.fisco.util.WeEventUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author v_wbhwliu
 * @version 1.2
 * @since 2020/2/12
 */
@Slf4j
@RequestMapping(value = "/file")
@Controller
public class FileRest {
    private FileTransportService fileTransportService;

    @Autowired(required = false)
    public void setFileTransportService(FileTransportService fileTransportService) {
        this.fileTransportService = fileTransportService;
    }

    private void checkSupport() throws BrokerException {
        if (this.fileTransportService == null) {
            log.error("DO NOT SUPPORT file transport without zookeeper");
            throw new BrokerException(ErrorCode.ZOOKEEPER_NOT_SUPPORT_FILE_SUBSCRIPTION);
        }
    }

    @RequestMapping(path = "/openChunk")
    @ResponseBody
    public BaseResponse<FileChunksMeta> openChunk(@RequestParam(name = "topic") String topic,
                                                  @RequestParam(name = "groupId", required = false) String groupId,
                                                  @RequestParam(name = "fileName") String fileName,
                                                  @RequestParam(name = "fileSize") long fileSize,
                                                  @RequestParam(name = "md5") String md5) throws BrokerException {
        log.info("groupId:{} md5:{}", groupId, md5);

        checkSupport();
        ParamCheckUtils.validateFileName(fileName);
        ParamCheckUtils.validateFileSize(fileSize);
        ParamCheckUtils.validateFileMd5(md5);

        // create FileChunksMeta
        FileChunksMeta fileChunksMeta = new FileChunksMeta(WeEventUtils.generateUuid(),
                fileName,
                fileSize,
                md5,
                topic,
                groupId);

        // create AMOP channel with FileTransportSender
        FileChunksMeta remoteFileChunksMeta = this.fileTransportService.openChannel(fileChunksMeta);
        return BaseResponse.buildSuccess(remoteFileChunksMeta);
    }

    @RequestMapping(path = "/uploadChunk")
    @ResponseBody
    public BaseResponse<?> uploadChunk(@RequestParam(name = "fileId") String fileId,
                                       @RequestParam(name = "chunkIdx") int chunkIdx,
                                       @RequestParam(name = "chunkData") MultipartFile chunkFile) throws BrokerException, IOException {
        log.info("fileId: {}  chunkIdx: {} chunkData: {}", fileId, chunkIdx, chunkFile.getSize());
        checkSupport();
        byte[] chunkData = chunkFile.getBytes();

        ParamCheckUtils.validateFileId(fileId);
        ParamCheckUtils.validateChunkIdx(chunkIdx);
        ParamCheckUtils.validateChunkData(chunkData);

        // send data to FileTransportSender
        this.fileTransportService.sendChunkData(fileId, chunkIdx, chunkData);

        return BaseResponse.buildSuccess();
    }

    @RequestMapping(path = "/downloadChunk")
    public void downloadChunk(@RequestParam(name = "fileId") String fileId,
                              @RequestParam(name = "chunkIdx") int chunkIdx,
                              HttpServletResponse response) throws BrokerException {
        log.info("fileId: {} chunkIdx: {}", fileId, chunkIdx);

        checkSupport();
        ParamCheckUtils.validateFileId(fileId);
        ParamCheckUtils.validateChunkIdx(chunkIdx);

        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");

        byte[] downloadChunkBytes = this.fileTransportService.downloadChunk(fileId, chunkIdx);
        if (downloadChunkBytes.length == 0) {
            throw new BrokerException(ErrorCode.FILE_DOWNLOAD_ERROR);
        }

        try (OutputStream os = response.getOutputStream()) {
            os.write(downloadChunkBytes);
            os.flush();
        } catch (IOException e) {
            log.error("write bytes to client error, fileId:{} chunkIdx:{}", fileId, chunkIdx, e);
            throw new BrokerException(ErrorCode.FILE_DOWNLOAD_ERROR);
        }
    }

    @RequestMapping(path = "/listChunk")
    @ResponseBody
    public BaseResponse<FileChunksMeta> listChunk(@RequestParam(name = "fileId") String fileId) throws BrokerException {
        log.info("fileId: {}", fileId);

        checkSupport();
        ParamCheckUtils.validateFileId(fileId);

        // get file chunks info from Zookeeper
        FileChunksMeta fileChunksMeta = this.fileTransportService.getReceiverFileChunksMeta(fileId);

        return BaseResponse.buildSuccess(fileChunksMeta);
    }

    @RequestMapping(path = "/closeChunk")
    @ResponseBody
    public BaseResponse<SendResult> closeChunk(@RequestParam(name = "fileId") String fileId) throws BrokerException {
        log.info("fileId: {}", fileId);
        checkSupport();
        ParamCheckUtils.validateFileId(fileId);

        // close channel and send WeEvent
        SendResult sendResult = this.fileTransportService.closeChannel(fileId);
        return BaseResponse.buildSuccess(sendResult);
    }

    @ExceptionHandler(value = BrokerException.class)
    public Object baseErrorHandler(HttpServletRequest req, BrokerException e) {
        log.error("rest api, remote: {} uri: {}", req.getRemoteHost(), req.getRequestURL());
        log.error("detect BrokerException", e);

        return BaseResponse.buildException(e);
    }

    @ExceptionHandler(value = Exception.class)
    public Object baseErrorHandler(HttpServletRequest req, Exception e) {
        log.error("rest api, remote: {} uri: {}", req.getRemoteHost(), req.getRequestURL());
        log.error("detect Exception", e);

        return BaseResponse.buildException(e);
    }
}
