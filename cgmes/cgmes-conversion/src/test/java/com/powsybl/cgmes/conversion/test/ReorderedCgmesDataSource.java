/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Test data source exposing the files of another CGMES data source with the top-level RDF elements of the selected
 * files reordered. The content of the model is unchanged: only the order in which the elements are read differs.
 *
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class ReorderedCgmesDataSource implements ReadOnlyDataSource {

    private final ReadOnlyDataSource delegate;
    private final Predicate<String> reorderedFile;
    private final UnaryOperator<List<Node>> reordering;
    private final AtomicInteger reorderedFiles = new AtomicInteger();

    /**
     * @param delegate the data source to read
     * @param reorderedFile predicate on the file name (or on its suffix and extension) selecting the files to reorder
     * @param reordering function returning the new order of the top-level elements of a file
     */
    public ReorderedCgmesDataSource(ReadOnlyDataSource delegate, Predicate<String> reorderedFile,
                                    UnaryOperator<List<Node>> reordering) {
        this.delegate = Objects.requireNonNull(delegate);
        this.reorderedFile = Objects.requireNonNull(reorderedFile);
        this.reordering = Objects.requireNonNull(reordering);
    }

    /**
     * @return the number of files that have been reordered when read
     */
    public int getReorderedFiles() {
        return reorderedFiles.get();
    }

    @Override
    public String getBaseName() {
        return delegate.getBaseName();
    }

    @Override
    public String getDataExtension() {
        return delegate.getDataExtension();
    }

    @Override
    public boolean exists(String suffix, String ext) throws IOException {
        return delegate.exists(suffix, ext);
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        return delegate.exists(fileName);
    }

    @Override
    public boolean isDataExtension(String ext) {
        return delegate.isDataExtension(ext);
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        InputStream input = delegate.newInputStream(suffix, ext);
        return reorderedFile.test(suffix + "." + ext) ? reorder(input) : input;
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        InputStream input = delegate.newInputStream(fileName);
        return reorderedFile.test(fileName) ? reorder(input) : input;
    }

    @Override
    public Set<String> listNames(String regex) throws IOException {
        return delegate.listNames(regex);
    }

    private InputStream reorder(InputStream input) throws IOException {
        try (input) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document document = documentBuilderFactory.newDocumentBuilder().parse(input);
            Element root = document.getDocumentElement();
            NodeList childNodes = root.getChildNodes();
            List<Node> children = new ArrayList<>(childNodes.getLength());
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    children.add(childNodes.item(i));
                }
            }
            List<Node> reordered = reordering.apply(new ArrayList<>(children));
            children.forEach(root::removeChild);
            reordered.forEach(root::appendChild);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            var transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(new DOMSource(document), new StreamResult(output));
            reorderedFiles.incrementAndGet();
            return new ByteArrayInputStream(output.toByteArray());
        } catch (ParserConfigurationException | SAXException | TransformerException | IllegalArgumentException e) {
            throw new IOException("Unable to reorder CGMES elements", e);
        }
    }
}
