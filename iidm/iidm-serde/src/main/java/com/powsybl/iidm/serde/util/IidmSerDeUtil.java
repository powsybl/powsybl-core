/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.serde.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class IidmSerDeUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(IidmSerDeUtil.class);

    private static final String MAXIMUM_REASON = "IIDM version should be <= ";
    private static final String MINIMUM_REASON = "IIDM version should be >= ";

    public enum ErrorMessage {
        NOT_SUPPORTED("not supported"),
        MANDATORY("mandatory"),
        NOT_NULL_NOT_SUPPORTED("not null and not supported"),
        NOT_DEFAULT_NOT_SUPPORTED("not defined as default and not supported");

        private final String message;

        ErrorMessage(String message) {
            this.message = message;
        }
    }

    private static String message(String elementName, ErrorMessage type, IidmVersion version, IidmVersion contextVersion, String reason) {
        return elementName + " is " + type.message + " for IIDM version " + contextVersion.toString(".") + ". " + reason + version.toString(".");
    }

    private static PowsyblException createException(String rootElementName, String elementName, ErrorMessage type, IidmVersion version, IidmVersion contextVersion, String reason) {
        return createException(rootElementName + "." + elementName, type, version, contextVersion, reason);
    }

    private static PowsyblException createException(String elementName, ErrorMessage type, IidmVersion version, IidmVersion contextVersion, String reason) {
        return new PowsyblException(message(elementName, type, version, contextVersion, reason));
    }

    private static void createExceptionOrLogError(String rootElementName, String elementName, ErrorMessage type, IidmVersion refVersion, String reason, NetworkSerializerContext context) {
        createExceptionOrLogError(rootElementName + "." + elementName, type, refVersion, reason, context);
    }

    private static void createExceptionOrLogError(String elementName, ErrorMessage type, IidmVersion refVersion, String reason, NetworkSerializerContext context) {
        if (context.getOptions().getIidmVersionIncompatibilityBehavior() == ExportOptions.IidmVersionIncompatibilityBehavior.THROW_EXCEPTION) {
            throw createException(elementName, type, refVersion, context.getVersion(), reason);
        } else if (context.getOptions().getIidmVersionIncompatibilityBehavior() == ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message(elementName, type, refVersion, context.getVersion(), reason));
            }
        } else {
            throw new IllegalStateException("Unexpected behaviour: " + context.getOptions().getIidmVersionIncompatibilityBehavior());
        }
    }

    /**
     * Assert that the reader context's IIDM version equals or is less recent than a given IIDM version.
     * If not, throw an exception with a given type of error message.
     */
    public static void assertMaximumVersion(String rootElementName, String elementName, ErrorMessage type, IidmVersion maxVersion, NetworkDeserializerContext context) {
        if (context.getVersion().compareTo(maxVersion) > 0) {
            throw createException(rootElementName, elementName, type, maxVersion, context.getVersion(), MAXIMUM_REASON);
        }
    }

    /**
     * Assert that the writer context's IIDM version equals or is less recent than a given IIDM version.
     * If not, throw an exception or log an error with a given type of error message,
     * depending of {@link ExportOptions.IidmVersionIncompatibilityBehavior} found in the {@link ExportOptions} of the given context.
     */
    public static void assertMaximumVersion(String rootElementName, String elementName, ErrorMessage type, IidmVersion maxVersion, NetworkSerializerContext context) {
        if (context.getVersion().compareTo(maxVersion) > 0) {
            createExceptionOrLogError(rootElementName, elementName, type, maxVersion, MAXIMUM_REASON, context);
        }
    }

    /**
     * Assert that the reader context's IIDM version equals or is less recent than a given IIDM version.
     * If not, throw an exception with a given type of error message.
     */
    public static void assertMaximumVersion(String elementName, ErrorMessage type, IidmVersion maxVersion, NetworkDeserializerContext context) {
        if (context.getVersion().compareTo(maxVersion) > 0) {
            throw createException(elementName, type, maxVersion, context.getVersion(), MAXIMUM_REASON);
        }
    }

    /**
     * Assert that the writer context's IIDM version equals or is less recent than a given IIDM version.
     * If not, throw an exception or log an error with a given type of error message,
     * depending of {@link ExportOptions.IidmVersionIncompatibilityBehavior} found in the {@link ExportOptions} of the given context.
     */
    public static void assertMaximumVersion(String elementName, ErrorMessage type, IidmVersion maxVersion, NetworkSerializerContext context) {
        if (context.getVersion().compareTo(maxVersion) > 0) {
            createExceptionOrLogError(elementName, type, maxVersion, MAXIMUM_REASON, context);
        }
    }

    /**
     * Assert that the reader context's IIDM version equals or is more recent than a given IIDM version.
     * If not, throw an exception with a given type of error message.
     */
    public static void assertMinimumVersion(String rootElementName, String elementName, ErrorMessage type, IidmVersion minVersion, NetworkDeserializerContext context) {
        if (context.getVersion().compareTo(minVersion) < 0) {
            throw createException(rootElementName, elementName, type, minVersion, context.getVersion(), MINIMUM_REASON);
        }
    }

    /**
     * Assert that the reader context's IIDM version equals or is more recent than a given IIDM version.
     * If not, throw an exception with a given type of error message.
     */
    public static void assertMinimumVersion(String rootElementName, String elementName, ErrorMessage type, IidmVersion minVersion, IidmVersion version) {
        if (version.compareTo(minVersion) < 0) {
            throw createException(rootElementName, elementName, type, minVersion, version, MINIMUM_REASON);
        }
    }

    /**
     * Assert that the writer context's IIDM version equals or is more recent than a given IIDM version.
     * If not, throw an exception or log an error with a given type of error message,
     * depending of {@link ExportOptions.IidmVersionIncompatibilityBehavior} found in the {@link ExportOptions} of the given context.
     */
    public static void assertMinimumVersion(String rootElementName, String elementName, ErrorMessage type, IidmVersion minVersion, NetworkSerializerContext context) {
        if (context.getVersion().compareTo(minVersion) < 0) {
            createExceptionOrLogError(rootElementName, elementName, type, minVersion, MINIMUM_REASON, context);
        }
    }

    /**
     * Assert that the reader context's IIDM version equals or is more recent than a given IIDM version.
     * If not, throw an exception with a given type of error message.
     */
    public static void assertMinimumVersion(String elementName, ErrorMessage type, IidmVersion minVersion, NetworkDeserializerContext context) {
        if (context.getVersion().compareTo(minVersion) < 0) {
            throw createException(elementName, type, minVersion, context.getVersion(), MINIMUM_REASON);
        }
    }

    /**
     * Assert that the writer context's IIDM version equals or is more recent than a given IIDM version.
     * If not, throw an exception or log an error with a given type of error message,
     * depending of {@link ExportOptions.IidmVersionIncompatibilityBehavior} found in the {@link ExportOptions} of the given context.
     */
    public static void assertMinimumVersion(String elementName, ErrorMessage type, IidmVersion minVersion, NetworkSerializerContext context) {
        if (context.getVersion().compareTo(minVersion) < 0) {
            createExceptionOrLogError(elementName, type, minVersion, MINIMUM_REASON, context);
        }
    }

    /**
     * Assert that the reader context's IIDM version equals or is more recent than a given IIDM version if the value of an attribute or the state of an equipment
     * is not default (interpretable for previous versions).
     * If not, throw an exception with a given type of error message.
     */
    public static void assertMinimumVersionIfNotDefault(boolean valueIsNotDefault, String rootElementName,
                                                        String elementName, ErrorMessage type, IidmVersion minVersion,
                                                        IidmVersion version) {
        if (valueIsNotDefault) {
            assertMinimumVersion(rootElementName, elementName, type, minVersion, version);
        }
    }

    /**
     * Assert that the reader context's IIDM version equals or is more recent than a given IIDM version if the value of an attribute or the state of an equipment
     * is not default (interpretable for previous versions).
     * If not, throw an exception with a given type of error message.
     */
    public static void assertMinimumVersionIfNotDefault(boolean valueIsNotDefault, String rootElementName,
                                                        String elementName, ErrorMessage type, IidmVersion minVersion,
                                                        NetworkDeserializerContext context) {
        if (valueIsNotDefault) {
            assertMinimumVersion(rootElementName, elementName, type, minVersion, context);
        }
    }

    /**
     * Assert that the writer context's IIDM version equals or is more recent than a given IIDM version if the value of an attribute or the state of an equipment
     * is not default (interpretable for previous versions).
     * If not, throw an exception or log an error with a given type of error message,
     * depending of {@link ExportOptions.IidmVersionIncompatibilityBehavior} found in the {@link ExportOptions} of the given context.
     */
    public static void assertMinimumVersionIfNotDefault(boolean valueIsNotDefault, String rootElementName,
                                                        String elementName, ErrorMessage type, IidmVersion minVersion,
                                                        NetworkSerializerContext context) {
        if (valueIsNotDefault) {
            assertMinimumVersion(rootElementName, elementName, type, minVersion, context);
        }
    }

    /**
     * Assert that the reader context's IIDM version equals or is more recent than a given IIDM version if the value of an attribute or the state of an equipment
     * is not default (interpretable for previous versions).
     * If not, throw an exception with a given type of error message.
     * If the value is not default and no exception has been thrown, run a given runnable.
     */
    public static void assertMinimumVersionAndRunIfNotDefault(boolean valueIsNotDefault, String rootElementName,
                                                              String elementName, ErrorMessage type, IidmVersion minVersion,
                                                              NetworkDeserializerContext context, Runnable runnable) {
        if (valueIsNotDefault) {
            assertMinimumVersion(rootElementName, elementName, type, minVersion, context);
            runnable.run();
        }
    }

    /**
     * Assert that the writer context's IIDM version equals or is more recent than a given IIDM version if the value of an attribute or the state of an equipment
     * is not default (interpretable for previous versions).
     * If not, throw an exception or log an error with a given type of error message,
     * depending of {@link ExportOptions.IidmVersionIncompatibilityBehavior} found in the {@link ExportOptions} of the given context.
     * If the value is not default and the version is compatible, run a given runnable.
     */
    public static void assertMinimumVersionAndRunIfNotDefault(boolean valueIsNotDefault, String rootElementName,
                                                              String elementName, ErrorMessage type, IidmVersion minVersion,
                                                              NetworkSerializerContext context, Runnable runnable) {
        if (valueIsNotDefault) {
            assertMinimumVersion(rootElementName, elementName, type, minVersion, context);
            runFromMinimumVersion(minVersion, context, runnable::run);
        }
    }

    /**
     * Assert that the reader context's IIDM version is strictly older than a given IIDM version.
     * If not, throw an exception with a given type of error message.
     */
    public static void assertStrictMaximumVersion(String rootElementName, String elementName, ErrorMessage type, IidmVersion maxVersion, NetworkDeserializerContext context) {
        if (context.getVersion().compareTo(maxVersion) >= 0) {
            throw createException(rootElementName, elementName, type, maxVersion, context.getVersion(), "IIDM version should be < ");
        }
    }

    /**
     * Assert that the writer context's IIDM version is strictly older than a given IIDM version.
     * If not, throw an exception or log an error with a given type of error message,
     * depending of {@link ExportOptions.IidmVersionIncompatibilityBehavior} found in the {@link ExportOptions} of the given context.
     */
    public static void assertStrictMaximumVersion(String rootElementName, String elementName, ErrorMessage type, IidmVersion maxVersion, NetworkSerializerContext context) {
        if (context.getVersion().compareTo(maxVersion) >= 0) {
            createExceptionOrLogError(rootElementName, elementName, type, maxVersion, "IIDM version should be < ", context);
        }
    }

    /**
     * Run a given runnable if the context's IIDM version equals or is more recent than a given minimum IIDM version.
     */
    public static <C extends AbstractNetworkSerDeContext> void runFromMinimumVersion(IidmVersion minVersion, C context, Runnable runnable) {
        if (context.getVersion().compareTo(minVersion) >= 0) {
            runnable.run();
        }

    }

    /**
     * Run a given runnable if the context's IIDM version equals or is older than a given maximum IIDM version.
     */
    public static <C extends AbstractNetworkSerDeContext> void runUntilMaximumVersion(IidmVersion maxVersion, C context, Runnable runnable) {
        if (context.getVersion().compareTo(maxVersion) <= 0) {
            runnable.run();
        }
    }

    /**
     * Write a <b>mandatory</b> boolean attribute from a given minimum IIDM version.<br>
     * If the context's IIDM version is strictly older than the given minimum IIDM version, the attribute's value <b>should be default</b>
     * (else an exception is thrown).
     */
    public static void writeBooleanAttributeFromMinimumVersion(String rootElementName, String attributeName, boolean value, boolean defaultValue,
                                                               ErrorMessage type, IidmVersion minVersion, NetworkSerializerContext context) {
        writeAttributeFromMinimumVersion(rootElementName, attributeName, value != defaultValue, type,
                minVersion, context, () -> context.getWriter().writeBooleanAttribute(attributeName, value));
    }

    /**
     * Write a double attribute from a given minimum IIDM version if its value is defined.<br>
     * If the context's IIDM version is strictly older than the given minimum IIDM version, the attribute's value <b>should be undefined i.e. NaN</b>
     * (else an exception is thrown).
     */
    public static void writeDoubleAttributeFromMinimumVersion(String rootElementName, String attributeName, double value,
                                                              ErrorMessage type, IidmVersion minVersion, NetworkSerializerContext context) {
        writeDoubleAttributeFromMinimumVersion(rootElementName, attributeName, value, Double.NaN, type, minVersion, context);
    }

    /**
     * Write a double attribute from a given minimum IIDM version if its value is defined.<br>
     * If the context's IIDM version is strictly older than the given minimum IIDM version, the attribute's value <b>should be default</b>
     * (else an exception is thrown).
     */
    public static void writeDoubleAttributeFromMinimumVersion(String rootElementName, String attributeName, double value, double defaultValue,
                                                              ErrorMessage type, IidmVersion minVersion, NetworkSerializerContext context) {
        writeAttributeFromMinimumVersion(rootElementName, attributeName, !Objects.equals(value, defaultValue), type,
                minVersion, context, () -> context.getWriter().writeDoubleAttribute(attributeName, value));
    }

    private static void writeAttributeFromMinimumVersion(String rootElementName, String attributeName, boolean isNotDefaultValue,
                                                         ErrorMessage type, IidmVersion minVersion, NetworkSerializerContext context,
                                                         Runnable write) {
        if (context.getVersion().compareTo(minVersion) >= 0) {
            write.run();
        } else {
            assertMinimumVersionIfNotDefault(isNotDefaultValue, rootElementName, attributeName, type, minVersion, context);
        }
    }

    /**
     * Write a <b>mandatory</b> int attribute until a given maximum IIDM version. <br>
     * If the context's IIDM version is strictly more recent than the given maximum IIDM version, do nothing.
     */
    public static void writeIntAttributeUntilMaximumVersion(String attributeName, int value, IidmVersion maxVersion, NetworkSerializerContext context) {
        if (context.getVersion().compareTo(maxVersion) <= 0) {
            context.getWriter().writeIntAttribute(attributeName, value);
        }
    }

    /**
     * Get an attribute name depending on IIDM version.
     * @return oldName if version strictly older than comparisonVersion, else newName.
     */
    public static String getAttributeName(String oldName, String newName, IidmVersion version, IidmVersion comparisonVersion) {
        if (version.compareTo(comparisonVersion) < 0) {
            return oldName;
        } else {
            return newName;
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
    public static Iterable<LoadingLimits.TemporaryLimit> sortedTemporaryLimits(Iterable<LoadingLimits.TemporaryLimit> temporaryLimits, ExportOptions exportOptions) {
        Objects.requireNonNull(temporaryLimits);
        Objects.requireNonNull(exportOptions);
        return exportOptions.isSorted() ? StreamSupport.stream(temporaryLimits.spliterator(), false)
                                                       .sorted(Comparator.comparing(LoadingLimits.TemporaryLimit::getName))
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

    /**
     * Sort names.
     */
    public static Iterable<String> sortedNames(Iterable<String> names, ExportOptions exportOptions) {
        Objects.requireNonNull(names);
        Objects.requireNonNull(exportOptions);
        return exportOptions.isSorted() ? StreamSupport.stream(names.spliterator(), false)
                .sorted()
                .collect(Collectors.toList())
                : names;
    }

    private IidmSerDeUtil() {
    }
}
