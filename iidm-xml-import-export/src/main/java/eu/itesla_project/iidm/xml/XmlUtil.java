/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class XmlUtil {

    private XmlUtil() {
    }

    interface XmlEventHandler {

        void onStartElement() throws XMLStreamException;

    }

    static String readUntilEndElement(String endElementName, XMLStreamReader reader, XmlEventHandler eventHandler) throws XMLStreamException {
        String text = null;
        int event;
        while (!((event = reader.next()) == XMLStreamConstants.END_ELEMENT
                && reader.getLocalName().equals(endElementName))) {
            text = null;
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (eventHandler != null) {
                       eventHandler.onStartElement();
                    }
                    break;

                case XMLStreamConstants.CHARACTERS:
                    text = reader.getText();
                    break;
            }
        }
        return text;
    }

}
