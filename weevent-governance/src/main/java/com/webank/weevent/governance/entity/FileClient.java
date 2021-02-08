package com.webank.weevent.governance.entity;

import com.webank.weevent.file.IWeEventFileClient;
import com.webank.weevent.file.inner.DiskFiles;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileClient {

    private IWeEventFileClient weEventFileClient;
    private DiskFiles diskFiles;

}
