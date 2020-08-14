package com.webank.weevent.governance.entity;

import lombok.Data;

/**
 * File chunk information. support file larger then 2G, be carefully fileSize
 *
 * @author matthewliu
 * @since 2020/02/12
 */
@Data
public class FileChunksMetaEntity {

	// file name in biz
	private String fileName;
	
	// file data's md5
	private String fileMd5;
	
	// file size in byte
	private long fileSize;
	
	// status
	private String status;
	
	// speed
	private String speed;
	
	// process
	private String process;

	@Override
	public String toString() {
		return "FileChunksMetaEntity [fileName=" + fileName + ", fileMd5=" + fileMd5 + ", fileSize=" + fileSize
				+ ", status=" + status + ", speed=" + speed + ", process=" + process + "]";
	}
	
	

}
