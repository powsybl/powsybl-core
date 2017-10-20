/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import com.google.common.collect.ImmutableMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TsoOverloadSecurityIndex extends AbstractSecurityIndex {

    static final String XML_NAME = "tso-overload";

    private final int overloadCount;

    private final List<String> overloadedBranches;

    private final boolean computationSucceed;

    public static TsoOverloadSecurityIndex fromXml(String contingencyId, XMLStreamReader xmlsr) throws XMLStreamException {
        String text = null;
        int overloadCount = -1;
        List<String> overloadedBranches = new ArrayList<>();
        boolean computationSucceed = true;
        while (xmlsr.hasNext()) {
            int eventType = xmlsr.next();
            switch (eventType) {
                case XMLEvent.CHARACTERS:
                    text = xmlsr.getText();
                    break;
                case XMLEvent.END_ELEMENT:
                    switch (xmlsr.getLocalName()) {
                        case "overload-count":
                            overloadCount = Integer.parseInt(text);
                            break;

                        case "overloaded-branch":
                            overloadedBranches.add(text);
                            break;

                        case "computation-succeed":
                            computationSucceed = Boolean.parseBoolean(text);
                            break;

                        case "index":
                            return new TsoOverloadSecurityIndex(contingencyId, overloadCount, overloadedBranches, computationSucceed);

                        default:
                            break;
                    }
                    break;

                default:
                    break;
            }
        }
        throw new AssertionError("index element not found");
    }

    public TsoOverloadSecurityIndex(String contingencyId, int overloadCount) {
        this(contingencyId, overloadCount, Collections.emptyList(), true);
    }

    public TsoOverloadSecurityIndex(String contingencyId, int overloadCount, List<String> overloadedBranches) {
        this(contingencyId, overloadCount, overloadedBranches, true);
    }

    public TsoOverloadSecurityIndex(String contingencyId, int overloadCount, List<String> overloadedBranches, boolean computationSucceed) {
        super(contingencyId, SecurityIndexType.TSO_OVERLOAD);
        this.overloadCount = overloadCount;
        this.overloadedBranches = overloadedBranches;
        this.computationSucceed = computationSucceed;
    }

    public boolean isComputationSucceed() {
        return computationSucceed;
    }

    public int getOverloadCount() {
        return overloadCount;
    }

    @Override
    public boolean isOk() {
        return computationSucceed && overloadCount == 0;
    }

    @Override
    public void toXml(XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartDocument();
        xmlWriter.writeStartElement("index");
        xmlWriter.writeAttribute("name", XML_NAME);

        xmlWriter.writeStartElement("computation-succeed");
        xmlWriter.writeCharacters(Boolean.toString(computationSucceed));
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("overload-count");
        xmlWriter.writeCharacters(Integer.toString(overloadCount));
        xmlWriter.writeEndElement();

        for (String overloadedBranch : overloadedBranches) {
            xmlWriter.writeStartElement("overloaded-branch");
            xmlWriter.writeCharacters(overloadedBranch);
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of("computationSucceed", Boolean.toString(computationSucceed),
                               "overloadCount", Integer.toString(overloadCount),
                               "overloadedBranches", overloadedBranches.toString());
    }

}
