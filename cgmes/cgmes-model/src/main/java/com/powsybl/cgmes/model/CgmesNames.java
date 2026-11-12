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

    public static final String IDENTIFIED_OBJECT_NAME = "IdentifiedObject.name";
    public static final String MRID = "IdentifiedObject.mRID";
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

    public static final String REGION_ID = "regionId";
    public static final String REGION_NAME = "regionName";
    public static final String SUB_REGION_ID = "subRegionId";
    public static final String CONNECTIVITY_NODE = "ConnectivityNode";
    public static final String SUBSTATION = "Substation";
    public static final String VOLTAGE_LEVEL = "VoltageLevel";
    public static final String BASE_VOLTAGE = "BaseVoltage";
    public static final String TERMINAL = "Terminal";
    public static final String AC_DC_TERMINAL = "ACDCTerminal";
    public static final String SEQUENCE_NUMBER = "sequenceNumber";
    public static final String AC_LINE_SEGMENT = "ACLineSegment";
    public static final String SERIES_COMPENSATOR = "SeriesCompensator";
    public static final String TOPOLOGICAL_NODE = "TopologicalNode";
    public static final String CONDUCTING_EQUIPMENT = "ConductingEquipment";
    public static final String SWITCH = "Switch";

    public static final String TRANSFORMER_WINDING_RATED_U = "transformerWindingRatedU";
    public static final String TRANSFORMER_END = "TransformerEnd";
    public static final String TRANSFORMER_END1 = "TransformerEnd1";
    public static final String TRANSFORMER_END2 = "TransformerEnd2";
    public static final String TRANSFORMER_END3 = "TransformerEnd3";
    public static final String TAP_CHANGER = "TapChanger";
    public static final String TAP_CHANGER_CONTROL = "TapChangerControl";
    public static final String CONTINUOUS_POSITION = "continuousPosition";
    public static final String POSITION = "position";
    public static final String LOW_STEP = "lowStep";
    public static final String HIGH_STEP = "highStep";

    public static final String DC_NODE = "DCNode";
    public static final String DC_TOPOLOGICAL_NODE = "DCTopologicalNode";
    public static final String ACDC_CONVERTER_DC_TERMINAL = "ACDCConverterDCTerminal";
    public static final String DC_BASE_TERMINAL = "DCBaseTerminal";
    public static final String DC_CONDUCTING_EQUIPMENT = "DCConductingEquipment";
    public static final String DC_TERMINAL = "DCTerminal";
    public static final String DC_TERMINAL1 = "DCTerminal1";
    public static final String DC_TERMINAL2 = "DCTerminal2";
    public static final String RATED_UDC = "ratedUdc";
    public static final String MONOPOLAR_GROUND_RETURN = "monopolarGroundReturn";

    public static final String B_PER_SECTION = "bPerSection";

    public static final String VOLTAGE = "v";
    public static final String ANGLE = "angle";

    public static final String RATIO_TAP_CHANGER_TABLE = "RatioTapChangerTable";
    public static final String RATIO_TAP_CHANGER_TABLE_POINT = "RatioTapChangerTablePoint";
    public static final String PHASE_TAP_CHANGER_TABLE = "PhaseTapChangerTable";
    public static final String PHASE_TAP_CHANGER_TABLE_POINT = "PhaseTapChangerTablePoint";
    public static final String TAP_CHANGER_TABLE_POINT = "TapChangerTablePoint";

    public static final String TERMINAL1 = "Terminal1";
    public static final String TERMINAL2 = "Terminal2";
    public static final String TERMINAL3 = "Terminal3";

    public static final String TOPOLOGICAL_ISLAND = "TopologicalIsland";
    public static final String ANGLEREF_TOPOLOGICALNODE = "AngleRefTopologicalNode";
    public static final String TOPOLOGICAL_NODES = "TopologicalNodes";

    public static final String POWER_TRANSFORMER = "PowerTransformer";
    public static final String POWER_TRANSFORMER_END = "PowerTransformerEnd";
    public static final String R = "r";
    public static final String X = "x";
    public static final String RATIO_TAP_CHANGER = "RatioTapChanger";
    public static final String RATIO_TAP_CHANGER1 = "RatioTapChanger1";
    public static final String RATIO_TAP_CHANGER2 = "RatioTapChanger2";
    public static final String RATIO_TAP_CHANGER3 = "RatioTapChanger3";
    public static final String PHASE_TAP_CHANGER = "PhaseTapChanger";
    public static final String PHASE_TAP_CHANGER1 = "PhaseTapChanger1";
    public static final String PHASE_TAP_CHANGER2 = "PhaseTapChanger2";
    public static final String PHASE_TAP_CHANGER3 = "PhaseTapChanger3";
    public static final String RATEDU = "ratedU";
    public static final String RATEDS = "ratedS";
    public static final String G = "g";
    public static final String B = "b";
    public static final String PHASE_ANGLE_CLOCK = "phaseAngleClock";
    public static final String STEP_VOLTAGE_INCREMENT = "stepVoltageIncrement";
    public static final String STEP_PHASE_SHIFT_INCREMENT = "stepPhaseShiftIncrement";
    public static final String NEUTRAL_STEP = "neutralStep";
    public static final String NEUTRAL_U = "neutralU";
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
    public static final String TRANSFORMER_CONTROL_MODE = "TransformerControlMode";
    public static final String TAP_CHANGER_CONTROL_ENABLED = "tapChangerControlEnabled";
    public static final String PHASE_TAP_CHANGER_TABULAR = "PhaseTapChangerTabular";

    public static final Set<String> SWITCH_TYPES = Set.of(SWITCH, "Breaker", "Disconnector", "LoadBreakSwitch", "ProtectedSwitch", "GroundDisconnector");

    public static final String BUSBAR_SECTION = "BusbarSection";
    public static final String GROUND = "Ground";
    public static final String SHUNT_COMPENSATOR = "ShuntCompensator";
    public static final String LINEAR_SHUNT_COMPENSATOR = "LinearShuntCompensator";
    public static final String NONLINEAR_SHUNT_COMPENSATOR = "NonlinearShuntCompensator";
    public static final String NONLINEAR_SHUNT_COMPENSATOR_POINT = "NonlinearShuntCompensatorPoint";
    public static final String EQUIVALENT_SHUNT = "EquivalentShunt";
    public static final String EQUIVALENT_BRANCH = "EquivalentBranch";
    public static final String STATIC_VAR_COMPENSATOR = "StaticVarCompensator";
    public static final String REACTIVE_CAPABILITY_CURVE = "ReactiveCapabilityCurve";
    public static final String VS_CAPABILITY_CURVE = "VsCapabilityCurve";
    public static final String CURVE = "Curve";
    public static final String REGULATING_CONTROL = "RegulatingControl";
    public static final String CONTROL_AREA = "ControlArea";
    public static final String CONTROL_AREA_TYPE_KIND_INTERCHANGE = "ControlAreaTypeKind.Interchange";
    public static final String ENERGY_AREA = "EnergyArea";
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
    public static final String VS_CONVERTER = "VsConverter";
    public static final String CS_CONVERTER = "CsConverter";
    public static final String DC_CONVERTER_UNIT = "DCConverterUnit";
    public static final String DC_EQUIPMENT_CONTAINER = "DCEquipmentContainer";
    public static final String DC_LINE_SEGMENT = "DCLineSegment";
    public static final String DC_LINE_SEGMENT2 = "DCLineSegment2";
    public static final String DC_SWITCH = "DCSwitch";
    public static final String DC_GROUND = "DCGround";

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
    public static final String OPERATIONAL_LIMIT_TYPE = "OperationalLimitType";
    public static final String NORMAL_VALUE = "normalValue";
    public static final String VALUE = "value";
    public static final String CURRENT_LIMIT = "CurrentLimit";
    public static final String ACTIVE_POWER_LIMIT = "ActivePowerLimit";
    public static final String APPARENT_POWER_LIMIT = "ApparentPowerLimit";
    public static final String P_FIXED = "pFixed";
    public static final String Q_FIXED = "qFixed";
    public static final String TERMINAL_SIGN = "terminalSign";
    public static final String TERMINAL_SIGN1 = "terminalSign1";
    public static final String TERMINAL_SIGN2 = "terminalSign2";
    public static final String TERMINAL_SIGN3 = "terminalSign3";
    public static final String GENERATING_UNIT = "GeneratingUnit";
    public static final String NORMAL_PF = "normalPF";
    public static final String REGULATION_CAPABILITY = "regulationCapability";
    public static final String REGULATION_TARGET = "regulationTarget";
    public static final String REGULATION_STATUS = "regulationStatus";
    public static final String MODE = "mode";
    public static final String INITIAL_P = "initialP";
    public static final String MIN_P = "minP";
    public static final String MAX_P = "maxP";
    public static final String MIN_Q = "minQ";
    public static final String MAX_Q = "maxQ";
    public static final String NORMAL_SECTIONS = "normalSections";
    public static final String TARGET_VALUE = "targetValue";
    public static final String TARGET_DEADBAND = "targetDeadband";
    public static final String ENABLED = "enabled";
    public static final String CONTROL_ENABLED = "controlEnabled";
    public static final String REACTIVE_POWER = "reactivepower";
    public static final String VOLTAGE_TAG = "voltage";
    public static final String NORMAL_OPEN = "normalOpen";
    public static final String CONNECTED = "connected";
    public static final String OPEN = "open";
    public static final String TERMINAL_BOUNDARY = "Terminal_Boundary";
    public static final String HIGH_VOLTAGE_LIMIT = "highVoltageLimit";
    public static final String LOW_VOLTAGE_LIMIT = "lowVoltageLimit";
    public static final String SVC_EQ_VOLTAGE_SET_POINT = "svcEquipmentVoltageSetPoint";

    public static final String ENERGY_IDENT_CODE_EIC = "energyIdentCodeEic";
    public static final String P_TOLERANCE = "pTolerance";

    public static final String CONDUCTING_EQUIPMENT_BASE_VOLTAGE = CgmesNames.CONDUCTING_EQUIPMENT + "." + CgmesNames.BASE_VOLTAGE;
    public static final String EQUIPMENT_EQUIPMENT_CONTAINER = "Equipment.EquipmentContainer";
    public static final String REGULATING_COND_EQ_REGULATING_CONTROL = "RegulatingCondEq." + REGULATING_CONTROL;
    public static final String REGULATING_CONTROL_VOLTAGE = "RegulatingControlModeKind.voltage";
    public static final String REGULATING_CONTROL_REACTIVE_POWER = "RegulatingControlModeKind.reactivePower";
    public static final String REGULATING_CONTROL_ACTIVE_POWER = "RegulatingControlModeKind.activePower";

    public static final String AC_LINE_SEGMENT_R = AC_LINE_SEGMENT + "." + R;
    public static final String AC_LINE_SEGMENT_X = AC_LINE_SEGMENT + "." + X;
    public static final String AC_LINE_SEGMENT_GCH = AC_LINE_SEGMENT + ".gch";
    public static final String AC_LINE_SEGMENT_BCH = AC_LINE_SEGMENT + ".bch";
    public static final String ACDC_CONVERTER_RATED_UDC = ACDC_CONVERTER + "." + RATED_UDC;
    public static final String ACDC_CONVERTER_IDLE_LOSS = ACDC_CONVERTER + ".idleLoss";
    public static final String ACDC_CONVERTER_SWITCHING_LOSS = ACDC_CONVERTER + ".switchingLoss";
    public static final String ACDC_CONVERTER_RESISTIVE_LOSS = ACDC_CONVERTER + ".resistiveLoss";
    public static final String ACDC_CONVERTER_PCC_TERMINAL = ACDC_CONVERTER + ".PccTerminal";
    public static final String VS_CONVERTER_CAPABILITY_CURVE = VS_CONVERTER + ".CapabilityCurve";
    public static final String BASE_VOLTAGE_NOMINAL_VOLTAGE = BASE_VOLTAGE + ".nominalVoltage";
    public static final String CONNECTIVITY_NODE_CONNECTIVITY_NODE_CONTAINER = CONNECTIVITY_NODE + "." + CONNECTIVITY_NODE_CONTAINER;
    public static final String CONTROL_AREA_TYPE = CONTROL_AREA + ".type";
    public static final String CONTROL_AREA_ENERGY_AREA = CONTROL_AREA + "." + ENERGY_AREA;
    public static final String IDENTIFIED_OBJECT_ENERGY_IDENT_CODE_EIC = "IdentifiedObject." + ENERGY_IDENT_CODE_EIC;
    public static final String CURVE_STYLE_STRAIGHT_LINE_Y_VALUES = "CurveStyle.straightLineYValues";
    public static final String UNIT_SYMBOL_W = "UnitSymbol.W";
    public static final String UNIT_SYMBOL_VAR = "UnitSymbol.VAr";
    public static final String CURVE_CURVE_STYLE = CURVE + ".curveStyle";
    public static final String CURVE_X_UNIT = CURVE + ".xUnit";
    public static final String CURVE_Y1_UNIT = CURVE + ".y1Unit";
    public static final String CURVE_Y2_UNIT = CURVE + ".y2Unit";
    public static final String CURVE_DATA = "CurveData";
    public static final String CURVE_DATA_XVALUE = CURVE_DATA + ".xvalue";
    public static final String CURVE_DATA_Y1VALUE = CURVE_DATA + ".y1value";
    public static final String CURVE_DATA_Y2VALUE = CURVE_DATA + ".y2value";
    public static final String CURVE_DATA_CURVE = CURVE_DATA + ".Curve";
    public static final String OPERATING_MODE_KIND = "DCConverterOperatingModeKind.";
    public static final String DC_CONVERTER_UNIT_OPERATION_MODE = DC_CONVERTER_UNIT + ".operationMode";
    public static final String DC_CONVERTER_UNIT_SUBSTATION = DC_CONVERTER_UNIT + "." + SUBSTATION;
    public static final String DC_GROUND_R = DC_GROUND + "." + R;
    public static final String DC_LINE_SEGMENT_RESISTANCE = DC_LINE_SEGMENT + ".resistance";
    public static final String DC_NODE_DC_EQUIPMENT_CONTAINER = DC_NODE + "." + DC_EQUIPMENT_CONTAINER;
    public static final String DC_BASE_TERMINAL_DC_NODE = DC_BASE_TERMINAL + "." + DC_NODE;
    public static final String AC_DC_TERMINAL_SEQUENCE_NUMBER = AC_DC_TERMINAL + "." + SEQUENCE_NUMBER;
    public static final String ENERGY_CONSUMER_LOAD_RESPONSE = ENERGY_CONSUMER + ".LoadResponse";
    public static final String LOAD_GROUP = "LoadGroup";
    public static final String EQUIVALENT_BRANCH_R = EQUIVALENT_BRANCH + "." + R;
    public static final String EQUIVALENT_BRANCH_X = EQUIVALENT_BRANCH + "." + X;
    public static final String EQUIVALENT_INJECTION_REGULATION_CAPABILITY = EQUIVALENT_INJECTION + "." + REGULATION_CAPABILITY;
    public static final String EQUIVALENT_INJECTION_MIN_P = EQUIVALENT_INJECTION + "." + MIN_P;
    public static final String EQUIVALENT_INJECTION_MAX_P = EQUIVALENT_INJECTION + "." + MAX_P;
    public static final String EQUIVALENT_INJECTION_MIN_Q = EQUIVALENT_INJECTION + "." + MIN_Q;
    public static final String EQUIVALENT_INJECTION_MAX_Q = EQUIVALENT_INJECTION + "." + MAX_Q;
    public static final String EQUIVALENT_INJECTION_REACTIVE_CAPABILITY_CURVE = EQUIVALENT_INJECTION + ".ReactiveCapabilityCurve";
    public static final String EQUIVALENT_SHUNT_B = EQUIVALENT_SHUNT + "." + B;
    public static final String EQUIVALENT_SHUNT_G = EQUIVALENT_SHUNT + "." + G;
    public static final String EXTERNAL_NETWORK_INJECTION_GOVERNOR_SCD = EXTERNAL_NETWORK_INJECTION + ".governorSCD";
    public static final String EXTERNAL_NETWORK_INJECTION_MAX_P = EXTERNAL_NETWORK_INJECTION + "." + MAX_P;
    public static final String EXTERNAL_NETWORK_INJECTION_MAX_Q = EXTERNAL_NETWORK_INJECTION + "." + MAX_Q;
    public static final String EXTERNAL_NETWORK_INJECTION_MIN_P = EXTERNAL_NETWORK_INJECTION + "." + MIN_P;
    public static final String EXTERNAL_NETWORK_INJECTION_MIN_Q = EXTERNAL_NETWORK_INJECTION + "." + MIN_Q;
    public static final String FOSSIL_FUEL = "FossilFuel";
    public static final String FOSSIL_FUEL_FOSSIL_FUEL_TYPE = FOSSIL_FUEL + ".fossilFuelType";
    public static final String FOSSIL_FUEL_THERMAL_GENERATING_UNIT = FOSSIL_FUEL + ".ThermalGeneratingUnit";
    public static final String GENERATING_UNIT_MIN_OPERATING_P = GENERATING_UNIT + ".minOperatingP";
    public static final String GENERATING_UNIT_MAX_OPERATING_P = GENERATING_UNIT + ".maxOperatingP";
    public static final String GENERATING_UNIT_INITIAL_P = GENERATING_UNIT + "." + INITIAL_P;
    public static final String WIND_GENERATING_UNIT = "WindGeneratingUnit";
    public static final String WIND_GENERATING_UNIT_WIND_GEN_UNIT_TYPE = WIND_GENERATING_UNIT + ".windGenUnitType";
    public static final String WIND_GEN_UNIT_KIND = "WindGenUnitKind.";
    public static final String HYDRO_GENERATING_UNIT = "HydroGeneratingUnit";
    public static final String HYDRO_POWER_PLANT = "HydroPowerPlant";
    public static final String HYDRO_GENERATING_UNIT_HYDRO_POWER_PLANT = HYDRO_GENERATING_UNIT + "." + HYDRO_POWER_PLANT;
    public static final String HYDRO_POWER_PLANT_HYDRO_PLANT_STORAGE_TYPE = HYDRO_POWER_PLANT + ".hydroPlantStorageType";
    public static final String GEOGRAPHICAL_REGION = "GeographicalRegion";
    public static final String LOAD_AREA = "LoadArea";
    public static final String SUB_LOAD_AREA = "SubLoadArea";
    public static final String SUB_LOAD_AREA_LOAD_AREA = SUB_LOAD_AREA + "." + LOAD_AREA;
    public static final String LOAD_GROUP_SUB_LOAD_AREA = LOAD_GROUP + "." + SUB_LOAD_AREA;
    public static final String LOAD_RESPONSE_CHARACTERISTIC = "LoadResponseCharacteristic";
    public static final String LOAD_RESPONSE_CHARACTERISTIC_EXPONENT_MODEL = LOAD_RESPONSE_CHARACTERISTIC + ".exponentModel";
    public static final String LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_CURRENT = LOAD_RESPONSE_CHARACTERISTIC + ".pConstantCurrent";
    public static final String LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_IMPEDANCE = LOAD_RESPONSE_CHARACTERISTIC + ".pConstantImpedance";
    public static final String LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_POWER = LOAD_RESPONSE_CHARACTERISTIC + ".pConstantPower";
    public static final String LOAD_RESPONSE_CHARACTERISTIC_P_VOLTAGE_EXPONENT = LOAD_RESPONSE_CHARACTERISTIC + ".pVoltageExponent";
    public static final String LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_CURRENT = LOAD_RESPONSE_CHARACTERISTIC + ".qConstantCurrent";
    public static final String LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_IMPEDANCE = LOAD_RESPONSE_CHARACTERISTIC + ".qConstantImpedance";
    public static final String LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_POWER = LOAD_RESPONSE_CHARACTERISTIC + ".qConstantPower";
    public static final String LOAD_RESPONSE_CHARACTERISTIC_Q_VOLTAGE_EXPONENT = LOAD_RESPONSE_CHARACTERISTIC + ".qVoltageExponent";
    public static final String OPERATIONAL_LIMIT_OPERATIONAL_LIMIT_SET = OPERATIONAL_LIMIT + "." + OPERATIONAL_LIMIT_SET;
    public static final String OPERATIONAL_LIMIT_OPERATIONAL_LIMIT_TYPE = OPERATIONAL_LIMIT + "." + OPERATIONAL_LIMIT_TYPE;
    public static final String OPERATIONAL_LIMIT_SET_TERMINAL = OPERATIONAL_LIMIT_SET + "." + TERMINAL;
    public static final String OPERATIONAL_LIMIT_DIRECTION_KIND_ABSOLUTE_VALUE = "OperationalLimitDirectionKind.absoluteValue";
    public static final String OPERATIONAL_LIMIT_TYPE_DIRECTION = OPERATIONAL_LIMIT_TYPE + ".direction";
    public static final String OPERATIONAL_LIMIT_TYPE_ACCEPTABLE_DURATION = OPERATIONAL_LIMIT_TYPE + ".acceptableDuration";
    public static final String OPERATIONAL_LIMIT_TYPE_IS_INFINITE_DURATION = OPERATIONAL_LIMIT_TYPE + ".isInfiniteDuration";
    public static final String TRANSFORMER_END_END_NUMBER = TRANSFORMER_END + ".endNumber";
    public static final String TRANSFORMER_END_TERMINAL = TRANSFORMER_END + "." + TERMINAL;
    public static final String TRANSFORMER_END_BASE_VOLTAGE = TRANSFORMER_END + "." + BASE_VOLTAGE;
    public static final String POWER_TRANSFORMER_END_POWER_TRANSFORMER = POWER_TRANSFORMER_END + "." + POWER_TRANSFORMER;
    public static final String POWER_TRANSFORMER_END_R = POWER_TRANSFORMER_END + "." + R;
    public static final String POWER_TRANSFORMER_END_X = POWER_TRANSFORMER_END + "." + X;
    public static final String POWER_TRANSFORMER_END_G = POWER_TRANSFORMER_END + "." + G;
    public static final String POWER_TRANSFORMER_END_B = POWER_TRANSFORMER_END + "." + B;
    public static final String POWER_TRANSFORMER_END_RATED_S = POWER_TRANSFORMER_END + "." + RATEDS;
    public static final String POWER_TRANSFORMER_END_RATED_U = POWER_TRANSFORMER_END + "." + RATEDU;
    public static final String REGULATING_CONTROL_TERMINAL = REGULATING_CONTROL + "." + TERMINAL;
    public static final String REGULATING_CONTROL_MODE = REGULATING_CONTROL + "." + MODE;
    public static final String SERIES_COMPENSATOR_R = SERIES_COMPENSATOR + "." + R;
    public static final String SERIES_COMPENSATOR_X = SERIES_COMPENSATOR + "." + X;
    public static final String SERIES_COMPENSATOR_VARISTOR_PRESENT = SERIES_COMPENSATOR + ".varistorPresent";
    public static final String SERIES_COMPENSATOR_VARISTOR_RATED_CURRENT = SERIES_COMPENSATOR + ".varistorRatedCurrent";
    public static final String SERIES_COMPENSATOR_VARISTOR_VOLTAGE_THRESHOLD = SERIES_COMPENSATOR + ".varistorVoltageThreshold";
    public static final String SHUNT_COMPENSATOR_NORMAL_SECTIONS = SHUNT_COMPENSATOR + "." + NORMAL_SECTIONS;
    public static final String SHUNT_COMPENSATOR_MAXIMUM_SECTIONS = SHUNT_COMPENSATOR + ".maximumSections";
    public static final String SHUNT_COMPENSATOR_NOM_U = SHUNT_COMPENSATOR + ".nomU";
    public static final String LINEAR_SHUNT_COMPENSATOR_B_PER_SECTION = LINEAR_SHUNT_COMPENSATOR + "." + B_PER_SECTION;
    public static final String LINEAR_SHUNT_COMPENSATOR_G_PER_SECTION = LINEAR_SHUNT_COMPENSATOR + ".gPerSection";
    public static final String NONLINEAR_SHUNT_COMPENSATOR_POINT_NONLINEAR_SHUNT_COMPENSATOR = NONLINEAR_SHUNT_COMPENSATOR_POINT + "." + NONLINEAR_SHUNT_COMPENSATOR;
    public static final String NONLINEAR_SHUNT_COMPENSATOR_POINT_SECTION_NUMBER = NONLINEAR_SHUNT_COMPENSATOR_POINT + ".sectionNumber";
    public static final String NONLINEAR_SHUNT_COMPENSATOR_POINT_B = NONLINEAR_SHUNT_COMPENSATOR_POINT + "." + B;
    public static final String NONLINEAR_SHUNT_COMPENSATOR_POINT_G = NONLINEAR_SHUNT_COMPENSATOR_POINT + "." + G;
    public static final String STATIC_VAR_COMPENSATOR_INDUCTIVE_RATING = STATIC_VAR_COMPENSATOR + ".inductiveRating";
    public static final String STATIC_VAR_COMPENSATOR_CAPACITIVE_RATING = STATIC_VAR_COMPENSATOR + ".capacitiveRating";
    public static final String STATIC_VAR_COMPENSATOR_SLOPE = STATIC_VAR_COMPENSATOR + ".slope";
    public static final String STATIC_VAR_COMPENSATOR_SVC_CONTROL_MODE = STATIC_VAR_COMPENSATOR + ".sVCControlMode";
    public static final String STATIC_VAR_COMPENSATOR_VOLTAGE_SET_POINT = STATIC_VAR_COMPENSATOR + ".voltageSetPoint";
    public static final String SUB_GEOGRAPHICAL_REGION = "SubGeographicalRegion";
    public static final String SUB_GEOGRAPHICAL_REGION_REGION = "SubGeographicalRegion.Region";
    public static final String SUBSTATION_REGION = SUBSTATION + ".Region";
    public static final String SWITCH_NORMAL_OPEN = SWITCH + "." + NORMAL_OPEN;
    public static final String SWITCH_RETAINED = SWITCH + ".retained";
    public static final String ROTATING_MACHINE_GENERATING_UNIT = "RotatingMachine." + GENERATING_UNIT;
    public static final String SYNCHRONOUS_MACHINE_INITIAL_REACTIVE_CAPABILITY_CURVE = SYNCHRONOUS_MACHINE + ".InitialReactiveCapabilityCurve";
    public static final String SYNCHRONOUS_MACHINE_MIN_Q = SYNCHRONOUS_MACHINE + "." + MIN_Q;
    public static final String SYNCHRONOUS_MACHINE_MAX_Q = SYNCHRONOUS_MACHINE + "." + MAX_Q;
    public static final String ROTATING_MACHINE_RATED_S = "RotatingMachine." + RATEDS;
    public static final String SYNCHRONOUS_MACHINE_TYPE = SYNCHRONOUS_MACHINE + ".type";
    public static final String SYNCHRONOUS_MACHINE_KIND = "SynchronousMachineKind.";
    public static final String PHASE_TAP_CHANGER_TRANSFORMER_END = PHASE_TAP_CHANGER + "." + TRANSFORMER_END;
    public static final String PHASE_TAP_CHANGER_TABULAR_PHASE_TAP_CHANGER_TABLE = PHASE_TAP_CHANGER_TABULAR + "." + PHASE_TAP_CHANGER_TABLE;
    public static final String RATIO_TAP_CHANGER_TRANSFORMER_END = RATIO_TAP_CHANGER + "." + TRANSFORMER_END;
    public static final String RATIO_TAP_CHANGER_RATIO_TAP_CHANGER_TABLE = RATIO_TAP_CHANGER + "." + RATIO_TAP_CHANGER_TABLE;
    public static final String RATIO_TAP_CHANGER_STEP_VOLTAGE_INCREMENT = RATIO_TAP_CHANGER + "." + STEP_VOLTAGE_INCREMENT;
    public static final String RATIO_TAP_CHANGER_TCUL_CONTROL_MODE = RATIO_TAP_CHANGER + "." + TCUL_CONTROL_MODE;
    public static final String TAP_CHANGER_LOW_STEP = TAP_CHANGER + "." + LOW_STEP;
    public static final String TAP_CHANGER_HIGH_STEP = TAP_CHANGER + "." + HIGH_STEP;
    public static final String TAP_CHANGER_NORMAL_STEP = TAP_CHANGER + "." + NORMAL_STEP;
    public static final String TAP_CHANGER_NEUTRAL_STEP = TAP_CHANGER + "." + NEUTRAL_STEP;
    public static final String TAP_CHANGER_NEUTRAL_U = TAP_CHANGER + "." + NEUTRAL_U;
    public static final String TAP_CHANGER_LTC_FLAG = TAP_CHANGER + "." + LTC_FLAG;
    public static final String TAP_CHANGER_TAP_CHANGER_CONTROL = TAP_CHANGER + "." + TAP_CHANGER_CONTROL;
    public static final String TAP_CHANGER_TABLE_POINT_R = TAP_CHANGER_TABLE_POINT + "." + R;
    public static final String TAP_CHANGER_TABLE_POINT_X = TAP_CHANGER_TABLE_POINT + "." + X;
    public static final String TAP_CHANGER_TABLE_POINT_G = TAP_CHANGER_TABLE_POINT + "." + G;
    public static final String TAP_CHANGER_TABLE_POINT_B = TAP_CHANGER_TABLE_POINT + "." + B;
    public static final String TAP_CHANGER_TABLE_POINT_STEP = TAP_CHANGER_TABLE_POINT + "." + STEP;
    public static final String TAP_CHANGER_TABLE_POINT_RATIO = TAP_CHANGER_TABLE_POINT + "." + RATIO;
    public static final String PHASE_TAP_CHANGER_TABLE_POINT_ANGLE = PHASE_TAP_CHANGER_TABLE_POINT + "." + ANGLE;
    public static final String PHASE_TAP_CHANGER_TABLE_POINT_PHASE_TAP_CHANGER_TABLE = PHASE_TAP_CHANGER_TABLE_POINT + "." + PHASE_TAP_CHANGER_TABLE;
    public static final String RATIO_TAP_CHANGER_TABLE_POINT_RATIO_TAP_CHANGER_TABLE = RATIO_TAP_CHANGER_TABLE_POINT + "." + RATIO_TAP_CHANGER_TABLE;
    public static final String TERMINAL_CONDUCTING_EQUIPMENT = TERMINAL + "." + CONDUCTING_EQUIPMENT;
    public static final String TERMINAL_CONNECTIVITY_NODE = TERMINAL + "." + CONNECTIVITY_NODE;
    public static final String TIE_FLOW_CONTROL_AREA = TIE_FLOW + "." + CONTROL_AREA;
    public static final String TIE_FLOW_TERMINAL = TIE_FLOW + "." + TERMINAL;
    public static final String TIE_FLOW_POSITIVE_FLOW_IN = TIE_FLOW + ".positiveFlowIn";
    public static final String VOLTAGE_LEVEL_LOW_VOLTAGE_LIMIT = VOLTAGE_LEVEL + "." + LOW_VOLTAGE_LIMIT;
    public static final String VOLTAGE_LEVEL_HIGH_VOLTAGE_LIMIT = VOLTAGE_LEVEL + "." + HIGH_VOLTAGE_LIMIT;
    public static final String VOLTAGE_LEVEL_SUBSTATION = VOLTAGE_LEVEL + "." + SUBSTATION;
    public static final String VOLTAGE_LEVEL_BASE_VOLTAGE = VOLTAGE_LEVEL + "." + BASE_VOLTAGE;

    private CgmesNames() {
    }
}
