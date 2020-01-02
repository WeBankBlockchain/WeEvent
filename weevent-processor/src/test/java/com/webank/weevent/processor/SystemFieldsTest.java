package com.webank.weevent.processor;

import com.webank.weevent.processor.utils.SystemFields;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Slf4j
public class SystemFieldsTest {
    @Test
    public void getByClassCodeAndInfoCode() {
        log.info("field2:{}", SystemFields.getByClassCodeAndInfoCode("eventId"));
        assertEquals(SystemFields.EVENT_ID, SystemFields.getByClassCodeAndInfoCode("eventId"));
    }

    @Test
    public void getByClassCodeAndInfoCode2() {

        log.info("field2:{}", SystemFields.getByClassCodeAndInfoCode("EVENT_ID111"));
        assertNull(SystemFields.getByClassCodeAndInfoCode("EVENT_ID111"));
    }
}
