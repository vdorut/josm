// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.validation.tests;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.josm.JOSMFixture;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

/**
 * JUnit Test of "Public Transport Route" validation test.
 */
public class PublicTransportRouteTestTest {

    final PublicTransportRouteTest test = new PublicTransportRouteTest();

    /**
     * Setup test.
     */
    @Before
    public void setUp() {
        JOSMFixture.createUnitTestFixture().init();
    }

    /**
     * Performs various tests.
     */
    @Test
    public void test() {
        final List<Node> nodes = Arrays.asList(new Node(), new Node(), new Node(), new Node(), new Node(), new Node());
        final Way w1 = TestUtils.newWay("", nodes.get(0), nodes.get(1));
        final Way w2 = TestUtils.newWay("", nodes.get(1), nodes.get(2));
        final Way w3 = TestUtils.newWay("", nodes.get(3), nodes.get(2));
        final Way w4 = TestUtils.newWay("", nodes.get(3), nodes.get(4));

        test.startTest(null);
        test.visit(TestUtils.newRelation("type=route route=tram public_transport:version=2"));
        test.visit(TestUtils.newRelation("type=unknown"));
        assertEquals(0, test.getErrors().size());

        final Relation r2 = TestUtils.newRelation("type=route route=tram public_transport:version=2",
                new RelationMember("", w1), new RelationMember("", w2), new RelationMember("", w3), new RelationMember("", w4));
        test.startTest(null);
        test.visit(r2);
        assertEquals(0, test.getErrors().size());

        final Relation r3 = TestUtils.newRelation("type=route route=tram public_transport:version=2",
                new RelationMember("forward", w1));
        test.startTest(null);
        test.visit(r3);
        assertEquals(1, test.getErrors().size());
        assertEquals("Route relation contains a 'forward/backward' role", test.getErrors().get(0).getMessage());

        final Relation r4 = TestUtils.newRelation("type=route route=tram public_transport:version=2",
                new RelationMember("", w1), new RelationMember("", w3), new RelationMember("", w2));
        test.startTest(null);
        test.visit(r4);
        assertEquals(1, test.getErrors().size());
        assertEquals("Route relation contains a gap", test.getErrors().get(0).getMessage());

    }
}