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
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TsoFrequencySecurityIndex extends AbstractSecurityIndex {

    static final String XML_NAME = "tso-frequency";

    private static final int MAX_FREQ_OUT_COUNT = 10;

    private final int freqOutCount;

    public static TsoFrequencySecurityIndex fromXml(String contingencyId, XMLStreamReader xmlsr) throws XMLStreamException {
        String text = null;
        while (xmlsr.hasNext()) {
            int eventType = xmlsr.next();
            switch (eventType) {
                case XMLEvent.CHARACTERS:
                    text = xmlsr.getText();
                    break;
                case XMLEvent.END_ELEMENT:
                    if ("freq-out-count".equals(xmlsr.getLocalName())) {
                        return new TsoFrequencySecurityIndex(contingencyId, Integer.parseInt(text));
                    }
                    break;
                default:
                    break;
            }
        }
        throw new AssertionError("freq-out-count element not found");
    }

    public TsoFrequencySecurityIndex(String contingencyId, int freqOutCount) {
        super(contingencyId, SecurityIndexType.TSO_FREQUENCY);
        this.freqOutCount = freqOutCount;
    }

    public int getFreqOutCount() {
        return freqOutCount;
    }

    @Override
    public boolean isOk() {
        return freqOutCount < MAX_FREQ_OUT_COUNT;
    }

    @Override
    public void toXml(XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartDocument();
        xmlWriter.writeStartElement("index");
        xmlWriter.writeAttribute("name", XML_NAME);
        xmlWriter.writeStartElement("freq-out-count");
        xmlWriter.writeCharacters(Integer.toString(freqOutCount));
        xmlWriter.writeEndElement();
        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of("freqOutCount", Integer.toString(freqOutCount));
    }

}
