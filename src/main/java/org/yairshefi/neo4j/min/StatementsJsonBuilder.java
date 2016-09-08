package org.yairshefi.neo4j.min;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by yairshefi on 9/8/16.
 */
public class StatementsJsonBuilder {

    private final static Pattern pattern = Pattern.compile("(\\{[\\s]*[\\w]+[\\s]*\\})");

    private final List<Statement> statements = new ArrayList<>();

    private Statement currentStmt = null;

    private StatementsJsonBuilder() {}

    public static StatementsJsonBuilder create() {
        return new StatementsJsonBuilder();
    }

    public StatementsJsonBuilder statement(final String cql) {
        currentStmt = new Statement();
        currentStmt.cql = cql.trim();
        final Matcher matcher = pattern.matcher(currentStmt.getCql());
        while ( matcher.find() ) {
            final String match = matcher.group();
            // remove enclosing curly braces and then trim spaces:
            final String paramsGroupName = match.substring(1, match.length() - 1).trim();
            currentStmt.params.put(paramsGroupName, new HashMap<>());
        }
        statements.add(currentStmt);
        return this;
    }

    public StatementsJsonBuilder param(final String group, final String name, final Object value) {
        if (currentStmt == null) {
            throw new IllegalArgumentException(
                    String.format("cannot add parameter[%s=%s] to parameters group '%s'. No cql was yet added to this %s",
                            name, value, group, currentStmt.getCql(), StatementsJsonBuilder.class.getSimpleName()));
        }
        if (value == null) {
            return this;
        }
        final Map<String, Map<String, Object>> allGroups = currentStmt.getParametersGroups();
        if ( ! allGroups.containsKey(group) ) {
            throw new IllegalArgumentException(
                    String.format("cannot add parameter[%s=%s] to parameters group '%s'. The group's name isn't specified in the cql[%s]",
                            name, value, group, currentStmt.getCql()));
        }
        final Map<String, Object> paramsGroup = allGroups.get(group);
        paramsGroup.put(name, value);
        return this;
    }

    public JSONObject build() {
        final JSONArray stmts = new JSONArray();
        for (final Statement statement : statements) {
            final JSONObject createStmt = new JSONObject().put("statement", statement.getCql());
            final JSONObject parameters = new JSONObject();
            for (final Entry<String, Map<String, Object>> groupEntry : statement.getParametersGroups().entrySet() ) {
                parameters.put(groupEntry.getKey(), groupEntry.getValue());
            }
            createStmt.put("parameters", parameters);
            stmts.put(createStmt);
        }
        final JSONObject rootJson = new JSONObject().put("statements", stmts);
        return rootJson;
    }

    public List<Statement> getAllQueries() {
        return statements;
    }

    public static class Statement {

        private String cql;
        private Map<String, Map<String, Object>> params = new HashMap<>();

        public String getCql() {
            return cql;
        }

        public Map<String, Map<String, Object>> getParametersGroups() {
            return params;
        }
    }
}

