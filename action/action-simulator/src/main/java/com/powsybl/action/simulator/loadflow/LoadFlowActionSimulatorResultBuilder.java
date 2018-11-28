/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.commons.util.ServiceLoaderCache;

import java.io.Writer;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Defines the result expected from an execution of the load flow action simulator.
 *
 * In particular:
 *  - allows to retrieve the result
 *  - allows to write down the result
 *  - defines how to read and merge results for distributed execution
 *  - defines the building of the actual result by providing a dedicated observer
 *  - provides the possibility to add results consumers, executed on creation of the result
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public interface LoadFlowActionSimulatorResultBuilder<R> {

    /**
     * Name of that output builder, used to identify and retrieve it.
     */
    String getName();

    /**
     * The built result.
     */
    R getResult();

    /**
     * Writes the output to an output stream.
     * A specific format may be requested, but does not have to be supported by all implementations.
     */
    void writeResult(Writer writer, String format);

    /**
     * Creates the merger in charge of reading and merging results for distributed execution.
     */
    ResultMerger createMerger();

    /**
     * Creates the observer in charge of building the actual result during action simulator execution.
     */
    LoadFlowActionSimulatorObserver createObserver();

    /**
     * Add a result handler, which will be executed on result creation (or merging, for distributed execution).
     */
    void addResultHandler(Consumer<R> handler);

    /**
     * Retrieve a result builder implementation by its name. Returns null if implementation is not found.
     */
    static LoadFlowActionSimulatorResultBuilder find(String name) {
        Objects.requireNonNull(name);

        return new ServiceLoaderCache<>(LoadFlowActionSimulatorResultBuilder.class).getServices().stream()
                .filter(e -> name.equals(e.getName()))
                .findFirst()
                .orElseThrow(null);
    }

}
