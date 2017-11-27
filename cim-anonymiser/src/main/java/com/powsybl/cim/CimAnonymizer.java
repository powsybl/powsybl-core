/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cim;

import com.google.common.collect.ImmutableSet;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.util.StringAnonymizer;
import javanet.staxutils.helpers.EventWriterDelegate;
import net.java.truevfs.comp.zip.ZipEntry;
import net.java.truevfs.comp.zip.ZipFile;
import net.java.truevfs.comp.zip.ZipOutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CimAnonymizer {

    public interface Logger {

        void logAnonymizingFile(Path file);

        void logSkipped(Set<String> skipped);
    }

    public static class DefaultLogger implements Logger {

        public void logAnonymizingFile(Path file) {
            // empty default implementation
        }

        public void logSkipped(Set<String> skipped) {
            // empty default implementation
        }
    }

    private static final Set<String> NAMES_TO_EXCLUDE = ImmutableSet.of("PATL", "TATL");

    private static final Set<String> DESCRIPTIONS_TO_EXCLUDE = ImmutableSet.of();

    private static final String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    private static final String CIM_URI_PATTERN = "http://iec.ch/TC57/20\\d\\d/CIM-schema-cim\\d\\d";

    private static final QName RDF_ID = new QName(RDF_URI, "ID");
    private static final QName RDF_RESOURCE = new QName(RDF_URI, "resource");
    private static final QName RDF_ABOUT = new QName(RDF_URI, "about");

    private static class XmlStaxContext {
        private final XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        private final XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
        private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    }

    private final XmlStaxContext xmlStaxContext = new XmlStaxContext();

    private static class XmlAnonymizer extends EventWriterDelegate {

        private final XmlStaxContext xmlStaxContext;
        private final StringAnonymizer dictionary;
        private final Set<String> rdfIdValues;
        private final Set<String> skipped;

        private boolean identifiedObjectName = false;
        private boolean identifiedObjectDescription = false;

        public XmlAnonymizer(XMLEventWriter out, XmlStaxContext xmlStaxContext, StringAnonymizer dictionary, Set<String> rdfIdValues, Set<String> skipped) {
            super(out);
            this.xmlStaxContext = Objects.requireNonNull(xmlStaxContext);
            this.dictionary = Objects.requireNonNull(dictionary);
            this.rdfIdValues = rdfIdValues;
            this.skipped = Objects.requireNonNull(skipped);
        }

        private static XMLEvent anonymizeCharacters(Characters characters, Set<String> exclude, Set<String> skipped, XMLEventFactory eventFactory,
                                                    StringAnonymizer dictionary) {
            if (exclude.contains(characters.getData())) {
                skipped.add(characters.getData());
                return null;
            } else {
                return eventFactory.createCharacters(dictionary.anonymize(characters.getData()));
            }
        }

        private static final class AttributeValue {

            private final String nsUri;
            private final String value;

            private AttributeValue(String nsUri, String value) {
                this.nsUri = nsUri;
                this.value = Objects.requireNonNull(value);
            }

            private static AttributeValue parseValue(Attribute attribute) {
                int hashTagPos = attribute.getValue().indexOf('#');
                String nsUri;
                String value;
                if (hashTagPos != -1) {
                    nsUri = attribute.getValue().substring(0, hashTagPos);
                    value = attribute.getValue().substring(hashTagPos + 1);
                } else {
                    nsUri = null;
                    value = attribute.getValue();
                }
                return new AttributeValue(nsUri, value);
            }

            private String getNsUri() {
                return nsUri;
            }

            private String get() {
                return value;
            }

            private String toString(StringAnonymizer dictionary) {
                return (nsUri != null ? nsUri : "") + "#" + dictionary.anonymize(value);
            }
        }

        private Attribute anonymizeAttribute(Attribute attribute) {
            if (attribute.getName().equals(RDF_ID)) {
                return xmlStaxContext.eventFactory.createAttribute(attribute.getName(), dictionary.anonymize(attribute.getValue()));
            } else if (attribute.getName().equals(RDF_RESOURCE) || attribute.getName().equals(RDF_ABOUT)) {
                // skip outside graph rdf:ID references
                AttributeValue value  = AttributeValue.parseValue(attribute);
                if ((value.getNsUri() == null || !value.getNsUri().matches(CIM_URI_PATTERN)) &&
                        (rdfIdValues == null || rdfIdValues.contains(value.get()))) {
                    return xmlStaxContext.eventFactory.createAttribute(attribute.getName(), value.toString(dictionary));
                } else {
                    skipped.add(attribute.getValue());
                    return null;
                }
            } else {
                throw new AssertionError("Unknown attribute " + attribute.getName());
            }
        }

        private XMLEvent anonymizeStartElement(StartElement startElement) {
            if (startElement.getName().getLocalPart().equals("IdentifiedObject.name")) {
                identifiedObjectName = true;
            } else if (startElement.getName().getLocalPart().equals("IdentifiedObject.description")) {
                identifiedObjectDescription = true;
            } else {
                Iterator it = startElement.getAttributes();
                if (it.hasNext()) {
                    List<Attribute> newAttributes = new ArrayList<>();
                    while (it.hasNext()) {
                        Attribute attribute = (Attribute) it.next();
                        Attribute newAttribute = anonymizeAttribute(attribute);
                        newAttributes.add(newAttribute != null ? newAttribute : attribute);
                    }
                    return xmlStaxContext.eventFactory.createStartElement(startElement.getName(),
                                                                          newAttributes.iterator(),
                                                                          startElement.getNamespaces());
                }
            }
            return null;
        }

        private XMLEvent anonymizeCharacters(Characters characters) {
            if (identifiedObjectName) {
                identifiedObjectName = false;
                return anonymizeCharacters(characters, NAMES_TO_EXCLUDE, skipped, xmlStaxContext.eventFactory, dictionary);
            } else if (identifiedObjectDescription) {
                identifiedObjectDescription = false;
                return anonymizeCharacters(characters, DESCRIPTIONS_TO_EXCLUDE, skipped, xmlStaxContext.eventFactory, dictionary);
            }
            return null;
        }

        @Override
        public void add(XMLEvent event) throws XMLStreamException {
            XMLEvent newEvent = null;

            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                newEvent = anonymizeStartElement(startElement);
            } else if (event.isCharacters()) {
                Characters characters = event.asCharacters();
                newEvent = anonymizeCharacters(characters);
            }

            super.add(newEvent != null ? newEvent : event);
        }
    }

    private static void anonymizeFile(InputStream cimFileInputStream, OutputStream anonymizedCimFileOutputStream, XmlStaxContext xmlStaxContext,
                                      StringAnonymizer dictionary, Set<String> rdfIdValues, Set<String> skipped) {
        try {
            XMLEventReader eventReader = xmlStaxContext.inputFactory.createXMLEventReader(cimFileInputStream);
            XMLEventWriter eventWriter = new XmlAnonymizer(xmlStaxContext.outputFactory.createXMLEventWriter(anonymizedCimFileOutputStream),
                    xmlStaxContext, dictionary, rdfIdValues, skipped);
            eventWriter.add(eventReader);
            eventWriter.close();
            eventReader.close();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static StringAnonymizer loadDic(Path dictionaryFile) {
        StringAnonymizer dictionary = new StringAnonymizer();
        // load previous dictionary
        if (dictionaryFile != null && Files.exists(dictionaryFile)) {
            try (BufferedReader reader = Files.newBufferedReader(dictionaryFile, StandardCharsets.UTF_8)) {
                dictionary.readCsv(reader);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return dictionary;
    }

    private void saveDic(StringAnonymizer dictionary, Path dictionaryFile) {
        // save updated dictionary
        try (BufferedWriter writer = Files.newBufferedWriter(dictionaryFile, StandardCharsets.UTF_8)) {
            dictionary.writeCsv(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void addRdfIdValues(InputStream is, Set<String> rdfIdValues) throws XMLStreamException {
        // memoize RDF ID values of the document
        XMLEventReader eventReader = xmlStaxContext.inputFactory.createXMLEventReader(is);
        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                Iterator it = startElement.getAttributes();
                while (it.hasNext()) {
                    Attribute attribute = (Attribute) it.next();
                    QName name = attribute.getName();
                    if (RDF_ID.equals(name)) {
                        rdfIdValues.add(attribute.getValue());
                    }
                }
            }
        }
        eventReader.close();
    }

    private Set<String> getRdfIdValues(ZipFile zipFileData) {
        Set<String> rdfIdValues = new HashSet<>();

        // memoize rdf:ID values, will be used to detect outside graph references
        for (ZipEntry entry : zipFileData) {
            try (InputStream is = zipFileData.getInputStream(entry.getName())) {
                addRdfIdValues(is, rdfIdValues);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        }

        return rdfIdValues;
    }

    public void anonymizeZip(Path cimZipFile, Path anonymizedCimFileDir, Path dictionaryFile, Logger logger, boolean skipExternalRef) {
        Objects.requireNonNull(cimZipFile);
        Objects.requireNonNull(anonymizedCimFileDir);
        Objects.requireNonNull(dictionaryFile);
        Objects.requireNonNull(logger);

        logger.logAnonymizingFile(cimZipFile);

        if (!Files.isDirectory(anonymizedCimFileDir)) {
            throw new PowsyblException(anonymizedCimFileDir + " has to be a directory");
        }

        // load dictionary
        StringAnonymizer dictionary = loadDic(dictionaryFile);

        // anonymize each file of the archive
        try (ZipFile zipFileData = new ZipFile(cimZipFile)) {

            Set<String> rdfIdValues = skipExternalRef ? getRdfIdValues(zipFileData) : null;

            Set<String> skipped = new HashSet<>();

            Path anonymizedCimZipFile = anonymizedCimFileDir.resolve(cimZipFile.getFileName());
            try (ZipOutputStream anonymizedZipOutputStream = new ZipOutputStream(Files.newOutputStream(anonymizedCimZipFile))) {
                for (ZipEntry entry : zipFileData) {
                    anonymizedZipOutputStream.putNextEntry(entry);
                    try (InputStream cimFileInputStream = zipFileData.getInputStream(entry.getName())) {
                        anonymizeFile(cimFileInputStream, anonymizedZipOutputStream, xmlStaxContext, dictionary, rdfIdValues, skipped);
                    }
                    anonymizedZipOutputStream.closeEntry();
                }
            }

            logger.logSkipped(skipped);

            saveDic(dictionary, dictionaryFile);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
