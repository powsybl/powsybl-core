/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.dynamicsimulation.groovy;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.dynamicsimulation.DynamicSimulationProvider;
import groovy.lang.Binding;

import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public interface GroovyExtension<T> {

    /**
     * Return the name of the {@link DynamicSimulationProvider} instance, this provider is compatible with.
     * This method can return null, if this extension is compatible with any {@link DynamicSimulationProvider} objects.
     *
     * @return The name of a compatible {@link DynamicSimulationProvider}, or null for any
     */
    default String getName() {
        return null;
    }

    /**
     * Load the extension into the binding object. Each time an object is created, the consumer is notified.
     * @param binding The binding where to register the extension
     * @param consumer The consumer to notify on objects creation
     * @param reportNode the reportNode used for functional logs
     */
    void load(Binding binding, Consumer<T> consumer, ReportNode reportNode);

    /**
     * Return the list of available GroovyExtension of type clazz, compatible with the provider which the name is given
     * @param clazz The type of extension to look for in the classpath
     * @param providerName The name of the provider
     *
     * @return A list of extensions compatible with a given provider
     */
    static <T extends GroovyExtension> List<T> find(Class<T> clazz, String providerName) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(providerName);

        return StreamSupport.stream(ServiceLoader.load(clazz, GroovyExtension.class.getClassLoader()).spliterator(), true)
                .filter(e -> e.getName() == null || e.getName().equals(providerName))
                .collect(Collectors.toList());
    }
}
