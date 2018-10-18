/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import org.joda.time.DateTime;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public interface CgmesModel {

    Properties getProperties();

    String modelId();

    String version();

    DateTime scenarioTime();

    DateTime created();

    boolean isNodeBreaker();

    CgmesTerminal terminal(String terminalId);

    PropertyBags numObjectsByType();

    PropertyBags allObjectsOfType(String type);

    PropertyBags boundaryNodes();

    PropertyBags baseVoltages();

    PropertyBags substations();

    PropertyBags voltageLevels();

    PropertyBags terminals();

    PropertyBags terminalsTP();

    PropertyBags terminalsCN();

    PropertyBags terminalLimits();

    PropertyBags connectivityNodes();

    PropertyBags topologicalNodes();

    PropertyBags switches();

    PropertyBags acLineSegments();

    PropertyBags equivalentBranches();

    PropertyBags transformers();

    PropertyBags transformerEnds();

    // Transformer ends grouped by transformer
    Map<String, PropertyBags> groupedTransformerEnds();

    PropertyBags ratioTapChangers();

    PropertyBags phaseTapChangers();

    PropertyBags energyConsumers();

    PropertyBags energySources();

    PropertyBags shuntCompensators();

    PropertyBags staticVarCompensators();

    PropertyBags synchronousMachines();

    PropertyBags equivalentInjections();

    PropertyBags externalNetworkInjections();

    PropertyBags asynchronousMachines();

    PropertyBags phaseTapChangerTable(String tableId);

    PropertyBags acDcConverters();

    PropertyBags dcLineSegments();

    PropertyBags dcTerminals();

    PropertyBags dcTerminalsTP();

    void clear(Subset subset);

    void add(String contextName, String type, PropertyBags objects);

    void print(PrintStream out);

    void print(Consumer<String> liner);

    void write(DataSource ds);

    // Helper mappings

    // TODO If we could store identifiers for tap changers and terminals in IIDM
    // then we would not need to query back the CGMES model for these mappings

    String terminalForEquipment(String conductingEquipmentId);

    String ratioTapChangerForPowerTransformer(String powerTransformerId);

    String phaseTapChangerForPowerTransformer(String powerTransformerId);

    // Terminals

    class CgmesTerminal {
        private final String id;
        private final String conductingEquipment;
        private final String conductingEquipmentType;
        private final boolean connected;
        private final PowerFlow flow;

        private String connectivityNode;
        private String topologicalNode;
        private String voltageLevel;
        private String substation;

        public CgmesTerminal(
                String id,
                String conductingEquipment,
                String conductingEquipmentType,
                boolean connected,
                PowerFlow flow) {
            this.id = id;
            this.conductingEquipment = conductingEquipment;
            this.conductingEquipmentType = conductingEquipmentType;
            this.connected = connected;
            this.flow = flow;
        }

        public void assignTP(String topologicalNode, String voltageLevel, String substation) {
            checkAssign(topologicalNode, voltageLevel, substation);
        }

        public void assignCN(String connectivityNode, String topologicalNode, String voltageLevel,
                String substation) {
            this.connectivityNode = connectivityNode;
            checkAssign(topologicalNode, voltageLevel, substation);
        }

        public String id() {
            return id;
        }

        public String conductingEquipment() {
            return conductingEquipment;
        }

        public String conductingEquipmentType() {
            return conductingEquipmentType;
        }

        public String connectivityNode() {
            return connectivityNode;
        }

        public String topologicalNode() {
            return topologicalNode;
        }

        public String voltageLevel() {
            return voltageLevel;
        }

        public String substation() {
            return substation;
        }

        public boolean connected() {
            return connected;
        }

        public PowerFlow flow() {
            return flow;
        }

        private void checkAssign(String topologicalNode, String voltageLevel, String substation) {
            checkAssignAttr("topologicalNode", this.topologicalNode, topologicalNode);
            this.topologicalNode = topologicalNode;
            checkAssignAttr("voltageLevel", this.voltageLevel, voltageLevel);
            this.voltageLevel = voltageLevel;
            checkAssignAttr("substation", this.substation, substation);
            this.substation = substation;
        }

        private void checkAssignAttr(String attribute, String value0, String value1) {
            if (value0 == null || value0.equals(value1)) {
                return;
            }
            throw new CgmesModelException(
                    String.format("Inconsistent values for %s: previous %s, now %s",
                            attribute, value0, value1));
        }
    }
}
