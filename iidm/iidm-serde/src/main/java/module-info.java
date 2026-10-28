/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.iidm.serde {
    requires com.fasterxml.jackson.core;
    requires com.github.luben.zstd_jni;
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.iidm.extensions;
    requires java.xml;
    requires org.apache.commons.lang3;
    requires static org.jspecify;
    requires org.slf4j;

    exports com.powsybl.iidm.serde;
    exports com.powsybl.iidm.serde.anonymizer;
    exports com.powsybl.iidm.serde.extensions;
    exports com.powsybl.iidm.serde.extensions.util;
    exports com.powsybl.iidm.serde.test;
    exports com.powsybl.iidm.serde.util;

    uses com.powsybl.iidm.serde.extensions.ExtinctExtensionSerDe;

    provides com.powsybl.commons.extensions.ExtensionSerDe with
        com.powsybl.iidm.serde.extensions.ActivePowerControlSerDe,
        com.powsybl.iidm.serde.extensions.BatteryShortCircuitSerDe,
        com.powsybl.iidm.serde.extensions.BranchObservabilitySerDe,
        com.powsybl.iidm.serde.extensions.BranchStatusSerDe,
        com.powsybl.iidm.serde.extensions.BusbarSectionPositionSerDe,
        com.powsybl.iidm.serde.extensions.ConnectablePositionSerDe,
        com.powsybl.iidm.serde.extensions.CoordinatedReactiveControlSerDe,
        com.powsybl.iidm.serde.extensions.DiscreteMeasurementsSerDe,
        com.powsybl.iidm.serde.extensions.GeneratorEntsoeCategorySerDe,
        com.powsybl.iidm.serde.extensions.GeneratorFortescueSerDe,
        com.powsybl.iidm.serde.extensions.GeneratorShortCircuitSerDe,
        com.powsybl.iidm.serde.extensions.GeneratorStartupSerDe,
        com.powsybl.iidm.serde.extensions.HvdcAngleDroopActivePowerControlSerDe,
        com.powsybl.iidm.serde.extensions.HvdcOperatorActivePowerRangeSerDe,
        com.powsybl.iidm.serde.extensions.IdentifiableShortCircuitSerDe,
        com.powsybl.iidm.serde.extensions.InjectionObservabilitySerDe,
        com.powsybl.iidm.serde.extensions.LineCouplingsSerDe,
        com.powsybl.iidm.serde.extensions.LineFortescueSerDe,
        com.powsybl.iidm.serde.extensions.LinePositionSerDe,
        com.powsybl.iidm.serde.extensions.LoadAsymmetricalSerDe,
        com.powsybl.iidm.serde.extensions.LoadDetailSerDe,
        com.powsybl.iidm.serde.extensions.ManualFrequencyRestorationReserveSerDe,
        com.powsybl.iidm.serde.extensions.MeasurementsSerDe,
        com.powsybl.iidm.serde.extensions.ObservabilityAreaSerDe,
        com.powsybl.iidm.serde.extensions.OperatingStatusSerDe,
        com.powsybl.iidm.serde.extensions.ReferencePrioritiesSerDe,
        com.powsybl.iidm.serde.extensions.ReferenceTerminalsSerDe,
        com.powsybl.iidm.serde.extensions.RemoteReactivePowerControlSerDe,
        com.powsybl.iidm.serde.extensions.SecondaryVoltageControlSerDe,
        com.powsybl.iidm.serde.extensions.SlackTerminalSerDe,
        com.powsybl.iidm.serde.extensions.StandbyAutomatonSerDe,
        com.powsybl.iidm.serde.extensions.SubstationPositionSerDe,
        com.powsybl.iidm.serde.extensions.ThreeWindingsTransformerFortescueSerDe,
        com.powsybl.iidm.serde.extensions.ThreeWindingsTransformerPhaseAngleClockSerDe,
        com.powsybl.iidm.serde.extensions.ThreeWindingsTransformerToBeEstimatedSerDe,
        com.powsybl.iidm.serde.extensions.TwoWindingsTransformerFortescueSerDe,
        com.powsybl.iidm.serde.extensions.TwoWindingsTransformerPhaseAngleClockSerDe,
        com.powsybl.iidm.serde.extensions.TwoWindingsTransformerToBeEstimatedSerDe,
        com.powsybl.iidm.serde.extensions.VoltagePerReactivePowerControlSerDe,
        com.powsybl.iidm.serde.extensions.VoltageRegulationSerDe;
    provides com.powsybl.iidm.network.Exporter with
        com.powsybl.iidm.serde.BinaryExporter,
        com.powsybl.iidm.serde.JsonExporter,
        com.powsybl.iidm.serde.XMLExporter;
    provides com.powsybl.iidm.network.Importer with
        com.powsybl.iidm.serde.BinaryImporter,
        com.powsybl.iidm.serde.JsonImporter,
        com.powsybl.iidm.serde.XMLImporter;
    provides com.powsybl.iidm.serde.extensions.ExtinctExtensionSerDe with
        com.powsybl.iidm.serde.extensions.RemoteReactivePowerControlSerDe,
        com.powsybl.iidm.serde.extensions.VoltagePerReactivePowerControlSerDe,
        com.powsybl.iidm.serde.extensions.VoltageRegulationSerDe;
}
