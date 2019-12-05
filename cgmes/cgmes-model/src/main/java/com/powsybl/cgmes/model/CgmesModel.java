/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import org.joda.time.DateTime;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public interface CgmesModel {

    TripleStore tripleStore();

    Properties getProperties();

    boolean hasEquipmentCore();

    String modelId();

    String version();

    DateTime scenarioTime();

    DateTime created();

    boolean isNodeBreaker();

    boolean hasBoundary();

    CgmesTerminal terminal(String terminalId);

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

    PropertyBags nonlinearShuntCompensatorPoints(String id);

    PropertyBags staticVarCompensators();

    PropertyBags synchronousMachines();

    PropertyBags equivalentInjections();

    PropertyBags externalNetworkInjections();

    PropertyBags svInjections();

    PropertyBags asynchronousMachines();

    PropertyBags reactiveCapabilityCurveData();

    PropertyBags ratioTapChangerTablesPoints();

    PropertyBags ratioTapChangerTable(String tableId);

    PropertyBags phaseTapChangerTable(String tableId);

    PropertyBags acDcConverters();

    PropertyBags dcLineSegments();

    PropertyBags dcTerminals();

    PropertyBags dcTerminalsTP();

    void clear(CgmesSubset subset);

    void add(CgmesSubset subset, String type, PropertyBags objects);

    void print(PrintStream out);

    void print(Consumer<String> liner);

    // read/write

    static String baseName(ReadOnlyDataSource ds) {
        return new CgmesOnDataSource(ds).baseName();
    }

    void setBasename(String baseName);

    void write(DataSource ds);

    void read(ReadOnlyDataSource ds);

    void read(ReadOnlyDataSource mainDataSource, ReadOnlyDataSource alternativeDataSourceForBoundary);

    void read(InputStream is, String baseName, String contextName);

    // Helper mappings

    // TODO If we could store identifiers for tap changers and terminals in IIDM
    // then we would not need to query back the CGMES model for these mappings

    String terminalForEquipment(String conductingEquipmentId);

    String ratioTapChangerForPowerTransformer(String powerTransformerId);

    String phaseTapChangerForPowerTransformer(String powerTransformerId);

    // TODO(Luma) refactoring node-breaker conversion temporal

    String substation(CgmesTerminal t);

    String voltageLevel(CgmesTerminal t);

    CgmesContainer container(String containerId);

    double nominalVoltage(String baseVoltageId);

    default PropertyBags modelProfiles() {
        throw new UnsupportedOperationException();
    }
}
