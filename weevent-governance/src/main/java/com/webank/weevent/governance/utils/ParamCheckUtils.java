package com.webank.weevent.governance.utils;


import com.webank.weevent.governance.common.ConstantProperties;
import com.webank.weevent.governance.common.ErrorCode;
import com.webank.weevent.governance.common.GovernanceException;

import org.apache.commons.lang3.StringUtils;

/**
 * param check utils.
 *
 * @author v_wbhwliu
 * @version 1.3
 * @since 2020/5/25
 */
public class ParamCheckUtils {
    public static void validateFileName(String fileName) throws GovernanceException {
        if (StringUtils.isBlank(fileName)) {
            throw new GovernanceException(ErrorCode.FILE_NAME_IS_NULL);
        }
    }

    public static void validateFileId(String fileId) throws GovernanceException {
        if (StringUtils.isBlank(fileId)) {
            throw new GovernanceException(ErrorCode.FILE_ID_IS_NULL);
        }
        if (fileId.length() != 32) {
            throw new GovernanceException(ErrorCode.FILE_ID_ILLEGAL);
        }

    }

    public static void validateFileSize(long fileSize) throws GovernanceException {
        if (fileSize <= 0) {
            throw new GovernanceException(ErrorCode.FILE_SIZE_ILLEGAL);
        }
    }

    public static void validateChunkIdx(int chunkIdx) throws GovernanceException {
        if (chunkIdx < 0) {
            throw new GovernanceException(ErrorCode.FILE_CHUNK_INDEX_ILLEGAL);
        }
    }

    public static void validateChunkData(byte[] chunkData) throws GovernanceException {
        if (chunkData == null || chunkData.length == 0) {
            throw new GovernanceException(ErrorCode.FILE_CHUNK_DATA_IS_NULL);
        }
    }

    public static void validateTransportName(String topic) throws GovernanceException {
        if (StringUtils.isBlank(topic)) {
            throw new GovernanceException(ErrorCode.TRANSPORT_ROLE_INVALID);
        }
    }

    public static void validateTransportRole(String roleId) throws GovernanceException {
        if (!ConstantProperties.TRANSPORT_RECEIVER.equals(roleId) && !ConstantProperties.TRANSPORT_SENDER.equals(roleId)) {
            throw new GovernanceException(ErrorCode.TRANSPORT_ROLE_INVALID);
        }
    }

    public static void validateOverWrite(String overWrite) throws GovernanceException {
        if (StringUtils.isBlank(overWrite) ||
                (!"0".equals(overWrite) && !"1".equals(overWrite))) {
            throw new GovernanceException(ErrorCode.TRANSPORT_OVERWRITE_INVALID);
        }
    }

}
