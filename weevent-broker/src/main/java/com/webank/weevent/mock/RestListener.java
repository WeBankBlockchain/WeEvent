package com.webank.weevent.mock;


import com.webank.weevent.protocol.rest.SubscriptionWeEvent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Mock interface onEvent for Restful subscribe.
 * request example:
 * curl -H "Content-Type: application/json" -d 'events=[{"topic":"com.webank.test.matthew","content":"hello world"},{"topic":"com.webank.test.matthew","content":"hello wevent"}]' http://localhost:8081/weevent/mock/rest/onEvent
 *
 * @author matthewliu
 * @since 2019/02/25
 */
@Slf4j
@RequestMapping(value = "/mock/rest")
@RestController
public class RestListener {
    @RequestMapping(path = "/onEvent", method = {RequestMethod.POST})
    public void onEvent(@RequestBody SubscriptionWeEvent subscriptionWeEvent) {
        log.info("mock restful onEvent, subscriptionId: {}, event: {}",
                subscriptionWeEvent.getSubscriptionId(),
                subscriptionWeEvent.getEvent());
    }
}
