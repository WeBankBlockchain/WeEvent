package com.webank.weevent.processor.sample;

import java.io.StringReader;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

@Slf4j
public class ParsingLike {

    public static void main(String[] args) throws Exception {
        String statement = "SELECT * FROM tab1 WHERE a LIKE 'temperate*'";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();

        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        log.info(((StringValue) ((LikeExpression) plainSelect.getWhere()).
                        getRightExpression()).getValue());
    }
}
