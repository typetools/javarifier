// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
//
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//

package org.htmlparser.tests.utilTests;

import org.htmlparser.Node;
import org.htmlparser.nodes.AbstractNode;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;
import org.htmlparser.visitors.NodeVisitor;

public class NodeListTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.utilTests.NodeListTest", "NodeListTest");
    }

    private NodeList nodeList;
    private Node[] testNodes;

    public NodeListTest(String name) {
        super(name);
    }

    protected void setUp() {
        nodeList = new NodeList();
    }

    public void testOneItemConstructor() {
        Node node = createHTMLNodeObject();
        nodeList = new NodeList(node);
        assertEquals("Vector Size",1,nodeList.size());
        assertTrue("First Element",node==nodeList.elementAt(0));
    }

    public void testAddOneItem() {
        Node node = createHTMLNodeObject();
        nodeList.add(node);
        assertEquals("Vector Size",1,nodeList.size());
        assertTrue("First Element",node==nodeList.elementAt(0));
    }

    public void testAddTwoItems() {
        Node node1 = createHTMLNodeObject();
        Node node2 = createHTMLNodeObject();
        nodeList.add(node1);
        nodeList.add(node2);
        assertEquals("Vector Size",2,nodeList.size());
        assertTrue("First Element",node1==nodeList.elementAt(0));
        assertTrue("Second Element",node2==nodeList.elementAt(1));
    }

    public void testAddTenItems() {
        createTestDataAndPutInVector(10);
        assertTestDataCouldBeExtractedFromVector(10);
    }

    public void testAddElevenItems() {
        createTestDataAndPutInVector(11);
        assertTestDataCouldBeExtractedFromVector(11);
    }

    public void testAddThirtyItems() {
        createTestDataAndPutInVector(30);
        assertTestDataCouldBeExtractedFromVector(30);
    }

    public void testAddThirtyOneItems() {
        createTestDataAndPutInVector(31);
        assertTestDataCouldBeExtractedFromVector(31);
    }

    public void testAddFiftyItems() {
        createTestDataAndPutInVector(50);
        assertTestDataCouldBeExtractedFromVector(50);
    }

    public void testAddFiftyOneItems() {
        createTestDataAndPutInVector(51);
        assertTestDataCouldBeExtractedFromVector(51);
    }

    public void testAddTwoHundredItems() {
        createTestDataAndPutInVector(200);
        assertTestDataCouldBeExtractedFromVector(200);
    }

    public void testElements() throws Exception {
        createTestDataAndPutInVector(11);
        Node [] resultNodes = new Node[11];
        int i = 0;
        for (SimpleNodeIterator e = nodeList.elements();e.hasMoreNodes();) {
            resultNodes[i] = e.nextNode();
            assertTrue("Node "+i+" did not match",testNodes[i]==resultNodes[i]);
            i++;
        }

    }

    private Node createHTMLNodeObject() {
        Node node = new AbstractNode(null,10,20) {
            public void accept(NodeVisitor visitor) {
            }

            public String toHtml() {
                return null;
            }

            public String toPlainTextString() {
                return null;
            }

            public String toString() {
                return "";
            }
        };
        return node;
    }

    private void createTestDataAndPutInVector(int nodeCount) {
        testNodes = new Node[nodeCount];
        for (int i=0;i<nodeCount;i++) {
            testNodes[i]= createHTMLNodeObject();
            nodeList.add(testNodes[i]);
        }
    }

    private void assertTestDataCouldBeExtractedFromVector(int nodeCount) {
        for (int i=0;i<nodeCount;i++) {
            assertTrue("Element "+i+" did not match",testNodes[i]==nodeList.elementAt(i));
        }
    }

    public void testToNodeArray() {
        createTestDataAndPutInVector(387);
        Node nodes [] = nodeList.toNodeArray();
        assertEquals("Length of array",387,nodes.length);
        for (int i=0;i<nodes.length;i++)
            assertNotNull("node "+i+" should not be null",nodes[i]);
    }

    public void testRemove() {
        Node node1 = createHTMLNodeObject();
        Node node2 = createHTMLNodeObject();
        nodeList.add(node1);
        nodeList.add(node2);
        assertEquals("Vector Size",2,nodeList.size());
        assertTrue("First Element",node1==nodeList.elementAt(0));
        assertTrue("Second Element",node2==nodeList.elementAt(1));
        nodeList.remove(1);
        assertEquals("List Size",1,nodeList.size());
        assertTrue("First Element",node1==nodeList.elementAt(0));
    }

    public void testRemoveAll() {
        Node node1 = createHTMLNodeObject();
        Node node2 = createHTMLNodeObject();
        nodeList.add(node1);
        nodeList.add(node2);
        assertEquals("Vector Size",2,nodeList.size());
        assertTrue("First Element",node1==nodeList.elementAt(0));
        assertTrue("Second Element",node2==nodeList.elementAt(1));
        nodeList.removeAll();
        assertEquals("List Size",0,nodeList.size());
        assertTrue("First Element",null==nodeList.elementAt(0));
        assertTrue("Second Element",null==nodeList.elementAt(1));
    }


    public void testIndexOf() {
        Node node1 = createHTMLNodeObject();
        Node node2 = createHTMLNodeObject();
        Node node3 = createHTMLNodeObject();
        nodeList.add(node1);
        nodeList.add(node2);
        nodeList.add(node3);
        assertEquals("Vector Size",3,nodeList.size());
        assertTrue("First Element",node1==nodeList.elementAt(0));
        assertTrue("Second Element",node2==nodeList.elementAt(1));
        assertTrue("Third Element",node3==nodeList.elementAt(2));
        assertTrue("Index wrong",1 == nodeList.indexOf(node2));
        assertTrue("Index wrong",0 == nodeList.indexOf(node1));
        assertTrue("Index wrong",2 == nodeList.indexOf(node3));
    }

    public void testRemoveItem() {
        Node node1 = createHTMLNodeObject();
        Node node2 = createHTMLNodeObject();
        nodeList.add(node1);
        nodeList.add(node2);
        assertEquals("Vector Size",2,nodeList.size());
        assertTrue("First Element",node1==nodeList.elementAt(0));
        assertTrue("Second Element",node2==nodeList.elementAt(1));
        nodeList.remove(node1);
        assertEquals("List Size",1,nodeList.size());
        assertTrue("First Element",node2==nodeList.elementAt(0));
    }

    public void testRemoveLastItem() {
        Node node1 = createHTMLNodeObject();
        Node node2 = createHTMLNodeObject();
        nodeList.add(node1);
        nodeList.add(node2);
        assertEquals("Vector Size",2,nodeList.size());
        assertTrue("First Element",node1==nodeList.elementAt(0));
        assertTrue("Second Element",node2==nodeList.elementAt(1));
        nodeList.remove(node2);
        assertEquals("List Size",1,nodeList.size());
        assertTrue("First Element",node1==nodeList.elementAt(0));
    }
}
