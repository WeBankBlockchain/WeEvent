package com.webank.weevent.governance.controller;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.common.GovernanceResult;
import com.webank.weevent.governance.entity.FileTransportEntity;
import com.webank.weevent.governance.service.FileService;
import com.webank.weevent.governance.utils.ParamCheckUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * file upload/download Controller.
 *
 * @author v_wbhwliu
 * @version 1.3
 * @since 2020/5/20
 */

@Slf4j
@CrossOrigin
@RequestMapping(value = "/file")
@Controller
public class FileController {

    private FileService fileService;

    @Autowired
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(path = "/openTransport")
    @ResponseBody
    public GovernanceResult openTransport(@RequestBody FileTransportEntity fileTransport) throws GovernanceException {
        log.info("openTransport, fileTransport:{}.", fileTransport.toString());
        return this.fileService.openTransport(fileTransport);
    }

    @PostMapping(path = "/upload")
    @ResponseBody
    public GovernanceResult uploadChunk(HttpServletRequest request) throws GovernanceException {
        return this.fileService.uploadFile(request);
    }

    @GetMapping(path = "/upload")
    @ResponseBody
    public GovernanceResult prepareUploadFile(@RequestParam(name = "groupId") String groupId,
                                              @RequestParam(name = "identifier") String fileId,
                                              @RequestParam(name = "topicName") String topicName,
                                              @RequestParam(name = "totalChunks") Integer totalChunks,
                                              @RequestParam(name = "totalSize") long totalSize,
                                              @RequestParam(name = "chunkNumber") Integer chunkNumber,
                                              @RequestParam(name = "chunkSize") Integer chunkSize,
                                              @RequestParam(name = "filename") String filename) throws GovernanceException {
        log.info("prepareUploadFile, groupId:{}, fileId:{}, filename:{}, topic:{}, totalSize:{}, totalChunks:{}, chunkNumber:{}",
                groupId, fileId, filename, topicName, totalSize, totalChunks, chunkNumber);

        return this.fileService.prepareUploadFile(fileId, filename, topicName, groupId, totalSize, chunkSize);
    }

    @RequestMapping(path = "/download")
    public void download(@RequestParam(name = "groupId") String groupId,
                         @RequestParam(name = "brokerId") Integer brokerId,
                         @RequestParam(name = "fileId") String fileId,
                         HttpServletResponse response) throws GovernanceException {
        log.info("download file, groupId:{}, brokerId:{}, fileId:{}.", groupId, brokerId, fileId);
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");

        ParamCheckUtils.validateFileId(fileId);
        String downloadFile = fileService.downloadFile(groupId, brokerId, fileId);
        if (StringUtils.isBlank(downloadFile)) {
            throw new GovernanceException("download file not exist");
        }
        String fileName = downloadFile.substring(downloadFile.lastIndexOf("/") + 1);
        response.setHeader("fileName", fileName);

        byte[] buffer = new byte[1024];

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(downloadFile));
             OutputStream os = response.getOutputStream()) {
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                os.flush();
                i = bis.read(buffer);
            }
            log.info("download file success, fileId:{}", fileId);
        } catch (IOException e) {
            log.error("download file error, groupId:{} fileId:{}", groupId, fileId, e);
            throw new GovernanceException("download file error", e);
        }
    }

    @RequestMapping(path = "/listFile")
    @ResponseBody
    public GovernanceResult listFile(@RequestParam(name = "groupId") String groupId,
                                     @RequestParam(name = "brokerId") Integer brokerId,
                                     @RequestParam(name = "topicName") String topicName) throws GovernanceException {
        log.info("listFile, groupId:{}, topic:{}.", groupId, topicName);
        return fileService.listFile(groupId, brokerId, topicName);
    }

    @RequestMapping(path = "/status")
    @ResponseBody
    public GovernanceResult status(@RequestParam(name = "groupId") String groupId,
                                   @RequestParam(name = "brokerId") Integer brokerId,
                                   @RequestParam(name = "topicName") String topicName,
                                   @RequestParam(name = "role") String role) throws GovernanceException {
        log.info("status, groupId:{}, topic:{}, role:{}.", groupId, topicName, role);
        return fileService.status(groupId, brokerId, topicName, role);
    }

    @RequestMapping(path = "/listTransport")
    @ResponseBody
    public GovernanceResult listTransport(@RequestParam(name = "groupId") String groupId,
                                          @RequestParam(name = "brokerId") Integer brokerId) {
        log.info("listTransport, groupId:{}, brokerId:{}.", groupId, brokerId);
        return fileService.listTransport(groupId, brokerId);
    }

    @PostMapping(path = "/closeTransport")
    @ResponseBody
    public GovernanceResult closeTransport(@RequestBody FileTransportEntity fileTransport) throws GovernanceException {
        log.info("closeTransport, groupId:{}, brokerId:{}, transportId:{}, roleId:{}, topic:{}.", fileTransport.getGroupId(),
                fileTransport.getBrokerId(), fileTransport.getId(), fileTransport.getRole(), fileTransport.getTopicName());
        return fileService.closeTransport(fileTransport);
    }

}