/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.model;

import java.util.Set;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class CgmesNames {

    public static final String RDF = "RDF";
    public static final String ABOUT = "about";
    public static final String ID = "ID";
    public static final String RESOURCE = "resource";

    public static final String NAME = "IdentifiedObject.name";
    public static final String GRAPH = "graph";

    public static final String FULL_MODEL = "FullModel";
    public static final String SCENARIO_TIME = "Model.scenarioTime";
    public static final String CREATED = "Model.created";
    public static final String DESCRIPTION = "Model.description";
    public static final String VERSION = "Model.version";
    public static final String DEPENDENT_ON = "Model.DependentOn";
    public static final String PROFILE = "Model.profile";
    public static final String SUPERSEDES = "Model.Supersedes";
    public static final String MODELING_AUTHORITY_SET = "Model.modelingAuthoritySet";

    public static final String CONNECTIVITY_NODE = "ConnectivityNode";
    public static final String SUBSTATION = "Substation";
    public static final String VOLTAGE_LEVEL = "VoltageLevel";
    public static final String TERMINAL = "Terminal";
    public static final String SEQUENCE_NUMBER = "sequenceNumber";
    public static final String AC_LINE_SEGMENT = "ACLineSegment";
    public static final String SERIES_COMPENSATOR = "SeriesCompensator";
    public static final String TOPOLOGICAL_NODE = "TopologicalNode";
    public static final String CONDUCTING_EQUIPMENT = "ConductingEquipment";
    public static final String SWITCH = "Switch";

    public static final String TRANSFORMER_WINDING_RATED_U = "transformerWindingRatedU";
    public static final String TRANSFORMER_END = "TransformerEnd";
    public static final String TAP_CHANGER = "TapChanger";
    public static final String CONTINUOUS_POSITION = "continuousPosition";
    public static final String POSITION = "position";
    public static final String LOW_STEP = "lowStep";
    public static final String HIGH_STEP = "highStep";

    public static final String DC_TERMINAL = "DCTerminal";
    public static final String RATED_UDC = "ratedUdc";

    public static final String B_PER_SECTION = "bPerSection";

    public static final String VOLTAGE = "v";
    public static final String ANGLE = "angle";

    public static final String RATIO_TAP_CHANGER_TABLE = "RatioTapChangerTable";
    public static final String PHASE_TAP_CHANGER_TABLE = "PhaseTapChangerTable";

    public static final String TERMINAL1 = "Terminal1";
    public static final String TERMINAL2 = "Terminal2";
    public static final String TERMINAL3 = "Terminal3";

    public static final String TOPOLOGICAL_ISLAND = "TopologicalIsland";
    public static final String ANGLEREF_TOPOLOGICALNODE = "AngleRefTopologicalNode";
    public static final String TOPOLOGICAL_NODES = "TopologicalNodes";

    public static final String POWER_TRANSFORMER = "PowerTransformer";
    public static final String R = "r";
    public static final String X = "x";
    public static final String RATIO_TAP_CHANGER = "RatioTapChanger";
    public static final String PHASE_TAP_CHANGER = "PhaseTapChanger";
    public static final String RATEDU = "ratedU";
    public static final String RATEDS = "ratedS";
    public static final String G = "g";
    public static final String B = "b";
    public static final String PHASE_ANGLE_CLOCK = "phaseAngleClock";
    public static final String STEP_VOLTAGE_INCREMENT = "stepVoltageIncrement";
    public static final String STEP_PHASE_SHIFT_INCREMENT = "stepPhaseShiftIncrement";
    public static final String NEUTRAL_STEP = "neutralStep";
    public static final String NORMAL_STEP = "normalStep";
    public static final String SV_TAP_STEP = "SVtapStep";
    public static final String LTC_FLAG = "ltcFlag";
    public static final String STEP = "step";
    public static final String RATIO = "ratio";

    public static final String PHASE_TAP_CHANGER_TYPE = "phaseTapChangerType";
    public static final String X_STEP_MIN = "xStepMin";
    public static final String X_STEP_MAX = "xStepMax";
    public static final String X_MIN = "xMin";
    public static final String X_MAX = "xMax";
    public static final String VOLTAGE_STEP_INCREMENT = "voltageStepIncrement";
    public static final String WINDING_CONNECTION_ANGLE = "windingConnectionAngle";
    public static final String LINEAR = "linear";
    public static final String SYMMETRICAL = "symmetrical";
    public static final String ASYMMETRICAL = "asymmetrical";
    public static final String TABULAR = "tabular";
    public static final String TCUL_CONTROL_MODE = "tculControlMode";
    public static final String TAP_CHANGER_CONTROL_ENABLED = "tapChangerControlEnabled";
    public static final String PHASE_TAP_CHANGER_TABULAR = "PhaseTapChangerTabular";

    public static final Set<String> SWITCH_TYPES = Set.of(SWITCH, "Breaker", "Disconnector", "LoadBreakSwitch", "ProtectedSwitch", "GroundDisconnector");

    public static final String BUSBAR_SECTION = "BusbarSection";
    public static final String GROUND = "Ground";
    public static final String SHUNT_COMPENSATOR = "ShuntCompensator";
    public static final String EQUIVALENT_SHUNT = "EquivalentShunt";
    public static final String EQUIVALENT_BRANCH = "EquivalentBranch";
    public static final String STATIC_VAR_COMPENSATOR = "StaticVarCompensator";
    public static final String REGULATING_CONTROL = "RegulatingControl";
    public static final String CONTROL_AREA = "ControlArea";
    public static final String CONTROL_AREA_TYPE_KIND_INTERCHANGE = "ControlAreaTypeKind.Interchange";
    public static final String TIE_FLOW = "TieFlow";
    public static final String CONFORM_LOAD = "ConformLoad";
    public static final String NONCONFORM_LOAD = "NonConformLoad";
    public static final String ENERGY_CONSUMER = "EnergyConsumer";
    public static final String STATION_SUPPLY = "StationSupply";
    public static final String ASYNCHRONOUS_MACHINE = "AsynchronousMachine";
    public static final String SYNCHRONOUS_MACHINE = "SynchronousMachine";
    public static final String ENERGY_SOURCE = "EnergySource";
    public static final String SV_INJECTION = "SvInjection";
    public static final String CONFORM_LOAD_GROUP = "ConformLoadGroup";
    public static final String NONCONFORM_LOAD_GROUP = "NonConformLoadGroup";
    public static final String ACDC_CONVERTER = "ACDCConverter";
    public static final String DC_LINE_SEGMENT = "DCLineSegment";

    public static final String CONNECTIVITY_NODE_BOUNDARY = "ConnectivityNode_Boundary";
    public static final String TOPOLOGICAL_NODE_BOUNDARY = "TopologicalNode_Boundary";
    public static final String GENERATOR_OR_MOTOR = "generatorOrMotor";

    public static final String CONNECTIVITY_NODE_CONTAINER = "ConnectivityNodeContainer";
    public static final String IDENTIFIED_OBJECT_DESCRIPTION = "IdentifiedObject.description";

    public static final String EQUIVALENT_INJECTION = "EquivalentInjection";
    public static final String EQUIVALENT_INJECTION_TERMINAL = "EquivalentInjectionTerminal";
    public static final String EXTERNAL_NETWORK_INJECTION = "ExternalNetworkInjection";

    public static final String OPERATIONAL_LIMIT = "OperationalLimit";
    public static final String OPERATIONAL_LIMIT_SET = "OperationalLimitSet";
    public static final String CURRENT_LIMIT = "CurrentLimit";
    public static final String ACTIVE_POWER_LIMIT = "ActivePowerLimit";
    public static final String APPARENT_POWER_LIMIT = "ApparentPowerLimit";
    public static final String P_FIXED = "pFixed";
    public static final String Q_FIXED = "qFixed";

    public static final String ENERGY_IDENT_CODE_EIC = "energyIdentCodeEic";
    public static final String P_TOLERANCE = "pTolerance";

    private CgmesNames() {
    }
}
