package org.yairshefi.neo4j.min.examples;

import org.json.JSONObject;
import org.yairshefi.neo4j.min.Neo4jClient;
import org.yairshefi.neo4j.min.StatementsJsonBuilder;

import java.io.IOException;

/**
 * Created by yairshefi on 9/8/16.
 */
public class SimpleExample {

    public static void main(String[] args) throws IOException {

        /** 1. Create a client **/
        final Neo4jClient neo4jClient =
                new Neo4jClient.Builder()
                        .host("localhost").port(7474)
                        .user("neo4j").password("neo4j")
                    .build();

        /** 2. Create as many parameterized statements as required
               for your single super-complex commit **/
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

        /** 3. Post it to Neo4j sever! **/
        final JSONObject response = neo4jClient.post(stmtsJson);

        System.out.println(response);
    }
}
