package com.webank.weevent.file.dto;


import com.webank.weevent.file.service.FileChunksMeta;
import com.webank.weevent.client.WeEventPlus;

import lombok.Getter;
import lombok.Setter;

/**
 * FileChunksMeta plus used in verify.
 *
 * @author matthewliu
 * @since 2020/03/16
 */
@Getter
@Setter
public class FileChunksMetaPlus {
    private FileChunksMeta file;
    private WeEventPlus plus;
}
