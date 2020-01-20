/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class IidmXmlUtil {

    public enum ErrorMessage {
        NOT_SUPPORTED("not supported"),
        MANDATORY("mandatory");

        private String message;

        ErrorMessage(String message) {
            this.message = message;
        }
    }

    public static void assertMinimumVersion(String rootElementName, String elementName, ErrorMessage type, IidmXmlVersion minVersion, NetworkXmlReaderContext context) {
        if (context.getVersion().compareTo(minVersion) < 0) {
            throw new PowsyblException(rootElementName + "." + elementName + " is " + type.message + " for IIDM-XML version " + context.getVersion().toString(".") + ". " +
                    "IIDM-XML version should be >= " + minVersion.toString("."));
        }
    }

    public static void assertStrictMaximumVersion(String rootElementName, String elementName, ErrorMessage type, IidmXmlVersion maxVersion, NetworkXmlReaderContext context) {
        if (context.getVersion().compareTo(maxVersion) >= 0) {
            throw new PowsyblException(rootElementName + "." + elementName + " is " + type.message + " for IIDM-XML version " + context.getVersion().toString(".") + ". " +
                    "IIDM-XML version should be < " + maxVersion.toString("."));
        }
    }

    public static double readDoubleAttributeFromMinimumVersion(String rootElementName, String attributeName, IidmXmlVersion minVersion, NetworkXmlReaderContext context) {
        return readDoubleAttributeFromMinimumVersion(rootElementName, attributeName, Double.NaN, minVersion, context);
    }

    public static double readDoubleAttributeFromMinimumVersion(String rootElementName, String attributeName, double defaultValue, IidmXmlVersion minVersion, NetworkXmlReaderContext context) {
        String attributeStr = context.getReader().getAttributeValue(null, attributeName);
        if (attributeStr != null) {
            assertMinimumVersion(rootElementName, attributeName, ErrorMessage.NOT_SUPPORTED, minVersion, context);
        } else {
            assertStrictMaximumVersion(rootElementName, attributeName, ErrorMessage.MANDATORY, minVersion, context);
        }
        return attributeStr == null ? defaultValue : Double.valueOf(attributeStr);
    }

    private IidmXmlUtil() {
    }
}
