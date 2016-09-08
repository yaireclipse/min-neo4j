package org.yairshefi.neo4j.min;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;

/**
 * Created by yairshefi on 9/8/16.
 */
// TODO: add oh so many tests
public class StatementsJsonBuilderTests {

    @Test
    public void testSingleParamGroup() {
        final String cql = "MATCH (n:Person {funki})";
        final StatementsJsonBuilder stmtBuilder = StatementsJsonBuilder.create().statement(cql);
        Assert.assertTrue(stmtBuilder.getAllQueries().get(0).getParametersGroups().containsKey("funki"));
    }

    @Test
    public void testMultipleParamsGroups() {
        final String cql = "MATCH (n:Person {funki})-[FRIEND {props}]->(nFriends:Person) DETACH DELETE n,nFriends";
        final StatementsJsonBuilder stmtBuilder = StatementsJsonBuilder.create().statement(cql);
        Assert.assertTrue(stmtBuilder.getAllQueries().get(0).getParametersGroups().containsKey("funki"));
        Assert.assertTrue(stmtBuilder.getAllQueries().get(0).getParametersGroups().containsKey("props"));
    }

    @Test
    public void testActualValuesNotParsedAsParamGroup() {
        final String cql = "MATCH (n:Person {funki})-[FRIEND {props}]->(nFriends:Person {asdf:\"asf\"}) DETACH DELETE n,nFriends";
        final StatementsJsonBuilder stmtBuilder = StatementsJsonBuilder.create().statement(cql);
        Assert.assertTrue(stmtBuilder.getAllQueries().get(0).getParametersGroups().containsKey("funki"));
        Assert.assertTrue(stmtBuilder.getAllQueries().get(0).getParametersGroups().containsKey("props"));
        Assert.assertFalse(stmtBuilder.getAllQueries().get(0).getParametersGroups().containsKey("asdf"));
        Assert.assertFalse(stmtBuilder.getAllQueries().get(0).getParametersGroups().containsKey("asdf:\"asf\""));
    }

    @Test
    public void testParamGroupOfValues() {
        final String cql = "MATCH (a:Person) WHERE a.fbProfileId = '123' CREATE p =(a)-[:FRIEND]->(b:Person { friendProps }) RETURN p";
        final StatementsJsonBuilder stmtBuilder = StatementsJsonBuilder.create().statement(cql);
        Assert.assertTrue(stmtBuilder.getAllQueries().get(0).getParametersGroups().containsKey("friendProps"));
    }
}
