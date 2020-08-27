/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.xml.AbstractNetworkXmlContext;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class IidmXmlUtil {

    public interface IidmXmlRunnable extends Runnable {

        @Override
        default void run() {
            try {
                runWithXmlStreamException();
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        }

        void runWithXmlStreamException() throws XMLStreamException;
    }

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

    private static PowsyblException createException(String rootElementName, String elementName, ErrorMessage type, IidmXmlVersion version, IidmXmlVersion contextVersion, String reason) {
        return createException(rootElementName + "." + elementName, type, version, contextVersion, reason);
    }

    private static PowsyblException createException(String elementName, ErrorMessage type, IidmXmlVersion version, IidmXmlVersion contextVersion, String reason) {
        return new PowsyblException(elementName + " is " + type.message + " for IIDM-XML version " + contextVersion.toString(".") + ". " + reason + version.toString("."));
    }

    /**
     * Assert that the context's IIDM-XML version equals or is less recent than a given IIDM-XML version.
     * If not, throw an exception with a given type of error message.
     */
    public static <C extends AbstractNetworkXmlContext> void assertMaximumVersion(String rootElementName, String elementName, ErrorMessage type, IidmXmlVersion maxVersion, C context) {
        if (context.getVersion().compareTo(maxVersion) > 0) {
            throw createException(rootElementName, elementName, type, maxVersion, context.getVersion(), "IIDM-XML version should be <= ");
        }
    }

    /**
     * Assert that the context's IIDM-XML version equals or is less recent than a given IIDM-XML version.
     * If not, throw an exception with a given type of error message.
     */
    public static <C extends AbstractNetworkXmlContext> void assertMaximumVersion(String elementName, ErrorMessage type, IidmXmlVersion maxVersion, C context) {
        if (context.getVersion().compareTo(maxVersion) > 0) {
            throw createException(elementName, type, maxVersion, context.getVersion(), "IIDM-XML version should be <= ");
        }
    }

    /**
     * Assert that the context's IIDM-XML version equals or is more recent than a given IIDM-XML version.
     * If not, throw an exception with a given type of error message.
     */
    public static <C extends AbstractNetworkXmlContext> void assertMinimumVersion(String rootElementName, String elementName, ErrorMessage type, IidmXmlVersion minVersion, C context) {
        if (context.getVersion().compareTo(minVersion) < 0) {
            throw createException(rootElementName, elementName, type, minVersion, context.getVersion(), "IIDM-XML version should be >= ");
        }
    }

    /**
     * Assert that the context's IIDM-XML version equals or is more recent than a given IIDM-XML version.
     * If not, throw an exception with a given type of error message.
     */
    public static <C extends AbstractNetworkXmlContext> void assertMinimumVersion(String elementName, ErrorMessage type, IidmXmlVersion minVersion, C context) {
        if (context.getVersion().compareTo(minVersion) < 0) {
            throw createException(elementName, type, minVersion, context.getVersion(), "IIDM-XML version should be >= ");
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
                                                                                                    C context, IidmXmlRunnable runnable) {
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
            throw createException(rootElementName, elementName, type, maxVersion, context.getVersion(), "IIDM-XML version should be < ");
        }
    }

    /**
     * Run a given runnable if the context's IIDM-XML version equals or is more recent than a given minimum IIDM-XML version.
     */
    public static <C extends AbstractNetworkXmlContext> void runFromMinimumVersion(IidmXmlVersion minVersion, C context, IidmXmlRunnable runnable) {
        if (context.getVersion().compareTo(minVersion) >= 0) {
            runnable.run();
        }

    }

    /**
     * Run a given runnable if the context's IIDM-XML version equals or is older than a given maximum IIDM-XML version.
     */
    public static <C extends AbstractNetworkXmlContext> void runUntilMaximumVersion(IidmXmlVersion maxVersion, C context, IidmXmlRunnable runnable) {
        if (context.getVersion().compareTo(maxVersion) <= 0) {
            runnable.run();
        }
    }

    /**
     * Write a <b>mandatory</b> boolean attribute from a given minimum IIDM-XML version.<br>
     * If the context's IIDM-XML version is strictly older than the given minimum IIDM-XML version, the attribute's value <b>should be default</b>
     * (else an exception is thrown).
     */
    public static void writeBooleanAttributeFromMinimumVersion(String rootElementName, String attributeName, boolean value, boolean defaultValue,
                                                               ErrorMessage type, IidmXmlVersion minVersion, NetworkXmlWriterContext context) {
        writeAttributeFromMinimumVersion(rootElementName, attributeName, value != defaultValue, type,
                minVersion, context, () -> context.getWriter().writeAttribute(attributeName, Boolean.toString(value)));
    }

    /**
     * Write a double attribute from a given minimum IIDM-XML version if its value is defined.<br>
     * If the context's IIDM-XML version is strictly older than the given minimum IIDM-XML version, the attribute's value <b>should be undefined i.e. NaN</b>
     * (else an exception is thrown).
     */
    public static void writeDoubleAttributeFromMinimumVersion(String rootElementName, String attributeName, double value,
                                                              ErrorMessage type, IidmXmlVersion minVersion, NetworkXmlWriterContext context) {
        writeDoubleAttributeFromMinimumVersion(rootElementName, attributeName, value, Double.NaN, type, minVersion, context);
    }

    /**
     * Write a double attribute from a given minimum IIDM-XML version if its value is defined.<br>
     * If the context's IIDM-XML version is strictly older than the given minimum IIDM-XML version, the attribute's value <b>should be default</b>
     * (else an exception is thrown).
     */
    public static void writeDoubleAttributeFromMinimumVersion(String rootElementName, String attributeName, double value, double defaultValue,
                                                              ErrorMessage type, IidmXmlVersion minVersion, NetworkXmlWriterContext context) {
        writeAttributeFromMinimumVersion(rootElementName, attributeName, !Objects.equals(value, defaultValue), type,
                minVersion, context, () -> XmlUtil.writeDouble(attributeName, value, context.getWriter()));
    }

    private static void writeAttributeFromMinimumVersion(String rootElementName, String attributeName, boolean isNotDefaultValue,
                                                         ErrorMessage type, IidmXmlVersion minVersion, NetworkXmlWriterContext context,
                                                         IidmXmlRunnable write) {
        if (context.getVersion().compareTo(minVersion) >= 0) {
            write.run();
        } else {
            assertMinimumVersionIfNotDefault(isNotDefaultValue, rootElementName, attributeName, type, minVersion, context);
        }
    }

    /**
     * Write a <b>mandatory</b> int attribute until a given maximum IIDM-XML version. <br>
     * If the context's IIDM-XML version is strictly more recent than the given maximum IIDM-XML version, do nothing.
     */
    public static void writeIntAttributeUntilMaximumVersion(String attributeName, int value, IidmXmlVersion maxVersion, NetworkXmlWriterContext context) throws XMLStreamException {
        if (context.getVersion().compareTo(maxVersion) <= 0) {
            XmlUtil.writeInt(attributeName, value, context.getWriter());
        }
    }

    /**
     * Sort identifiables by their ids.
     */
    public static <T extends Identifiable> Iterable<T> sorted(Iterable<T> identifiables, ExportOptions exportOptions) {
        Objects.requireNonNull(identifiables);
        Objects.requireNonNull(exportOptions);
        return exportOptions.isSorted() ? StreamSupport.stream(identifiables.spliterator(), false)
                .sorted(Comparator.comparing(Identifiable::getId))
                .collect(Collectors.toList())
                : identifiables;
    }

    /**
     * Sort identifiables by their ids.
     */
    public static <T extends Identifiable<T>> Stream<T> sorted(Stream<T> stream, ExportOptions exportOptions) {
        Objects.requireNonNull(stream);
        Objects.requireNonNull(exportOptions);
        return exportOptions.isSorted() ? stream.sorted(Comparator.comparing(Identifiable::getId)) : stream;
    }

    /**
     * Sort extensions by their names.
     */
    public static Iterable<? extends Extension<? extends Identifiable<?>>> sortedExtensions(Iterable<? extends Extension<? extends Identifiable<?>>> extensions, ExportOptions exportOptions) {
        Objects.requireNonNull(exportOptions);
        Objects.requireNonNull(exportOptions);
        return exportOptions.isSorted() ? StreamSupport.stream(extensions.spliterator(), false)
                                                       .sorted(Comparator.comparing(Extension::getName))
                                                       .collect(Collectors.toList())
                : extensions;
    }

    /**
     * Sort temporary limits by their names.
     */
    public static Iterable<CurrentLimits.TemporaryLimit> sortedTemporaryLimits(Iterable<CurrentLimits.TemporaryLimit> temporaryLimits, ExportOptions exportOptions) {
        Objects.requireNonNull(temporaryLimits);
        Objects.requireNonNull(exportOptions);
        return exportOptions.isSorted() ? StreamSupport.stream(temporaryLimits.spliterator(), false)
                                                       .sorted(Comparator.comparing(CurrentLimits.TemporaryLimit::getName))
                                                       .collect(Collectors.toList())
                                        : temporaryLimits;
    }

    /**
     * Sort internal connections first by their side one node value then by their side 2 node value.
     */
    public static Iterable<VoltageLevel.NodeBreakerView.InternalConnection> sortedInternalConnections(Iterable<VoltageLevel.NodeBreakerView.InternalConnection> internalConnections, ExportOptions exportOptions) {
        Objects.requireNonNull(internalConnections);
        Objects.requireNonNull(exportOptions);
        return exportOptions.isSorted() ? StreamSupport.stream(internalConnections.spliterator(), false)
                                                       .sorted(Comparator.comparing(VoltageLevel.NodeBreakerView.InternalConnection::getNode1)
                                                                         .thenComparing(VoltageLevel.NodeBreakerView.InternalConnection::getNode2))
                                                       .collect(Collectors.toList())
                : internalConnections;
    }

    private IidmXmlUtil() {
    }
}
