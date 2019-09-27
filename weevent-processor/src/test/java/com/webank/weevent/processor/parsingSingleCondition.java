package com.webank.weevent.processor;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

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
    public void likeCondition() throws JSQLParserException{
        String statement = "SELECT * FROM tab1 WHERE a LIKE 'temperate*'";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();

        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        log.info(((StringValue) ((LikeExpression) plainSelect.getWhere()).
                getRightExpression()).getValue());
    }
}
