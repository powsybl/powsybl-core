/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.xml.AbstractNetworkXmlContext;
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

    /**
     * Assert that the context's IIDM-XML version equals or is more recent than a given IIDM-XML version.
     * If not, throw an exception with a given type of error message.
     */
    public static <C extends AbstractNetworkXmlContext> void assertMinimumVersion(String rootElementName, String elementName, ErrorMessage type, IidmXmlVersion minVersion, C context) {
        if (context.getVersion().compareTo(minVersion) < 0) {
            throw new PowsyblException(rootElementName + "." + elementName + " is " + type.message + " for IIDM-XML version " + context.getVersion().toString(".") + ". " +
                    "IIDM-XML version should be >= " + minVersion.toString("."));
        }
    }

    /**
     * Assert that the context's IIDM-XML version is strictly older than a given IIDM-XML version.
     * If not, throw an exception with a given type of error message.
     */
    public static <C extends AbstractNetworkXmlContext> void assertStrictMaximumVersion(String rootElementName, String elementName, ErrorMessage type, IidmXmlVersion maxVersion, C context) {
        if (context.getVersion().compareTo(maxVersion) >= 0) {
            throw new PowsyblException(rootElementName + "." + elementName + " is " + type.message + " for IIDM-XML version " + context.getVersion().toString(".") + ". " +
                    "IIDM-XML version should be < " + maxVersion.toString("."));
        }
    }

    /**
     * Read an attribute which is <b>mandatory</b> from a given minimum IIDM-XML version. <br>
     * If the context's IIDM-XML version is strictly older than the given minimum IIDM-XML version, the attribute <b>should not exist</b> (else an exception is thrown).
     * In this case, return Double.NaN <br>
     * If the context's IIDM-XML version equals or is more recent than the given minimum IIDM-XML version, the attribute <b>must exist</b> (else an exception is thrown).
     * In this case, return the read double value.
     */
    public static double readDoubleAttributeFromMinimumVersion(String rootElementName, String attributeName, IidmXmlVersion minVersion, NetworkXmlReaderContext context) {
        return readDoubleAttributeFromMinimumVersion(rootElementName, attributeName, Double.NaN, minVersion, context);
    }

    /**
     * Read an attribute which is <b>mandatory</b> from a given minimum IIDM-XML version. <br>
     * If the context's IIDM-XML version is strictly older than the given minimum IIDM-XML version, the attribute <b>should not exist</b> (else an exception is thrown).
     * In this case, return a given defaultValue. <br>
     * If the context's IIDM-XML version equals or is more recent than the given minimum IIDM-XML version, the attribute <b>must exist</b> (else an exception is thrown).
     * In this case, return the read double value.
     */
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
