package com.webank.weevent.broker.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * WeEvent Build Info that auto loaded by spring ApplicationContext
 *
 * @author v_wbhwliu
 * @version 1.0
 * @since 2019/9/06
 */
@Slf4j
@Getter
@Setter
@Component
@PropertySource(value = "classpath:git.properties", encoding = "UTF-8")
public class BuildInfo {
    @Value("${git.build.version:}")
    private String weEventVersion;

    @Value("${git.commit.time:}")
    private String gitCommitTimeStamp;

    @Value("${git.branch:}")
    private String gitBranch;

    @Value("${git.commit.id.abbrev:}")
    private String gitCommitHash;
}