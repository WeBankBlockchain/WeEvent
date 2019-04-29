package com.webank.weevent.sdk.jms;


/**
 * Event dispatch interface.
 *
 * @author matthewliu
 * @since 2019/04/11
 */
public interface CommandDispatcher {
    void dispatch(WeEventStompCommand command);
}
