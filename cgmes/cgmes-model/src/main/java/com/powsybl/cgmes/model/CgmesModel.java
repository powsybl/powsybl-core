/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.model;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;
import java.time.ZonedDateTime;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public interface CgmesModel {

    // Although generic cgmes models may not have an underlying triplestore
    TripleStore tripleStore();

    Properties getProperties();

    default PropertyBags fullModels() {
        return new PropertyBags();
    }

    boolean hasEquipmentCore();

    String modelId();

    String version();

    ZonedDateTime scenarioTime();

    ZonedDateTime created();

    boolean isNodeBreaker();

    boolean hasBoundary();

    CgmesTerminal terminal(String terminalId);

    Collection<CgmesTerminal> computedTerminals();

    PropertyBags numObjectsByType();

    PropertyBags allObjectsOfType(String type);

    PropertyBags boundaryNodes();

    PropertyBags baseVoltages();

    PropertyBags countrySourcingActors(String countryName);

    PropertyBags sourcingActor(String sourcingActor);

    PropertyBags substations();

    PropertyBags voltageLevels();

    PropertyBags terminals();

    PropertyBags connectivityNodeContainers();

    PropertyBags operationalLimits();

    PropertyBags connectivityNodes();

    PropertyBags topologicalNodes();

    PropertyBags busBarSections();

    PropertyBags switches();

    PropertyBags acLineSegments();

    PropertyBags equivalentBranches();

    PropertyBags seriesCompensators();

    PropertyBags transformers();

    PropertyBags transformerEnds();

    PropertyBags ratioTapChangers();

    PropertyBags ratioTapChangerTablePoints();

    PropertyBags phaseTapChangers();

    PropertyBags phaseTapChangerTablePoints();

    PropertyBags regulatingControls();

    PropertyBags energyConsumers();

    PropertyBags energySources();

    PropertyBags shuntCompensators();

    PropertyBags equivalentShunts();

    /**
     * Query all NonlinearShuntCompensatorPoint in the CgmesModel.
     *
     * @return A {@link PropertyBags} with the shunt compensators points properties.
     */
    PropertyBags nonlinearShuntCompensatorPoints();

    PropertyBags staticVarCompensators();

    /**
     * @deprecated Synchronous machines can be generators or condensers, they are obtained separately.
     * Use {@link #synchronousMachinesGenerators()} or {@link #synchronousMachinesCondensers()} instead.
     */
    @Deprecated(since = "6.3.0", forRemoval = true)
    default PropertyBags synchronousMachines() {
        return synchronousMachinesGenerators();
    }

    default PropertyBags synchronousMachinesGenerators() {
        return new PropertyBags();
    }

    default PropertyBags synchronousMachinesCondensers() {
        return new PropertyBags();
    }

    default PropertyBags synchronousMachinesAll() {
        PropertyBags p = new PropertyBags(synchronousMachinesGenerators());
        p.addAll(synchronousMachinesCondensers());
        return p;
    }

    PropertyBags equivalentInjections();

    PropertyBags externalNetworkInjections();

    PropertyBags svInjections();

    PropertyBags asynchronousMachines();

    PropertyBags reactiveCapabilityCurveData();

    PropertyBags controlAreas();

    PropertyBags dcSwitches();

    PropertyBags dcGrounds();

    PropertyBags acDcConverters();

    PropertyBags dcLineSegments();

    PropertyBags dcTerminals();

    default PropertyBags tieFlows() {
        return new PropertyBags();
    }

    default PropertyBags topologicalIslands() {
        return new PropertyBags();
    }

    default PropertyBags graph() {
        return new PropertyBags();
    }

    default PropertyBags grounds() {
        return new PropertyBags();
    }

    CgmesDcTerminal dcTerminal(String dcTerminalId);

    void clear(CgmesSubset subset);

    void add(CgmesSubset subset, String type, PropertyBags objects);

    default void add(String context, String type, PropertyBags objects) {
        throw new UnsupportedOperationException();
    }

    void print(PrintStream out);

    void print(Consumer<String> liner);

    static String baseName(ReadOnlyDataSource ds) {
        return new CgmesOnDataSource(ds).baseName();
    }

    void setBasename(String baseName);

    String getBasename();

    void write(DataSource ds);

    default void write(DataSource ds, CgmesSubset subset) {
        throw new UnsupportedOperationException();
    }

    void read(ReadOnlyDataSource ds, ReportNode reportNode);

    void read(ReadOnlyDataSource mainDataSource, ReadOnlyDataSource alternativeDataSourceForBoundary, ReportNode reportNode);

    void read(InputStream is, String baseName, String contextName, ReportNode reportNode);

    // Helper mappings

    /**
     * Obtain the substation of a given terminal.
     *
     * @param t the terminal
     * @param nodeBreaker to determine the terminal container, use node-breaker connectivity information first
     */
    String substation(CgmesTerminal t, boolean nodeBreaker);

    /**
     * Obtain the voltage level grouping in which a given terminal is contained.
     *
     * @param t the terminal
     * @param nodeBreaker to determine the terminal container, use node-breaker connectivity information first
     */
    String voltageLevel(CgmesTerminal t, boolean nodeBreaker);

    Optional<String> node(CgmesTerminal t, boolean nodeBreaker);

    Optional<CgmesContainer> nodeContainer(String nodeId);

    CgmesContainer container(String containerId);

    double nominalVoltage(String baseVoltageId);

    default PropertyBags modelProfiles() {
        throw new UnsupportedOperationException();
    }
}
