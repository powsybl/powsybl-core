/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model.io;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.nc.model.NcException;
import com.powsybl.nc.model.NcKeyword;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Optional;

/**
 * Reads the NC profile keyword from the model header of an RDF/XML profile.
 * <p>
 * The keyword is looked up by namespace and local name rather than by a literal {@code dcat:keyword}
 * element, so a profile remains readable whichever prefix it binds to the DCAT namespace.
 *
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public final class NcProfileHeaderReader {

    private static final String KEYWORD_ELEMENT = "keyword";
    private static final String FULL_MODEL_ELEMENT = "FullModel";

    private NcProfileHeaderReader() {
    }

    /**
     * Returns the NC keyword declared by the profile, or an empty optional when the profile declares none.
     */
    public static Optional<NcKeyword> readKeyword(InputStream inputStream, String profileName) {
        XMLStreamReader reader = null;
        try {
            reader = XmlUtil.getXMLInputFactory().createXMLStreamReader(inputStream);
            boolean inFullModel = false;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (FULL_MODEL_ELEMENT.equals(reader.getLocalName())
                        && NcConstants.MODEL_DESCRIPTION_NAMESPACE.equals(reader.getNamespaceURI())) {
                        inFullModel = true;
                    } else if (inFullModel
                        && KEYWORD_ELEMENT.equals(reader.getLocalName())
                        && NcConstants.DCAT_NAMESPACE.equals(reader.getNamespaceURI())) {
                        return Optional.of(NcKeyword.fromString(reader.getElementText().trim()));
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT
                    && FULL_MODEL_ELEMENT.equals(reader.getLocalName())
                    && NcConstants.MODEL_DESCRIPTION_NAMESPACE.equals(reader.getNamespaceURI())) {
                    inFullModel = false;
                }
            }
            return Optional.empty();
        } catch (XMLStreamException e) {
            throw new NcException("Cannot parse NC profile " + profileName, e);
        } finally {
            closeQuietly(reader);
        }
    }

    private static void closeQuietly(XMLStreamReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                // The underlying stream is owned and closed by the caller.
            }
            XmlUtil.gcXmlInputFactory(XmlUtil.getXMLInputFactory());
        }
    }
}
