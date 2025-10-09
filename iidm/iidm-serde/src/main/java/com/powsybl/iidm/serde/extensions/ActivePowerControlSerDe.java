/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.serde.IidmVersion;

/**
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class ActivePowerControlSerDe<T extends Injection<T>> extends AbstractVersionableNetworkExtensionSerDe<T, ActivePowerControl<T>, ActivePowerControlSerDe.Version> {

    public enum Version implements SerDeVersion<Version> {
        V_1_0("/xsd/activePowerControl_V1_0.xsd", "http://www.itesla_project.eu/schema/iidm/ext/active_power_control/1_0",
                new VersionNumbers(1, 0), IidmVersion.V_1_0, IidmVersion.V_1_13),
        V_1_1("/xsd/activePowerControl_V1_1.xsd", "http://www.powsybl.org/schema/iidm/ext/active_power_control/1_1",
                new VersionNumbers(1, 1), IidmVersion.V_1_0, IidmVersion.V_1_13),
        V_1_2("/xsd/activePowerControl_V1_2.xsd", "http://www.powsybl.org/schema/iidm/ext/active_power_control/1_2",
                new VersionNumbers(1, 2), IidmVersion.V_1_13, null);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "apc", versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, ActivePowerControl.NAME);
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public ActivePowerControlSerDe() {
        super(ActivePowerControl.NAME, ActivePowerControl.class, Version.values());
    }

    @Override
    public void write(ActivePowerControl<T> activePowerControl, SerializerContext context) {
        context.getWriter().writeBooleanAttribute("participate", activePowerControl.isParticipate());
        context.getWriter().writeDoubleAttribute("droop", activePowerControl.getDroop());
        Version extVersion = getExtensionVersionToExport(context);
        if (extVersion.isGreaterThan(Version.V_1_0)) {
            context.getWriter().writeDoubleAttribute("participationFactor", activePowerControl.getParticipationFactor());
        }
        if (extVersion.isGreaterThan(Version.V_1_1)) {
            // not using writeOptionalDouble and trusting implementation convention: : writeDoubleAttribute does not write NaN values in human-readable formats JSON/XML
            context.getWriter().writeDoubleAttribute("maxTargetP", activePowerControl.getMaxTargetP().orElse(Double.NaN));
            context.getWriter().writeDoubleAttribute("minTargetP", activePowerControl.getMinTargetP().orElse(Double.NaN));
        }
    }

    @Override
    public ActivePowerControl<T> read(T identifiable, DeserializerContext context) {
        boolean participate = context.getReader().readBooleanAttribute("participate");
        double droop = context.getReader().readDoubleAttribute("droop");
        double participationFactor = Double.NaN;
        double minTargetP = Double.NaN;
        double maxTargetP = Double.NaN;
        Version extVersion = getExtensionVersionImported(context);
        if (extVersion.isGreaterThan(Version.V_1_0)) {
            participationFactor = context.getReader().readDoubleAttribute("participationFactor");
        }
        if (extVersion.isGreaterThan(Version.V_1_1)) {
            // not using readOptionalDouble and trusting implementation convention: readDoubleAttribute returns Nan if attribute is absent in human-readable formats (JSON / XML)
            maxTargetP = context.getReader().readDoubleAttribute("maxTargetP");
            minTargetP = context.getReader().readDoubleAttribute("minTargetP");
        }
        context.getReader().readEndNode();
        ActivePowerControlAdder<T> activePowerControlAdder = identifiable.newExtension(ActivePowerControlAdder.class);
        return activePowerControlAdder.withParticipate(participate)
                .withDroop(droop)
                .withParticipationFactor(participationFactor)
                .withMinTargetP(minTargetP)
                .withMaxTargetP(maxTargetP)
                .add();
    }
}
