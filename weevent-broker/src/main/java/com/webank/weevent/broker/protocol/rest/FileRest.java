package com.webank.weevent.broker.protocol.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.broker.fisco.file.FileTransportService;
import com.webank.weevent.broker.fisco.file.dto.FileChunksMetaPlus;
import com.webank.weevent.broker.fisco.file.dto.FileTransportStats;
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

    @Autowired
    public void setFileTransportService(FileTransportService fileTransportService) {
        this.fileTransportService = fileTransportService;
    }

    @RequestMapping(path = "/openChunk")
    @ResponseBody
    public BaseResponse<FileChunksMeta> openChunk(@RequestParam(name = "topic") String topic,
                                                  @RequestParam(name = "groupId") String groupId,
                                                  @RequestParam(name = "fileName") String fileName,
                                                  @RequestParam(name = "fileSize") long fileSize,
                                                  @RequestParam(name = "md5") String md5) throws BrokerException {
        log.info("groupId:{} md5:{}", groupId, md5);

        ParamCheckUtils.validateFileName(fileName);
        ParamCheckUtils.validateFileSize(fileSize);
        ParamCheckUtils.validateFileMd5(md5);

        // create FileChunksMeta
        FileChunksMeta fileChunksMeta;
        try {
            fileChunksMeta = new FileChunksMeta(WeEventUtils.generateUuid(),
                    URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString()),
                    fileSize,
                    md5,
                    topic,
                    groupId);
        } catch (UnsupportedEncodingException e) {
            log.error("decode fileName error", e);
            throw new BrokerException(ErrorCode.DECODE_FILE_NAME_ERROR);
        }

        // create AMOP channel with FileTransportSender
        FileChunksMeta remoteFileChunksMeta = this.fileTransportService.openChannel(fileChunksMeta);
        return BaseResponse.buildSuccess(remoteFileChunksMeta);
    }

    @RequestMapping(path = "/uploadChunk")
    @ResponseBody
    public BaseResponse<?> uploadChunk(@RequestParam(name = "topic") String topic,
                                       @RequestParam(name = "groupId") String groupId,
                                       @RequestParam(name = "fileId") String fileId,
                                       @RequestParam(name = "chunkIdx") int chunkIdx,
                                       @RequestParam(name = "chunkData") MultipartFile chunkFile) throws BrokerException, IOException {
        log.info("fileId: {}  chunkIdx: {} chunkData: {}", fileId, chunkIdx, chunkFile.getSize());

        byte[] chunkData = chunkFile.getBytes();

        ParamCheckUtils.validateFileId(fileId);
        ParamCheckUtils.validateChunkIdx(chunkIdx);
        ParamCheckUtils.validateChunkData(chunkData);

        // send data to FileTransportSender
        this.fileTransportService.sendChunkData(topic, groupId, fileId, chunkIdx, chunkData);

        return BaseResponse.buildSuccess();
    }

    @RequestMapping(path = "/listChunk")
    @ResponseBody
    public BaseResponse<FileChunksMeta> listChunk(@RequestParam(name = "topic") String topic,
                                                  @RequestParam(name = "groupId") String groupId,
                                                  @RequestParam(name = "fileId") String fileId) throws BrokerException {
        log.info("fileId: {}", fileId);

        ParamCheckUtils.validateFileId(fileId);

        // get file chunks info from Zookeeper
        FileChunksMeta fileChunksMeta = this.fileTransportService.getReceiverFileChunksMeta(topic, groupId, fileId);

        return BaseResponse.buildSuccess(fileChunksMeta);
    }

    @RequestMapping(path = "/closeChunk")
    @ResponseBody
    public BaseResponse<SendResult> closeChunk(@RequestParam(name = "topic") String topic,
                                               @RequestParam(name = "groupId") String groupId,
                                               @RequestParam(name = "fileId") String fileId) throws BrokerException {
        log.info("fileId: {}", fileId);

        ParamCheckUtils.validateFileId(fileId);

        // close channel and send WeEvent
        SendResult sendResult = this.fileTransportService.closeChannel(topic, groupId, fileId);
        return BaseResponse.buildSuccess(sendResult);
    }

    @RequestMapping(path = "/verify")
    @ResponseBody
    public BaseResponse<FileChunksMetaPlus> verify(@RequestParam(name = "eventId") String eventId,
                                                   @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        return BaseResponse.buildSuccess(this.fileTransportService.verify(eventId, groupId));
    }

    @RequestMapping(path = "/stats")
    @ResponseBody
    public BaseResponse<FileTransportStats> stats(@RequestParam(name = "all", required = false) boolean all) {
        return BaseResponse.buildSuccess(this.fileTransportService.stats(all));
    }

    @RequestMapping(path = "/downloadChunk")
    public void downloadChunk(@RequestParam(name = "fileId") String fileId,
                              @RequestParam(name = "chunkIdx") int chunkIdx,
                              HttpServletResponse response) throws BrokerException {
        log.info("fileId: {} chunkIdx: {}", fileId, chunkIdx);

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
}
