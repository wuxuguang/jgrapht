/*
 * (C) Copyright 2005-2017, by John V Sichi and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.traverse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.EdgeReversedGraph;
import org.junit.Test;

/**
 * Tests for TopologicalOrderIterator.
 *
 * @author John V. Sichi
 * @since Apr 25, 2005
 */
public class TopologicalOrderIteratorTest
{

    @Test
    public void testRecipe()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String[] v = new String[9];

        v[0] = "preheat oven";
        v[1] = "sift dry ingredients";
        v[2] = "stir wet ingredients";
        v[3] = "mix wet and dry ingredients";
        v[4] = "spoon onto pan";
        v[5] = "bake";
        v[6] = "cool";
        v[7] = "frost";
        v[8] = "eat";

        // add in mixed up order
        graph.addVertex(v[4]);
        graph.addVertex(v[8]);
        graph.addVertex(v[1]);
        graph.addVertex(v[3]);
        graph.addVertex(v[7]);
        graph.addVertex(v[6]);
        graph.addVertex(v[0]);
        graph.addVertex(v[2]);
        graph.addVertex(v[5]);

        // specify enough edges to guarantee deterministic total order
        graph.addEdge(v[0], v[1]);
        graph.addEdge(v[1], v[2]);
        graph.addEdge(v[0], v[2]);
        graph.addEdge(v[1], v[3]);
        graph.addEdge(v[2], v[3]);
        graph.addEdge(v[3], v[4]);
        graph.addEdge(v[4], v[5]);
        graph.addEdge(v[5], v[6]);
        graph.addEdge(v[6], v[7]);
        graph.addEdge(v[7], v[8]);
        graph.addEdge(v[6], v[8]);

        Iterator<String> iter = new TopologicalOrderIterator<>(graph);
        int i = 0;

        while (iter.hasNext()) {
            assertEquals(v[i], iter.next());
            ++i;
        }

        // Test with a reversed view
        Graph<String, DefaultEdge> reversed = new EdgeReversedGraph<>(graph);

        iter = new TopologicalOrderIterator<>(reversed);
        i = v.length - 1;

        while (iter.hasNext()) {
            assertEquals(v[i], iter.next());
            --i;
        }
    }

    @Test
    public void testEmptyGraph()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        Iterator<String> iter = new TopologicalOrderIterator<>(graph);
        assertFalse(iter.hasNext());
    }

    @Test
    public void testGraph1()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        Graphs.addAllVertices(graph, Arrays.asList("v0", "v1", "v2", "v3", "v4", "v5"));
        graph.addEdge("v0", "v1");
        graph.addEdge("v0", "v2");
        graph.addEdge("v1", "v4");
        graph.addEdge("v2", "v4");
        graph.addEdge("v3", "v2");
        graph.addEdge("v3", "v4");
        graph.addEdge("v4", "v5");

        Iterator<String> it = new TopologicalOrderIterator<>(graph);
        assertTrue(it.hasNext());
        assertEquals("v0", it.next());
        assertTrue(it.hasNext());
        assertEquals("v3", it.next());
        assertTrue(it.hasNext());
        assertEquals("v1", it.next());
        assertTrue(it.hasNext());
        assertEquals("v2", it.next());
        assertTrue(it.hasNext());
        assertEquals("v4", it.next());
        assertTrue(it.hasNext());
        assertEquals("v5", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testGraphWithPartialOrder()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        Graphs.addAllVertices(graph, Arrays.asList("v0", "v1", "v2", "v3", "v4", "v5"));
        graph.addEdge("v0", "v1");
        graph.addEdge("v0", "v2");
        graph.addEdge("v1", "v4");
        graph.addEdge("v2", "v4");
        graph.addEdge("v3", "v2");
        graph.addEdge("v3", "v4");
        graph.addEdge("v4", "v5");

        Comparator<String> cf = (a, b) -> {
            if (a.equals("v0") && b.equals("v3")) {
                return 1;
            } else if (a.equals("v3") && b.equals("v0")) {
                return -1;
            }
            if (a.equals("v1") && b.equals("v2")) {
                return 1;
            } else if (a.equals("v2") && b.equals("v1")) {
                return -1;
            }
            return -1;
        };

        Iterator<String> it = new TopologicalOrderIterator<>(graph, cf);
        assertTrue(it.hasNext());
        assertEquals("v3", it.next());
        assertTrue(it.hasNext());
        assertEquals("v0", it.next());
        assertTrue(it.hasNext());
        assertEquals("v2", it.next());
        assertTrue(it.hasNext());
        assertEquals("v1", it.next());
        assertTrue(it.hasNext());
        assertEquals("v4", it.next());
        assertTrue(it.hasNext());
        assertEquals("v5", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testGraphWithParallelEdges()
    {
        Graph<String, DefaultEdge> graph = new DirectedPseudograph<>(DefaultEdge.class);

        Graphs.addAllVertices(graph, Arrays.asList("v0", "v1", "v2", "v3", "v4", "v5"));
        graph.addEdge("v0", "v1");
        graph.addEdge("v0", "v1");
        graph.addEdge("v0", "v2");
        graph.addEdge("v1", "v4");
        graph.addEdge("v2", "v4");
        graph.addEdge("v2", "v4");
        graph.addEdge("v3", "v2");
        graph.addEdge("v3", "v4");
        graph.addEdge("v4", "v5");

        Iterator<String> it = new TopologicalOrderIterator<>(graph);
        assertTrue(it.hasNext());
        assertEquals("v0", it.next());
        assertTrue(it.hasNext());
        assertEquals("v3", it.next());
        assertTrue(it.hasNext());
        assertEquals("v1", it.next());
        assertTrue(it.hasNext());
        assertEquals("v2", it.next());
        assertTrue(it.hasNext());
        assertEquals("v4", it.next());
        assertTrue(it.hasNext());
        assertEquals("v5", it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithSelfLoops()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        Graphs.addAllVertices(graph, Arrays.asList("v0", "v1", "v2"));
        graph.addEdge("v0", "v1");
        graph.addEdge("v0", "v2");
        graph.addEdge("v1", "v2");
        graph.addEdge("v2", "v2");

        new TopologicalOrderIterator<>(graph);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGraphWithCycle()
    {
        Graph<String, DefaultEdge> graph = new DirectedPseudograph<>(DefaultEdge.class);

        Graphs.addAllVertices(graph, Arrays.asList("v0", "v1", "v2", "v3", "v4", "v5"));
        graph.addEdge("v0", "v1");
        graph.addEdge("v0", "v1");
        graph.addEdge("v0", "v2");
        graph.addEdge("v1", "v4");
        graph.addEdge("v2", "v4");
        graph.addEdge("v2", "v4");
        graph.addEdge("v3", "v2");
        graph.addEdge("v3", "v4");
        graph.addEdge("v4", "v5");
        graph.addEdge("v5", "v2");

        Iterator<String> it = new TopologicalOrderIterator<>(graph);
        while (it.hasNext()) {
            it.next();
        }
    }

    @Test
    public void testDisconnected()
    {
        Graph<Integer, DefaultEdge> graph = new DirectedPseudograph<>(DefaultEdge.class);

        Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3));
        graph.addEdge(0, 1);
        graph.addEdge(2, 3);

        Comparator<Integer> cf = (a, b) -> {
            if (a < b) {
                return 1;
            } else if (a > b) {
                return -1;
            } else {
                return 0;
            }
        };

        Iterator<Integer> it = new TopologicalOrderIterator<>(graph, cf);
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(3), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(0), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTryToDisableCrossComponent()
    {
        Graph<Integer, DefaultEdge> graph = new DirectedPseudograph<>(DefaultEdge.class);
        new TopologicalOrderIterator<>(graph).setCrossComponentTraversal(false);
    }

}

// End TopologicalOrderIteratorTest.java
