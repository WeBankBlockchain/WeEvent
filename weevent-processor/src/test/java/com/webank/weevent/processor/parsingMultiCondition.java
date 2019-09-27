package com.webank.weevent.processor;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.Test;

@Slf4j
public class parsingMultiCondition {
    @Test
    public void serverCondition() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE a>30 and a<40";
    }
}
