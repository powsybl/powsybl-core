/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.iidm.impl {
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.iidm.extensions;
    requires transitive com.powsybl.math;
    requires transitive commons.math3; // No Automatic-Module-Name
    requires org.apache.commons.lang3;
    requires org.jgrapht.core;
    requires org.jgrapht.io;
    requires static org.jspecify;
    requires org.slf4j;
    requires transitive trove4j; // No Automatic-Module-Name

    exports com.powsybl.iidm.network.impl;
    exports com.powsybl.iidm.network.impl.extensions;

    provides com.powsybl.commons.extensions.ExtensionAdderProvider with
        com.powsybl.iidm.network.impl.extensions.ActivePowerControlAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.BatteryShortCircuitAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.BranchObservabilityAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.BusbarSectionPositionAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.ConnectablePositionAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.CoordinatedReactiveControlAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.DiscreteMeasurementsAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.DynamicModelInfoAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.GeneratorEntsoeCategoryAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.GeneratorFortescueAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.GeneratorShortCircuitAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.GeneratorStartupAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.HvdcAngleDroopActivePowerControlAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.HvdcOperatorActivePowerRangeAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.IdentifiableShortCircuitAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.InjectionObservabilityAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.LineCouplingsAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.LineFortescueAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.LinePositionAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.LoadAsymmetricalAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.LoadDetailAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.ManualFrequencyRestorationReserveAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.MeasurementsAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.ObservabilityAreaAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.OperatingStatusAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.ReferencePrioritiesAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.ReferenceTerminalsAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.SecondaryVoltageControlAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.SlackTerminalAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.StandbyAutomatonAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.SubstationPositionAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.ThreeWindingsTransformerFortescueAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.ThreeWindingsTransformerPhaseAngleClockAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.ThreeWindingsTransformerToBeEstimatedAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.TwoWindingsTransformerFortescueAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.TwoWindingsTransformerPhaseAngleClockAdderImplProvider,
        com.powsybl.iidm.network.impl.extensions.TwoWindingsTransformerToBeEstimatedAdderImplProvider;
    provides com.powsybl.iidm.network.NetworkFactoryService with
        com.powsybl.iidm.network.impl.NetworkFactoryServiceImpl;
}
