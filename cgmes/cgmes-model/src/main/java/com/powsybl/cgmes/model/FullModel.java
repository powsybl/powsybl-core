/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.time.ZonedDateTime;
import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class FullModel {

    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(() -> {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    });

    private final String id;

    private final ZonedDateTime scenarioTime;

    private final ZonedDateTime created;

    private final String description;

    private final int version;

    private final List<String> profiles;

    private final List<String> dependentOn;

    private final List<String> supersedes;

    private final String modelingAuthoritySet;

    public FullModel(String id, ZonedDateTime scenarioTime, ZonedDateTime created, String description, int version, List<String> profiles,
                     List<String> dependentOn, List<String> supersedes, String modelingAuthoritySet) {
        this.id = Objects.requireNonNull(id, "ID is missing");
        this.scenarioTime = Objects.requireNonNull(scenarioTime, "Scenario time is missing");
        this.created = Objects.requireNonNull(created, "Created time is missing");
        this.description = description;
        this.version = version;
        if (profiles == null || profiles.isEmpty()) {
            throw new PowsyblException("At least one profile is required");
        }
        this.profiles = new ArrayList<>(profiles);
        this.dependentOn = new ArrayList<>(Objects.requireNonNull(dependentOn));
        this.supersedes = new ArrayList<>(Objects.requireNonNull(supersedes));
        this.modelingAuthoritySet = Objects.requireNonNull(modelingAuthoritySet, "Modeling authority set is missing");
    }

    public String getId() {
        return id;
    }

    public ZonedDateTime getScenarioTime() {
        return scenarioTime;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public int getVersion() {
        return version;
    }

    public List<String> getProfiles() {
        return Collections.unmodifiableList(profiles);
    }

    public List<String> getDependentOn() {
        return Collections.unmodifiableList(dependentOn);
    }

    public List<String> getSupersedes() {
        return Collections.unmodifiableList(supersedes);
    }

    public String getModelingAuthoritySet() {
        return modelingAuthoritySet;
    }

    @Override
    public String toString() {
        return "FullModel(" +
                "id='" + id + '\'' +
                ", scenarioTime=" + scenarioTime +
                ", created=" + created +
                ", description='" + description + '\'' +
                ", version=" + version +
                ", profiles=" + profiles +
                ", dependentOn=" + dependentOn +
                ", supersedes=" + supersedes +
                ", modelingAuthoritySet='" + modelingAuthoritySet + '\'' +
                ')';
    }

    private static class ParsingContext {

        private String id;

        private ZonedDateTime scenarioTime;

        private ZonedDateTime created;

        private String description;

        private Integer version;

        private final List<String> profiles = new ArrayList<>();

        private final List<String> dependentOn = new ArrayList<>();

        private final List<String> supersedes = new ArrayList<>();

        private String modelingAuthoritySet;
    }

    public static FullModel parse(Reader reader) {
        Objects.requireNonNull(reader);
        ParsingContext context = new ParsingContext();
        try {
            XMLStreamReader xmlReader = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(reader);
            try {
                XmlUtil.readUntilStartElement(new String[] {"/", CgmesNames.RDF, CgmesNames.FULL_MODEL}, xmlReader, elementName1 -> {
                    context.id = xmlReader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, CgmesNames.ABOUT);
                    XmlUtil.readSubElements(xmlReader, subElementName -> readSubElement(subElementName, context, xmlReader));
                });
            } finally {
                xmlReader.close();
                XmlUtil.gcXmlInputFactory(XML_INPUT_FACTORY_SUPPLIER.get());
            }
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
        // the other attributes are checked in the constructor
        if (context.version == null) {
            throw new PowsyblException("Version is missing");
        }
        return new FullModel(context.id, context.scenarioTime, context.created, context.description, context.version,
                             context.profiles, context.dependentOn, context.supersedes, context.modelingAuthoritySet);
    }

    private static void readSubElement(String subElementName, ParsingContext context, XMLStreamReader xmlReader) {
        try {
            switch (subElementName) {
                case CgmesNames.SCENARIO_TIME -> context.scenarioTime = ZonedDateTime.parse(xmlReader.getElementText());
                case CgmesNames.CREATED -> context.created = ZonedDateTime.parse(xmlReader.getElementText());
                case CgmesNames.DESCRIPTION -> context.description = XmlUtil.readText(xmlReader);
                case CgmesNames.VERSION -> context.version = Integer.parseInt(xmlReader.getElementText());
                case CgmesNames.PROFILE -> context.profiles.add(XmlUtil.readText(xmlReader));
                case CgmesNames.DEPENDENT_ON -> {
                    context.dependentOn.add(xmlReader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, CgmesNames.RESOURCE));
                    XmlUtil.readEndElementOrThrow(xmlReader);
                }
                case CgmesNames.SUPERSEDES -> {
                    context.supersedes.add(xmlReader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, CgmesNames.RESOURCE));
                    XmlUtil.readEndElementOrThrow(xmlReader);
                }
                case CgmesNames.MODELING_AUTHORITY_SET -> context.modelingAuthoritySet = XmlUtil.readText(xmlReader);
                default -> {
                    // not yet interesting like superseded
                }
            }
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }
}
