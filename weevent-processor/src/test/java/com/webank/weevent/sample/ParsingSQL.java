package com.webank.weevent.processor.mq;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.webank.weevent.processor.utils.Util;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.json.JSONObject;

@Slf4j
public class ParsingSQL {
    // paring the condition and hit the message
    public static void main(String[] args) throws Exception {
        String eventContent = "{temperate:35}";
        String condition = "temperate=35";
        boolean flag = false;
        List<String> contentKeys = Util.getKeys(eventContent);
        String trigger = "SELECT * FROM table WHERE temperate=35";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        String operationStr = "OTHER";
        PlainSelect plainSelect = null;
        try {
            Select select = (Select) parserManager.parse(new StringReader(trigger));
            plainSelect = (PlainSelect) select.getSelectBody();

            List<String> oper = new ArrayList<>(Arrays.asList("=", "<>", "!=", "<", ">", ">=", "<=", "BETWEEN", "LIKE", "IN"));

            for (int i = 0; i < oper.size(); i++) {
                if (trigger.contains(oper.get(i))) {
                    log.info("current:{}", oper.get(i));
                    operationStr = oper.get(i);
                }
            }
        } catch (Exception e) {
            log.info("exception: {}", e.toString());
        }
        // parsing the operation
        switch (operationStr) {
            case "=":
                log.info("EQUALS_TO:{}", operationStr);
                log.info("left: {},right: {}", (((EqualsTo) plainSelect.getWhere()).getLeftExpression()).toString(), (((EqualsTo) plainSelect.getWhere()).getRightExpression()).toString());
                String leftKey = (((EqualsTo) plainSelect.getWhere()).getLeftExpression()).toString();
                int RightKey = Integer.valueOf((((EqualsTo) plainSelect.getWhere()).getRightExpression()).toString());
                for (int i = 0; i < contentKeys.size(); i++) {
                    if (contentKeys.get(i).equals(leftKey)) {
                        // compare the value
                        JSONObject jObj = new JSONObject(eventContent);
                        String extract = Util.recurseKeys(jObj, contentKeys.get(i));
                        log.info("extract:{}", extract);
                        // int number = Integer.valueOf(extract);
                        if (Integer.valueOf(extract) == RightKey) {
                            flag = true;
                            log.info("hit it extract");
                            break;
                        }
                    }
                }
                break;

            case "<>":
                log.info("NOT_QUALS_TO:{}", operationStr);
                log.info("check:{}", (((NotEqualsTo) plainSelect.getWhere()).getRightExpression()).toString());

                break;

            case "!=":
                log.info("NOT_QUALS_TO:{}", operationStr);
                log.info("check:{}", (((NotEqualsTo) plainSelect.getWhere()).getRightExpression()).toString());

                break;

            case "<":
                log.info("MINOR_THAN:{}", operationStr);
                log.info("check:{}", (((MinorThan) plainSelect.getWhere()).getRightExpression()).toString());
                break;

            case "<=":
                log.info("MINOR_THAN_EQUAL:{}", operationStr);
                log.info("check:{}", (((MinorThanEquals) plainSelect.getWhere()).getRightExpression()).toString());

                break;

            case ">":
                log.info("GREATER_THAN:{}", operationStr);


                log.info("check:{}", (((GreaterThan) plainSelect.getWhere()).getRightExpression()).toString());
                break;

            case ">=":
                log.info("GREATER_THAN_EQUAL:{}", operationStr);
                log.info("check:{}", (((GreaterThanEquals) plainSelect.getWhere()).getRightExpression()).toString());

                break;

            case "BETWEEN":
                log.info("BETWEEN:{}", operationStr);
                log.info("check:start: {},end{}", ((Between) plainSelect.getWhere()).getBetweenExpressionStart().toString(), ((Between) plainSelect.getWhere()).getBetweenExpressionEnd().toString());

                break;

            case "LIKE":
                log.info("LIKE:{}", operationStr);
                log.info("check:like: ", ((LikeExpression) plainSelect.getWhere()).toString());

                break;

            case "IN":
                log.info("IN:{}", operationStr);
                log.info("check:like: ", ((InExpression) plainSelect.getWhere()).toString());

                break;

            default:
                log.error("other ", operationStr);


        }
    }
}

// 1. 根据不同，进行匹配出where,然后获取其中的值