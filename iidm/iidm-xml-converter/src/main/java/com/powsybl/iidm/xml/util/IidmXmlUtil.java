/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.xml.AbstractNetworkXmlContext;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;
import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class IidmXmlUtil {

    public enum ErrorMessage {
        NOT_SUPPORTED("not supported"),
        MANDATORY("mandatory"),
        NOT_NULL_NOT_SUPPORTED("not null and not supported"),
        NOT_DEFAULT_NOT_SUPPORTED("not defined as default and not supported");

        private String message;

        ErrorMessage(String message) {
            this.message = message;
        }
    }

    public static <C extends AbstractNetworkXmlContext> void assertMaximumVersion(String rootElementName, String elementName, ErrorMessage type, IidmXmlVersion maxVersion, C context) {
        if (context.getVersion().compareTo(maxVersion) > 0) {
            throw new PowsyblException(rootElementName + "." + elementName + " is " + type.message + " for IIDM-XML version " + context.getVersion().toString(".") + ". " +
                    "IIDM-XML version should be <= " + maxVersion.toString("."));
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
     * Assert that the context's IIDM-XML version equals or is more recent than a given IIDM-XML version if the value of an attribute or the state of an equipment
     * is not default (interpretable for previous versions).
     * If not, throw an exception with a given type of error message.
     */
    public static <C extends AbstractNetworkXmlContext> void assertMinimumVersionIfNotDefault(boolean valueIsNotDefault, String rootElementName,
                                                                                              String elementName, ErrorMessage type, IidmXmlVersion minVersion,
                                                                                              C context) {
        if (valueIsNotDefault) {
            assertMinimumVersion(rootElementName, elementName, type, minVersion, context);
        }
    }

    /**
     * Assert that the context's IIDM-XML version equals or is more recent than a given IIDM-XML version if the value of an attribute or the state of an equipment
     * is not default (interpretable for previous versions).
     * If not, throw an exception with a given type of error message.
     * If the value is not default and no exception has been thrown, run a given runnable.
     */
    public static <C extends AbstractNetworkXmlContext> void assertMinimumVersionAndRunIfNotDefault(boolean valueIsNotDefault, String rootElementName,
                                                                                                    String elementName, ErrorMessage type, IidmXmlVersion minVersion,
                                                                                                    C context, Runnable runnable) {
        if (valueIsNotDefault) {
            assertMinimumVersion(rootElementName, elementName, type, minVersion, context);
            runnable.run();
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
     * Read a double attribute which is <b>mandatory</b> from a given minimum IIDM-XML version. <br>
     * If the context's IIDM-XML version is strictly older than the given minimum IIDM-XML version, the attribute <b>should not exist</b> (else an exception is thrown).
     * In this case, return Double.NaN <br>
     * If the context's IIDM-XML version equals or is more recent than the given minimum IIDM-XML version, the attribute <b>must exist</b> (else an exception is thrown).
     * In this case, return the read double value.
     */
    public static double readDoubleAttributeFromMinimumVersion(String rootElementName, String attributeName, IidmXmlVersion minVersion, NetworkXmlReaderContext context) {
        return readDoubleAttributeFromMinimumVersion(rootElementName, attributeName, Double.NaN, minVersion, context);
    }

    /**
     * Read a double attribute which is <b>mandatory</b> from a given minimum IIDM-XML version. <br>
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

    /**
     * Read a double attribute which is <b>optional</b> until a given maximum IIDM-XML version. <br>
     * If the context's IIDM-XML version is strictly more recent thant the given maximum IIDM-XML version, the attribute <b>should not exist</b> (else an exception is thrown).
     * In this case, return a given defaultValue. <br>
     * If the context's IIDM-XML version equals or is older than the given maximum IIDM-XML version, the attribute <b>can exist</b>. If it exists, return the read double value.
     * Else, return a given defaultValue.
     */
    public static double readOptionalDoubleAttributeUntilMaximumVersion(String rootElementName, String attributeName, double defaultValue, IidmXmlVersion maxVersion, NetworkXmlReaderContext context) {
        String attributeStr = context.getReader().getAttributeValue(null, attributeName);
        if (attributeStr != null) {
            assertMaximumVersion(rootElementName, attributeName, ErrorMessage.NOT_SUPPORTED, maxVersion, context);
            return Double.valueOf(attributeStr);
        }
        return defaultValue;
    }

    /**
     * Read a double attribute which is <b>optional</b> until a given maximum IIDM-XML version. <br>
     * If the context's IIDM-XML version is strictly more recent thant the given maximum IIDM-XML version, the attribute <b>should not exist</b> (else an exception is thrown).
     * In this case, return a given Double.NaN <br>
     * If the context's IIDM-XML version equals or is older than the given maximum IIDM-XML version, the attribute <b>can exist</b>. If it exists, return the read double value.
     * Else, return Double.NaN
     */
    public static double readOptionalDoubleAttributeUntilMaximumVersion(String rootElementName, String attributeName, IidmXmlVersion maxVersion, NetworkXmlReaderContext context) {
        return readOptionalDoubleAttributeUntilMaximumVersion(rootElementName, attributeName, Double.NaN, maxVersion, context);
    }

    /**
     * Write a <b>mandatory</b> double attribute from a given minimum IIDM-XML version.<br>
     * If the context's IIDM-XML version is strictly older than the given minimum IIDM-XML version, the attribute's value <b>should be default</b>
     * (else an exception is thrown).
     */
    public static void writeDoubleAttributeFromMinimumVersion(String rootElementName, String attributeName, double value, double defaultValue,
                                                              ErrorMessage type, IidmXmlVersion minVersion, NetworkXmlWriterContext context) throws XMLStreamException {
        if (context.getVersion().compareTo(minVersion) >= 0) {
            XmlUtil.writeDouble(attributeName, value, context.getWriter());
        } else {
            assertMinimumVersionIfNotDefault(!Objects.equals(value, defaultValue), rootElementName, attributeName, type, minVersion, context);
        }
    }

    private IidmXmlUtil() {
    }
}
