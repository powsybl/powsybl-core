/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;
import org.joda.time.DateTime;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public interface CgmesModel {

    // FIXME generic cgmes models may not have an underlying triplestore
    TripleStore tripleStore();

    Properties getProperties();

    default PropertyBags fullModel(String cgmesProfile) {
        return new PropertyBags();
    }

    boolean hasEquipmentCore();

    String modelId();

    String version();

    DateTime scenarioTime();

    DateTime created();

    boolean isNodeBreaker();

    boolean hasBoundary();

    CgmesTerminal terminal(String terminalId);

    Collection<CgmesTerminal> computedTerminals();

    PropertyBags numObjectsByType();

    PropertyBags allObjectsOfType(String type);

    PropertyBags boundaryNodes();

    PropertyBags baseVoltages();

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

    // Transformer ends grouped by transformer
    Map<String, PropertyBags> groupedTransformerEnds();

    PropertyBags ratioTapChangers();

    PropertyBags phaseTapChangers();

    PropertyBags regulatingControls();

    PropertyBags energyConsumers();

    PropertyBags energySources();

    PropertyBags shuntCompensators();

    PropertyBags equivalentShunts();

    PropertyBags nonlinearShuntCompensatorPoints(String id);

    PropertyBags staticVarCompensators();

    PropertyBags synchronousMachines();

    PropertyBags equivalentInjections();

    PropertyBags externalNetworkInjections();

    PropertyBags svInjections();

    PropertyBags asynchronousMachines();

    PropertyBags reactiveCapabilityCurveData();

    PropertyBags ratioTapChangerTablesPoints();

    PropertyBags phaseTapChangerTablesPoints();

    PropertyBags ratioTapChangerTable(String tableId);

    PropertyBags phaseTapChangerTable(String tableId);

    PropertyBags controlAreas();

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

    void read(ReadOnlyDataSource ds);

    void read(ReadOnlyDataSource mainDataSource, ReadOnlyDataSource alternativeDataSourceForBoundary);

    void read(InputStream is, String baseName, String contextName);

    // Helper mappings

    /**
     * @deprecated Not used anymore. To get the CGMES Terminal ID of an equipment, use alias i.e.
     * {@code equipement.getAlias("CGMES.Terminal1")}
     */
    @Deprecated
    default String terminalForEquipment(String conductingEquipmentId, int sequenceNumber) {
        return null;
    }

    /**
     * @deprecated Use {@link #ratioTapChangerListForPowerTransformer(String)} instead.
     */
    @Deprecated
    default String ratioTapChangerForPowerTransformer(String powerTransformerId) {
        return ratioTapChangerListForPowerTransformer(powerTransformerId).stream().filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * @deprecated Use {@link #phaseTapChangerListForPowerTransformer(String)} instead.
     */
    @Deprecated
    default String phaseTapChangerForPowerTransformer(String powerTransformerId) {
        return phaseTapChangerListForPowerTransformer(powerTransformerId).stream().filter(Objects::nonNull).findFirst().orElse(null);
    }

    default List<String> ratioTapChangerListForPowerTransformer(String powerTransformerId) {
        return Collections.singletonList(ratioTapChangerForPowerTransformer(powerTransformerId));
    }

    default List<String> phaseTapChangerListForPowerTransformer(String powerTransformerId) {
        return Collections.singletonList(phaseTapChangerForPowerTransformer(powerTransformerId));
    }

    // TODO(Luma) refactoring node-breaker conversion temporal

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

    CgmesContainer container(String containerId);

    double nominalVoltage(String baseVoltageId);

    default PropertyBags modelProfiles() {
        throw new UnsupportedOperationException();
    }
}
