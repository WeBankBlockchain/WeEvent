package com.webank.weevent.file.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Setter
@ToString
@Component
@PropertySource(value = "classpath:weevent.properties", encoding = "UTF-8")
public class WeEventFileConfig {

    @Value("${block.chain.type:fisco}")
    private String blockChainType;

    @Value("${file.path:./logs/file}")
    private String filePath;

    @Value("${file.chunk.size:1048576}")
    private int fileChunkSize;
}
