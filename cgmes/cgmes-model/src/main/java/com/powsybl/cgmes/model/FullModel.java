/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class FullModel {

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
        this.id = Objects.requireNonNull(id);
        this.scenarioTime = Objects.requireNonNull(scenarioTime);
        this.created = Objects.requireNonNull(created);
        this.description = description;
        this.version = version;
        this.profiles = Objects.requireNonNull(profiles);
        this.dependentOn = Objects.requireNonNull(dependentOn);
        this.supersedes = Objects.requireNonNull(supersedes);
        this.modelingAuthoritySet = Objects.requireNonNull(modelingAuthoritySet);
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
        return profiles;
    }

    public List<String> getDependentOn() {
        return dependentOn;
    }

    public List<String> getSupersedes() {
        return supersedes;
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

        private void validate() {
            if (id == null) {
                throw new PowsyblException("ID is missing");
            }
            if (scenarioTime == null) {
                throw new PowsyblException("Scenario time is missing");
            }
            if (created == null) {
                throw new PowsyblException("Created time is missing");
            }
            if (version == null) {
                throw new PowsyblException("Version is missing");
            }
            if (profiles.isEmpty()) {
                throw new PowsyblException("At least one profile is required");
            }
            if (modelingAuthoritySet == null) {
                throw new PowsyblException("Modeling authority set is missing");
            }
        }
    }

    public static FullModel parse(Reader reader) {
        Objects.requireNonNull(reader);
        ParsingContext context = new ParsingContext();
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
            try {
                XmlUtil.readUntilStartElement(new String[] {"/", CgmesNames.RDF, CgmesNames.FULL_MODEL}, xmlReader, () -> {
                    context.id = xmlReader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, CgmesNames.ABOUT);
                    XmlUtil.readUntilEndElement(CgmesNames.FULL_MODEL, xmlReader, () -> {
                        switch (xmlReader.getLocalName()) {
                            case CgmesNames.SCENARIO_TIME:
                                context.scenarioTime = ZonedDateTime.parse(XmlUtil.readUntilEndElement(CgmesNames.SCENARIO_TIME, xmlReader, () -> { }));
                                break;
                            case CgmesNames.CREATED:
                                context.created = ZonedDateTime.parse(XmlUtil.readUntilEndElement(CgmesNames.CREATED, xmlReader, () -> { }));
                                break;
                            case CgmesNames.DESCRIPTION:
                                context.description = XmlUtil.readUntilEndElement(CgmesNames.DESCRIPTION, xmlReader, () -> { });
                                break;
                            case CgmesNames.VERSION:
                                context.version = Integer.parseInt(XmlUtil.readUntilEndElement(CgmesNames.VERSION, xmlReader, () -> { }));
                                break;
                            case CgmesNames.PROFILE:
                                context.profiles.add(XmlUtil.readUntilEndElement(CgmesNames.PROFILE, xmlReader, () -> { }));
                                break;
                            case CgmesNames.DEPENDENT_ON:
                                context.dependentOn.add(xmlReader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, CgmesNames.RESOURCE));
                                break;
                            case CgmesNames.SUPERSEDES:
                                context.supersedes.add(xmlReader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, CgmesNames.RESOURCE));
                                break;
                            case CgmesNames.MODELING_AUTHORITY_SET:
                                context.modelingAuthoritySet = XmlUtil.readUntilEndElement(CgmesNames.MODELING_AUTHORITY_SET, xmlReader, () -> { });
                                break;
                            default:
                                // not yet interesting like superseded
                                break;
                        }
                    });
                });
            } finally {
                xmlReader.close();
            }
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
        context.validate();
        return new FullModel(context.id, context.scenarioTime, context.created, context.description, context.version,
                             context.profiles, context.dependentOn, context.supersedes, context.modelingAuthoritySet);
    }
}
