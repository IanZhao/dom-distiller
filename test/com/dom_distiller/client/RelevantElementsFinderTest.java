// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.dom_distiller.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Display;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RelevantElementsFinderTest extends DomDistillerTestCase {
    private static final Set<Node> mEmptySet = Collections.emptySet();

    public void testNoImage() {
        Node root = TestUtil.createDiv(0);
        Node contentText = TestUtil.createText("content");
        root.appendChild(contentText);

        List<Node> contentNodes = Arrays.<Node>asList(contentText);
        List<Node> contentAndImages = RelevantElementsFinder.findAndAddElements(contentNodes,
                mEmptySet, mEmptySet, root);

        assertEquals(1, contentAndImages.size());
        assertEquals(contentText, contentAndImages.get(0));
    }

    public void testImageAfterContent() {
        Node root = TestUtil.createDiv(0);
        Node contentText = TestUtil.createText("content");
        Node image = TestUtil.createImage();
        root.appendChild(contentText);
        root.appendChild(image);

        List<Node> contentNodes = Arrays.<Node>asList(contentText);
        List<Node> contentAndImages = RelevantElementsFinder.findAndAddElements(contentNodes,
                mEmptySet, mEmptySet, root);

        assertEquals(2, contentAndImages.size());
        assertEquals(contentText, contentAndImages.get(0));
        assertEquals(image, contentAndImages.get(1));
    }

    public void testInvisibleImageAfterContentIsHidden() {
        Node root = TestUtil.createDiv(0);
        Node contentText = TestUtil.createText("content");
        Node image = TestUtil.createImage();
        Element.as(image).getStyle().setDisplay(Display.NONE);
        root.appendChild(contentText);
        root.appendChild(image);

        List<Node> contentNodes = Arrays.<Node>asList(contentText);
        Set<Node> hiddenElems = Collections.singleton(image);
        List<Node> contentAndImages = RelevantElementsFinder.findAndAddElements(contentNodes,
                hiddenElems, mEmptySet, root);

        assertEquals(1, contentAndImages.size());
        assertEquals(contentText, contentAndImages.get(0));
    }

    public void testImageAfterContentInInvisibleParentIsHidden() {
        Node root = TestUtil.createDiv(0);
        Node contentText = TestUtil.createText("content");
        Node parent = TestUtil.createDiv(1);
        Element.as(parent).getStyle().setDisplay(Display.NONE);
        Node image = TestUtil.createImage();
        parent.appendChild(image);
        root.appendChild(contentText);
        root.appendChild(parent);

        List<Node> contentNodes = Arrays.<Node>asList(contentText);
        Set<Node> hiddenElems = Collections.singleton(parent);
        List<Node> contentAndImages = RelevantElementsFinder.findAndAddElements(contentNodes,
                hiddenElems, mEmptySet, root);

        assertEquals(1, contentAndImages.size());
        assertEquals(contentText, contentAndImages.get(0));
    }

    public void testImageAfterNonContent() {
        Node root = TestUtil.createDiv(0);
        Node contentText = TestUtil.createText("content");
        Node nonContentText = TestUtil.createText("not content");
        Node image = TestUtil.createImage();
        root.appendChild(contentText);
        root.appendChild(nonContentText);
        root.appendChild(image);

        List<Node> contentNodes = Arrays.<Node>asList(contentText);
        List<Node> contentAndImages = RelevantElementsFinder.findAndAddElements(contentNodes,
                mEmptySet, mEmptySet, root);

        assertEquals(1, contentAndImages.size());
        assertEquals(contentText, contentAndImages.get(0));
    }

    public void testImageWithDifferentParent() {
        Node root = TestUtil.createDiv(0);
        Node leftChild = TestUtil.createDiv(1);
        Node rightChild = TestUtil.createDiv(2);
        Node contentText = TestUtil.createText("content");
        Node image = TestUtil.createImage();
        leftChild.appendChild(contentText);
        rightChild.appendChild(image);
        root.appendChild(leftChild);
        root.appendChild(rightChild);

        List<Node> contentNodes = Arrays.<Node>asList(contentText);
        List<Node> contentAndImages = RelevantElementsFinder.findAndAddElements(contentNodes,
                mEmptySet, mEmptySet, root);

        assertEquals(2, contentAndImages.size());
        assertEquals(contentText, contentAndImages.get(0));
        assertEquals(image, contentAndImages.get(1));
    }

    public void testDataTable() {
        Node root = TestUtil.createDiv(0);
        Node contentText = TestUtil.createText("content");
        root.appendChild(contentText);

        Element div = TestUtil.createDiv(1);
        Node table = Document.get().createTableElement();
        String html = "<caption>Testing Data Table</caption>" + // This makes it a data table.
                      "<tbody>" +
                          "<tr>" +
                              "<td>row1col1</td>" +
                              "<td><img src=\"./table.png\"></td>" +
                          "</tr>" +
                      "</tbody>";
        Element.as(table).setInnerHTML(html);
        div.appendChild(table);
        root.appendChild(div);

        // Mark "content" as content.
        List<Node> contentNodes = Arrays.<Node>asList(contentText);
        Set<Node> dataTables = Collections.singleton(table);
        List<Node> contentAndTable = RelevantElementsFinder.findAndAddElements(contentNodes,
                mEmptySet, dataTables, root);

        // Expected nodes: 1 "content" text node, 1 <table> node, and table has 8 child nodes.
        assertEquals(10, contentAndTable.size());
        assertEquals(contentText, contentAndTable.get(0));
        Node n = contentAndTable.get(1);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TABLE", Element.as(n).getNodeName());
        n = contentAndTable.get(2);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("CAPTION", Element.as(n).getNodeName());
        n = contentAndTable.get(3);
        assertEquals("#text", n.getNodeName());
        assertEquals("Testing Data Table", n.getNodeValue());
        n = contentAndTable.get(4);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TBODY", Element.as(n).getNodeName());
        n = contentAndTable.get(5);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TR", Element.as(n).getNodeName());
        n = contentAndTable.get(6);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TD", Element.as(n).getNodeName());
        n = contentAndTable.get(7);
        assertEquals("#text", n.getNodeName());
        assertEquals("row1col1", n.getNodeValue());
        n = contentAndTable.get(8);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TD", Element.as(n).getNodeName());
        n = contentAndTable.get(9);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("IMG", n.getNodeName());
    }

    public void testInvisibleTDInDataTable() {
        Node root = TestUtil.createDiv(0);
        Node contentText = TestUtil.createText("content");
        root.appendChild(contentText);

        Element div = TestUtil.createDiv(1);
        Node table = Document.get().createTableElement();
        String html = "<caption>Testing Data Table</caption>" + // This makes it a data table.
                      "<tbody>" +
                          "<tr>" +
                              "<td style=\"display:none\">row1col1</td>" +
                              "<td>row1col2</td>" +
                          "</tr>" +
                      "</tbody>";
        Element.as(table).setInnerHTML(html);
        div.appendChild(table);
        root.appendChild(div);

        // Get the invisible <td> node.
        NodeList<Element> allTd = Element.as(table).getElementsByTagName("TD");
        assertEquals(2, allTd.getLength());
        Node hiddenTd = allTd.getItem(0);

        // Mark "content" as content.
        List<Node> contentNodes = Arrays.<Node>asList(contentText);
        Set<Node> hiddenElems = Collections.singleton(hiddenTd);
        Set<Node> dataTables = Collections.singleton(table);
        List<Node> contentAndTable = RelevantElementsFinder.findAndAddElements(contentNodes,
                hiddenElems, dataTables, root);

        // Expected nodes: 1 "content" text node, 1 <table> node, and table has 6 child nodes.
        assertEquals(8, contentAndTable.size());
        assertEquals(contentText, contentAndTable.get(0));
        Node n = contentAndTable.get(1);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TABLE", Element.as(n).getNodeName());
        n = contentAndTable.get(6);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TD", Element.as(n).getNodeName());
        n = contentAndTable.get(7);
        assertEquals("#text", n.getNodeName());
        assertEquals("row1col2", n.getNodeValue());
    }

    public void testNonDataTable() {
        Element root = TestUtil.createDiv(0);
        Node contentText = TestUtil.createText("content");
        root.appendChild(contentText);

        Element div = TestUtil.createDiv(1);
        // This is not a data table because there's no <caption> or <thead> or <th> tag.
        String table = "<table>" +
                           "<tbody>" +
                               "<tr>" +
                                   "<td>row1col1</td>" +
                                   "<td>row1col2</td>" +
                               "</tr>" +
                           "</tbody>" +
                       "</table>";
        div.setInnerHTML(table);
        root.appendChild(div);

        // Append some text after table.
        Node afterTable = TestUtil.createText("some text after table");
        root.appendChild(afterTable);
        root.appendChild(TestUtil.createText("some footer text at end of root"));

        // Get the "row1col2" text node.
        NodeList<Element> allTd = div.getElementsByTagName("TD");
        assertEquals(2, allTd.getLength());
        Node td = allTd.getItem(1);
        assertEquals(1, td.getChildCount());
        Node row1col2 = td.getFirstChild();

        // Mark "content" and "row1col2" as content, to emulate real production scenario where
        // boilerpipe identifies some table cells as content.
        List<Node> contentNodes = Arrays.<Node>asList(contentText, row1col2, afterTable);
        List<Node> contentAndTable = RelevantElementsFinder.findAndAddElements(contentNodes,
                mEmptySet, mEmptySet, root);

        assertEquals(3, contentAndTable.size());
        assertEquals(contentText, contentAndTable.get(0));
        assertEquals(row1col2, contentAndTable.get(1));
        assertEquals(afterTable, contentAndTable.get(2));
    }

    public void testNonDataTableWithNestedDataTable() {
        Node root = TestUtil.createDiv(0);
        Node contentText = TestUtil.createText("content");
        root.appendChild(contentText);

        Element div = TestUtil.createDiv(1);
        Node table = Document.get().createTableElement();
        String html = "<tbody>" +
                          "<tr>" +
                              "<td>" +
                                  "<table>" +  // Nested data table.
                                      "<caption>Nested Data Table</caption>" +
                                      "<tbody>" +
                                          "<tr>" +
                                              "<td>row1col1</td>" +
                                          "</tr>" +
                                      "</tbody>" +
                                  "</table>" +
                              "</td>" +
                              "<td>row1col2</td>" +
                          "</tr>" +
                      "</tbody>";
        Element.as(table).setInnerHTML(html);
        div.appendChild(table);
        root.appendChild(div);

        // Mark "content" as content.
        List<Node> contentNodes = Arrays.<Node>asList(contentText);
        List<Node> contentAndTable = RelevantElementsFinder.findAndAddElements(contentNodes,
                mEmptySet, mEmptySet, root);

        // Expected nodes: 1 "content" text node.
        assertEquals(1, contentAndTable.size());
        assertEquals(contentText, contentAndTable.get(0));
    }

    public void testDataTableWithNestedNonDataTable() {
        Node root = TestUtil.createDiv(0);
        Node contentText = TestUtil.createText("content");
        root.appendChild(contentText);

        Element div = TestUtil.createDiv(1);
        Node table = Document.get().createTableElement();
        String html = "<caption>Testing Data Table</caption>" + // This makes it a data table.
                      "<tbody>" +
                          "<tr>" +
                              "<td>" +
                                  "<table>" +  // Nested non-data table.
                                      "<tbody>" +
                                          "<tr>" +
                                              "<td>row1col1</td>" +
                                          "</tr>" +
                                      "</tbody>" +
                                  "</table>" +
                              "</td>" +
                              "<td>row1col2</td>" +
                          "</tr>" +
                      "</tbody>";
        Element.as(table).setInnerHTML(html);
        div.appendChild(table);
        root.appendChild(div);

        // Mark "content" as content.
        List<Node> contentNodes = Arrays.<Node>asList(contentText);
        Set<Node> dataTables = Collections.singleton(table);
        List<Node> contentAndTable = RelevantElementsFinder.findAndAddElements(contentNodes,
                mEmptySet, dataTables, root);

        // Expected nodes: 1 "content" text node, 2 <table> nodes, and a total of 12 child nodes.
        // 2nd table node has
        assertEquals(14, contentAndTable.size());
        assertEquals(contentText, contentAndTable.get(0));
        Node n = contentAndTable.get(1);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TABLE", Element.as(n).getNodeName());
        n = contentAndTable.get(7);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TABLE", n.getNodeName());
        n = contentAndTable.get(10);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TD", n.getNodeName());
        n = contentAndTable.get(11);
        assertEquals("#text", n.getNodeName());
        assertEquals("row1col1", n.getNodeValue());
        n = contentAndTable.get(12);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TD", n.getNodeName());
        n = contentAndTable.get(13);
        assertEquals("#text", n.getNodeName());
        assertEquals("row1col2", n.getNodeValue());
    }
}