package com.webank.weevent.processor;

import java.io.StringReader;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Slf4j
public class parsingMultiCondition {

    @Rule
    public TestName name = new TestName();

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testNotLike() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE a NOT LIKE 'test'";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("test", ((StringValue) ((LikeExpression) plainSelect.getWhere()).
                getRightExpression()).getValue());
        assertEquals(true, (boolean) ((LikeExpression) plainSelect.getWhere()).isNot());
    }

    @Test
    public void testIsNot() throws JSQLParserException {
        String stmt = "SELECT * FROM test WHERE a IS NOT NULL";
    }

    @Test
    public void serverCondition() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b ORDER BY tab1.a DESC, tab1.b ASC";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals(2, plainSelect.getOrderByElements().size());
        assertEquals("tab1.a",
                ((Column) plainSelect.getOrderByElements().get(0).getExpression())
                        .getFullyQualifiedName());
        assertEquals("b",
                ((Column) plainSelect.getOrderByElements().get(1).getExpression()).getColumnName());
        assertTrue(plainSelect.getOrderByElements().get(1).isAsc());
        assertFalse(plainSelect.getOrderByElements().get(0).isAsc());

        statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b ORDER BY tab1.a, 2";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals(2, plainSelect.getOrderByElements().size());
        assertEquals("a",
                ((Column) plainSelect.getOrderByElements().get(0).getExpression()).getColumnName());
        assertEquals(2,
                ((LongValue) plainSelect.getOrderByElements().get(1).getExpression()).getValue());
    }

    @Test
    public void testLimit() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT 3, ?";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        Expression offset = ((PlainSelect) select.getSelectBody()).getLimit().getOffset();
        Expression rowCount = ((PlainSelect) select.getSelectBody()).getLimit().getRowCount();

        assertEquals(3, ((LongValue) offset).getValue());
        assertTrue(rowCount instanceof JdbcParameter);
        assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isLimitAll());



        statement = "SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?";
        select = (Select) parserManager.parse(new StringReader(statement));

        assertNull(((PlainSelect) select.getSelectBody()).getLimit());
        assertNotNull(((PlainSelect) select.getSelectBody()).getOffset());
        assertEquals("?", ((PlainSelect) select.getSelectBody()).getOffset().getOffsetJdbcParameter().toString());


        statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION "
                + "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) LIMIT 3, 4";
        select = (Select) parserManager.parse(new StringReader(statement));
        SetOperationList setList = (SetOperationList) select.getSelectBody();
        offset = setList.getLimit().getOffset();
        rowCount = setList.getLimit().getRowCount();

        assertEquals(3, ((LongValue) offset).getValue());
        assertEquals(4, ((LongValue) rowCount).getValue());


    }
    @Test
    public void testLimitSqlServer1() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 ORDER BY mytable.id OFFSET 3 ROWS FETCH NEXT 5 ROWS ONLY";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertNotNull(((PlainSelect) select.getSelectBody()).getOffset());
        assertEquals("ROWS", ((PlainSelect) select.getSelectBody()).getOffset().getOffsetParam());
        assertNotNull(((PlainSelect) select.getSelectBody()).getFetch());
        assertEquals("ROWS", ((PlainSelect) select.getSelectBody()).getFetch().getFetchParam());
        assertFalse(((PlainSelect) select.getSelectBody()).getFetch().isFetchParamFirst());
        assertNull(((PlainSelect) select.getSelectBody()).getOffset().getOffsetJdbcParameter());
        assertNull(((PlainSelect) select.getSelectBody()).getFetch().getFetchJdbcParameter());
        assertEquals(3, ((PlainSelect) select.getSelectBody()).getOffset().getOffset());
        assertEquals(5, ((PlainSelect) select.getSelectBody()).getFetch().getRowCount());
    }


}
