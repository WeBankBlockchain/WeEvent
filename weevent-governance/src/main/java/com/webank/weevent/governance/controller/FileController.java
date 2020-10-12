package com.webank.weevent.governance.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.file.service.FileChunksMeta;
import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.common.GovernanceResponse;
import com.webank.weevent.governance.entity.FileChunksMetaEntity;
import com.webank.weevent.governance.entity.FileTransportChannelEntity;
import com.webank.weevent.governance.entity.FileTransportStatusEntity;
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
    public GovernanceResponse<Boolean> openTransport(@RequestBody FileTransportChannelEntity fileTransport) throws GovernanceException {
        log.info("openTransport, fileTransport:{}.", fileTransport.toString());
        return this.fileService.openTransport(fileTransport);
    }

    @PostMapping(path = "/upload")
    @ResponseBody
    public GovernanceResponse<Boolean> uploadChunk(HttpServletRequest request) throws GovernanceException {
        return this.fileService.uploadFile(request);
    }

    @GetMapping(path = "/upload")
    @ResponseBody
    public GovernanceResponse<List<Integer>> prepareUploadFile(@RequestParam(name = "groupId") String groupId,
                                              @RequestParam(name = "identifier") String fileId,
                                              @RequestParam(name = "topicName") String topicName,
                                              @RequestParam(name = "totalChunks") Integer totalChunks,
                                              @RequestParam(name = "totalSize") long totalSize,
                                              @RequestParam(name = "chunkSize") Integer chunkSize,
                                              @RequestParam(name = "filename") String filename) throws GovernanceException {
        log.info("prepareUploadFile, groupId:{}, fileId:{}, filename:{}, topic:{}, totalSize:{}, totalChunks:{}",
                groupId, fileId, filename, topicName, totalSize, totalChunks);
        return this.fileService.prepareUploadFile(fileId, filename, topicName, groupId, totalSize, chunkSize);
    }

    @RequestMapping(path = "/download")
    public void download(@RequestParam(name = "topic") String topic,
                         @RequestParam(name = "fileName") String fileName,
                         HttpServletResponse response) throws GovernanceException {
        log.info("download file, topic:{}, fileName:{}.", topic, fileName);
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream; charset=UTF-8");

        ParamCheckUtils.validateFileName(fileName);
        String downloadFile = this.fileService.downloadFile(topic, fileName);
        if (StringUtils.isBlank(downloadFile)) {
            throw new GovernanceException("download file not exist");
        }

        try {
            response.setHeader("filename", URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e) {
            log.error("encode fileName error, fileName:{}", fileName, e);
        }

        byte[] buffer = new byte[1024];

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(downloadFile));
             OutputStream os = response.getOutputStream()) {
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                os.flush();
                i = bis.read(buffer);
            }
            log.info("download file success, topic:{}, fileName:{}", topic, fileName);
        } catch (IOException e) {
            log.error("download file error, topic:{} fileName:{}", topic, fileName, e);
            throw new GovernanceException("download file error", e);
        }
    }

    @RequestMapping(path = "/listFile")
    @ResponseBody
    public GovernanceResponse<List<FileChunksMeta>> listFile(@RequestParam(name = "groupId") String groupId,
                                     @RequestParam(name = "brokerId") Integer brokerId,
                                     @RequestParam(name = "topicName") String topicName) throws GovernanceException {
        log.info("listFile, groupId:{}, topic:{}.", groupId, topicName);
        return this.fileService.listFile(groupId, brokerId, topicName);
    }

    @RequestMapping(path = "/downLoadStatus")
    @ResponseBody
    public GovernanceResponse<List<FileChunksMetaEntity>> downLoadStatus(@RequestParam(name = "groupId") String groupId,
                                           @RequestParam(name = "brokerId") Integer brokerId,
                                           @RequestParam(name = "topicName") String topicName) throws GovernanceException {
        log.info("status, groupId:{}, topic:{}.", groupId, topicName);
        return this.fileService.downLoadStatus(groupId, brokerId, topicName);
    }

    @RequestMapping(path = "/uploadStatus")
    @ResponseBody
    public GovernanceResponse<List<FileTransportStatusEntity>> uploadStatus(@RequestParam(name = "groupId") String groupId,
                                         @RequestParam(name = "brokerId") Integer brokerId,
                                         @RequestParam(name = "topicName") String topicName) throws GovernanceException {
        log.info("status, groupId:{}, topic:{}.", groupId, topicName);
        return this.fileService.uploadStatus(groupId, brokerId, topicName);
    }

    @RequestMapping(path = "/listTransport")
    @ResponseBody
    public GovernanceResponse<List<FileTransportChannelEntity>> listTransport(@RequestParam(name = "groupId") String groupId,
                                          @RequestParam(name = "brokerId") Integer brokerId) {
        log.info("listTransport, groupId:{}, brokerId:{}.", groupId, brokerId);
        return this.fileService.listTransport(groupId, brokerId);
    }

    @PostMapping(path = "/closeTransport")
    @ResponseBody
    public GovernanceResponse<Boolean> closeTransport(@RequestBody FileTransportChannelEntity fileTransport) throws GovernanceException {
        log.info("closeTransport, groupId:{}, brokerId:{}, transportId:{}, roleId:{}, topic:{}.", fileTransport.getGroupId(),
                fileTransport.getBrokerId(), fileTransport.getId(), fileTransport.getRole(), fileTransport.getTopicName());
        return fileService.closeTransport(fileTransport);
    }

    @RequestMapping(path = "/genPemFile")
    public void genPemFile(@RequestParam(name = "groupId") String groupId,
                           @RequestParam(name = "brokerId") Integer brokerId,
                           @RequestParam(name = "filePath") String filePath,
                           HttpServletResponse response) throws GovernanceException {
    	log.info("download file, groupId:{}, brokerId:{}", groupId, brokerId);
		response.setHeader("content-type", "application/octet-stream");
		response.setContentType("application/octet-stream; charset=UTF-8");
		Map<String, String> map = this.fileService.genPemFile(groupId, brokerId, filePath);
		
		String fileName = "公私钥文件.txt";
		String downloadFile = filePath+ "/" + fileName;
		FileWriter fwriter = null;
		try {
			// true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
			fwriter = new FileWriter(downloadFile, true);
			for(Map.Entry<String, String> entry : map.entrySet()){
			    String fileContent = entry.getValue();
			    fwriter.write(fileContent);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				fwriter.flush();
				fwriter.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		try {
			response.setHeader("filename", URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()));
		} catch (UnsupportedEncodingException e) {
			log.error("encode fileName error, fileName:{}", fileName, e);
		}

		byte[] buffer = new byte[1024];
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(downloadFile));
				OutputStream os = response.getOutputStream()) {
			int i = bis.read(buffer);
			while (i != -1) {
				os.write(buffer, 0, i);
				os.flush();
				i = bis.read(buffer);
			}
			log.info("download file success, brokerId:{}, fileName:{}", brokerId, fileName);
		} catch (IOException e) {
			log.error("download file error, brokerId:{} fileName:{}", brokerId, fileName, e);
			throw new GovernanceException("download file error", e);
		}
		new File(downloadFile).delete();
    }

    @RequestMapping(path = "/checkUploaded")
    @ResponseBody
    public GovernanceResponse<Object> checkFileIsUploaded(@RequestParam(name = "groupId") String groupId,
                                                @RequestParam(name = "brokerId") Integer brokerId,
                                                @RequestParam(name = "topicName") String topicName,
                                                @RequestParam(name = "fileName") String fileName) throws GovernanceException {
        log.info("checkFileIsUploaded, groupId:{}, topic:{}, fileName:{}.", groupId, topicName, fileName);
        return this.fileService.checkFileIsUploaded(groupId, brokerId, topicName, fileName);
    }

}