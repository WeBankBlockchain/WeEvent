package com.webank.weevent.processor;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.processor.utils.Util;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class parsingSingleCondition {

    @Test
    public void inCondition() throws JSQLParserException {
        final List exprList = new ArrayList();
        Select select = (Select) CCJSqlParserUtil.parse("select * from foo where x in (1,2,3)");
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        Expression where = plainSelect.getWhere();
        where.accept(new ExpressionVisitorAdapter() {

            @Override
            public void visit(InExpression expr) {
                super.visit(expr);
                exprList.add(expr.getLeftExpression());
                exprList.add(expr.getLeftItemsList());
                exprList.add(expr.getRightItemsList());
                ItemsList qq = expr.getRightItemsList();
            }
        });
        log.info(exprList.get(0).toString());
        log.info(exprList.get(2).toString());
        //
    }

    @Test
    public void likeCondition() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE a LIKE 'temperate*'";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();

        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        log.info(((StringValue) ((LikeExpression) plainSelect.getWhere()).
                getRightExpression()).getValue());
    }
    @Test
    public void testNamedParameter2() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE a = :param OR a = :param2 AND b = :param3";

        Statement st = CCJSqlParserUtil.parse(stmt);
        Select select = (Select) st;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        Expression exp_l = ((BinaryExpression) plainSelect.getWhere()).getLeftExpression();
        Expression exp_r = ((BinaryExpression) plainSelect.getWhere()).getRightExpression();
        Expression exp_rl = ((BinaryExpression) exp_r).getLeftExpression();
        Expression exp_rr = ((BinaryExpression) exp_r).getRightExpression();

        Expression exp_param1 = ((BinaryExpression) exp_l).getRightExpression();
        Expression exp_param2 = ((BinaryExpression) exp_rl).getRightExpression();
        Expression exp_param3 = ((BinaryExpression) exp_rr).getRightExpression();

        assertTrue(exp_param1 instanceof JdbcNamedParameter);
        assertTrue(exp_param2 instanceof JdbcNamedParameter);
        assertTrue(exp_param3 instanceof JdbcNamedParameter);

        JdbcNamedParameter namedParameter1 = (JdbcNamedParameter) exp_param1;
        JdbcNamedParameter namedParameter2 = (JdbcNamedParameter) exp_param2;
        JdbcNamedParameter namedParameter3 = (JdbcNamedParameter) exp_param3;

        assertEquals("param", namedParameter1.getName());
        assertEquals("param2", namedParameter2.getName());
        assertEquals("param3", namedParameter3.getName());
    }
    @Test
    public void testNamedParameter3() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE a = :param OR a = :param2 AND b = :param3";

        Statement st = CCJSqlParserUtil.parse(stmt);
        Select select = (Select) st;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<Expression> whereList = new ArrayList<>();
        List<String> operatorList = new ArrayList<>();

        Expression exp_l1 = ((BinaryExpression) plainSelect.getWhere()).getLeftExpression();
        Expression exp_r1 = ((BinaryExpression) plainSelect.getWhere()).getRightExpression();
        String exp_middle = ((BinaryExpression) plainSelect.getWhere()).getStringExpression();

        whereList.add(exp_l1);
        operatorList.add(exp_middle);

        Expression expression = exp_r1;
        while(((BinaryExpression) expression).getASTNode()==null){
                Expression exp_rl = ((BinaryExpression) expression).getLeftExpression();
                Expression exp_rr = ((BinaryExpression) expression).getRightExpression();
                String exp_mid = ((BinaryExpression) expression).getStringExpression();
                whereList.add(exp_rl);
                operatorList.add(exp_mid);
                log.info("exp_rl:{},exp_mid:{}  if....", exp_rl.toString(), exp_mid);
                if(((BinaryExpression) expression).getRightExpression().getASTNode().jjtGetNumChildren()>0){
                    whereList.add(expression);
                    log.info("whereList size:{}", whereList.size());
                    log.info("operatorList size:{}", operatorList.size());
                    break;
                }

        }
        log.info("========================================================");


        Expression exp_l = ((BinaryExpression) plainSelect.getWhere()).getLeftExpression();
        Expression exp_r = ((BinaryExpression) plainSelect.getWhere()).getRightExpression();
        Expression exp_rl = ((BinaryExpression) exp_r).getLeftExpression();
        Expression exp_rr = ((BinaryExpression) exp_r).getRightExpression();

        Expression exp_param1 = ((BinaryExpression) exp_l).getRightExpression();
        Expression exp_param2 = ((BinaryExpression) exp_rl).getRightExpression();
        Expression exp_param3 = ((BinaryExpression) exp_rr).getRightExpression();

        assertTrue(exp_param1 instanceof JdbcNamedParameter);
        assertTrue(exp_param2 instanceof JdbcNamedParameter);
        assertTrue(exp_param3 instanceof JdbcNamedParameter);

        JdbcNamedParameter namedParameter1 = (JdbcNamedParameter) exp_param1;
        JdbcNamedParameter namedParameter2 = (JdbcNamedParameter) exp_param2;
        JdbcNamedParameter namedParameter3 = (JdbcNamedParameter) exp_param3;

        assertEquals("param", namedParameter1.getName());
        assertEquals("param2", namedParameter2.getName());
        assertEquals("param3", namedParameter3.getName());
    }


    @Test
    public void testNamedParameter12() throws Exception {

        String stmt = "SELECT * FROM mytable WHERE a = :param OR a = :param2 AND b = :param3";

        CCJSqlParserManager parserManager = new CCJSqlParserManager();

        Statement st = CCJSqlParserUtil.parse(stmt);
        Select select = (Select) st;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        Expression exp_l = ((BinaryExpression) plainSelect.getWhere()).getLeftExpression();
        Expression exp_r = ((BinaryExpression) plainSelect.getWhere()).getRightExpression();
        Expression exp_rl = ((BinaryExpression) exp_r).getLeftExpression();
        Expression exp_rr = ((BinaryExpression) exp_r).getRightExpression();

        Expression exp_param1 = ((BinaryExpression) exp_l).getRightExpression();
        Expression exp_param2 = ((BinaryExpression) exp_rl).getRightExpression();
        Expression exp_param3 = ((BinaryExpression) exp_rr).getRightExpression();

        assertTrue(exp_param1 instanceof JdbcNamedParameter);
        assertTrue(exp_param2 instanceof JdbcNamedParameter);
        assertTrue(exp_param3 instanceof JdbcNamedParameter);

        JdbcNamedParameter namedParameter1 = (JdbcNamedParameter) exp_param1;
        JdbcNamedParameter namedParameter2 = (JdbcNamedParameter) exp_param2;
        JdbcNamedParameter namedParameter3 = (JdbcNamedParameter) exp_param3;

        assertEquals("param", namedParameter1.getName());
        assertEquals("param2", namedParameter2.getName());
        assertEquals("param3", namedParameter3.getName());
        String operationStr = "OTHER";
        String eventContent = "{alexa:10}";
        List<String> contentKeys = Util.getKeys(eventContent);
        try {
            Select select2 = (Select) parserManager.parse(new StringReader(stmt));
            plainSelect = (PlainSelect) select2.getSelectBody();

            List<String> oper = new ArrayList<>(Arrays.asList("=", "<>", "!=", "<", ">", ">=", "<=", "BETWEEN", "LIKE", "IN"));

            for (int i = 0; i < oper.size(); i++) {
                if (stmt.contains(oper.get(i))) {
                    log.info("current:{}", oper.get(i));
                    operationStr = oper.get(i);
                }
            }
        } catch (Exception e) {
            log.info("exception: {}", e.toString());
        }
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
                        if (Integer.valueOf(extract) == RightKey) {

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
                log.info("check:start: {},end: {},other:{}", ((Between) plainSelect.getWhere()).getBetweenExpressionStart().toString(), ((Between) plainSelect.getWhere()).getBetweenExpressionEnd().toString(), ((Between) plainSelect.getWhere()).getLeftExpression().toString());
                log.info("check:start: {},end: {}", ((Between) plainSelect.getWhere()).getBetweenExpressionStart().toString(), ((Between) plainSelect.getWhere()).getBetweenExpressionEnd().toString());
                int leftValue = Integer.valueOf(((Between) plainSelect.getWhere()).getBetweenExpressionStart().toString());
                int rightValue = Integer.valueOf(((Between) plainSelect.getWhere()).getBetweenExpressionEnd().toString());
                leftKey = ((Between) plainSelect.getWhere()).getLeftExpression().toString();
                if ("alexa".equals(leftKey)) {
                    // compare the value
                    JSONObject jObj = new JSONObject(eventContent);
                    String extract = Util.recurseKeys(jObj, "alexa");
                    if (Integer.valueOf(extract) > leftValue && Integer.valueOf(extract) < rightValue) {
                        log.info("hit it....");
                        //flag = true;
                        break;
                    }
                }
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
