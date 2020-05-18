package com.webank.weevent.jms;


import com.webank.weevent.client.WeEvent;

/**
 * Event dispatch interface.
 *
 * @author matthewliu
 * @since 2019/04/11
 */

public interface CommandDispatcher {
    void dispatch(WeEvent event);
}
