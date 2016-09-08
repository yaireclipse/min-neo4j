package org.yairshefi.neo4j.min;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.JsonOrgJsonProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilder;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * Created by yairshefi on 9/8/16.
 */
public class IntegrationTests {

    private final ParseContext jsonParser =
            JsonPath.using(Configuration.builder().jsonProvider(new JsonOrgJsonProvider()).build());

    private final TestServerBuilder serverBuilder = TestServerBuilders.newInProcessBuilder()
            .withFixture(graphDatabaseService -> {
                try (Transaction tx = graphDatabaseService.beginTx()) {
                    graphDatabaseService.createNode(Label.label("User"));
                    tx.success();
                }
                return null;
            });

    public ServerControls neo4j;

    private Neo4jClient neo4jClient;

    @Before
    public void setUp() {

        neo4j = serverBuilder.newServer();

        neo4jClient = new Neo4jClient.Builder()
                .host(neo4j.httpURI().getHost())
                .port(neo4j.httpURI().getPort())
                .build();
    }

    @After
    public void tearDown() {
        neo4j.close();
    }

    @Test
    public void testBasic() throws IOException {

        final StatementsJsonBuilder stmtBuilder = StatementsJsonBuilder.create();
        stmtBuilder.statement("CREATE (Neo:User {name: 'Neo'}), (Morpheus:User {name: 'Morpheus'})");
        stmtBuilder.statement("MATCH (user:User) RETURN user");
        final JSONObject rootJson = stmtBuilder.build();

        final JSONObject response = neo4jClient.post(rootJson);

        final DocumentContext parsedResponse = jsonParser.parse(response);
        assertEquals("Neo", parsedResponse.read("$.results[1].data[1].row[0].name"));
        assertEquals("Morpheus", parsedResponse.read("$.results[1].data[2].row[0].name"));
    }

    @Test
    public void testComplexExample() throws IOException {

        JSONObject stmtsJson = StatementsJsonBuilder.create()

                /* First, a non-parameterized simple CQL
                   - creating a User named Neo that chose the blue pill */
                .statement("CREATE (Neo:User {name: 'Neo', pill: 'blue'})")

                /* Parameterizing CREATE CQL props
                   - creating a User named Morpheus that chose the blue pill
                   - and a User named Neo that chose the red pill
                 */
                .statement("CREATE (Morpheus:User {createMorpheusProps}), (Neo2:User {createNeoProps})")
                    /* params always apply ONLY to the last statement added to the builder */
                .param("createMorpheusProps", "name", "Morpheus")
                .param("createMorpheusProps", "pill", "blue")
                .param("createNeoProps", "name", "Neo")
                .param("createNeoProps", "pill", "red")

                /* Merge propsMap into all Users:
                   - equivalent to the CQL: MERGE (u:User) SET u += {team: 'matrix', type: 'humans', canFly: true} */
                .statement("MERGE (u:User) SET u += {propsMap}")
                .param("propsMap", "team", "matrix")
                .param("propsMap", "type", "humans")
                .param("propsMap", "canFly", true)

                /* Parameterizing specific values in MERGE:
                        (explicitly indicating parameter names is a Neo4j's constraint, see
                        http://neo4j.com/docs/developer-manual/current/cypher/#merge-using-map-parameters-with-merge ).
                   - selecting the User Neo that chose the blue pill: */
                .statement("MERGE (u:User {name: {matchParams}.name, pill: {matchParams}.pill}) RETURN u")
                .param("matchParams", "name", "Neo")
                .param("matchParams", "pill", "blue")

                .build();

        final JSONObject response = neo4jClient.post(stmtsJson);

        final DocumentContext parsedResponse = jsonParser.parse(response);

        assertEquals("Neo", parsedResponse.read("$.results[3].data[0].row[0].name"));
        assertEquals("blue", parsedResponse.read("$.results[3].data[0].row[0].pill"));
        assertEquals("matrix", parsedResponse.read("$.results[3].data[0].row[0].team"));
        assertEquals("humans", parsedResponse.read("$.results[3].data[0].row[0].type"));
        assertEquals(true, parsedResponse.read("$.results[3].data[0].row[0].canFly"));
    }
}
