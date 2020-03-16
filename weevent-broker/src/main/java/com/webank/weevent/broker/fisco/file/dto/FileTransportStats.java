package com.webank.weevent.broker.fisco.file.dto;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Stats of file transport service.
 *
 * @author matthewliu
 * @since 2020/03/12
 */
@Getter
@Setter
public class FileTransportStats {
    // groupId -> topic -> files
    private Map<String, Map<String, List<FileChunksMetaStatus>>> sender = new HashMap<>();
    // groupId -> topic -> files
    private Map<String, Map<String, List<FileChunksMetaStatus>>> receiver = new HashMap<>();

    public FileTransportStats() {
    }
}
