/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions.alternatives;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;
import com.powsybl.iidm.serde.extensions.AbstractVersionableNetworkExtensionSerDe;

import java.io.InputStream;
import java.util.List;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class LegacyActivePowerControlSerDe<T extends Injection<T>> extends AbstractVersionableNetworkExtensionSerDe<T, ActivePowerControl<T>> {

    public LegacyActivePowerControlSerDe() {
        super("legacyActivePowerControl", ActivePowerControl.class, "lapc",
                new ImmutableMap.Builder<IidmVersion, ImmutableSortedSet<String>>()
                        .put(IidmVersion.V_1_0, ImmutableSortedSet.of("1.0", "1.1"))
                        .build(),
                new ImmutableMap.Builder<String, String>()
                        .put("1.0", "http://www.itesla_project.eu/schema/iidm/ext/legacy_active_power_control/1_0")
                        .put("1.1", "http://www.powsybl.org/schema/iidm/ext/legacy_active_power_control/1_1")
                        .build());
    }

    @Override
    public void write(ActivePowerControl<T> activePowerControl, SerializerContext context) {
        context.getWriter().writeBooleanAttribute("legacyParticipate", activePowerControl.isParticipate());
        context.getWriter().writeDoubleAttribute("legacyDroop", activePowerControl.getDroop());
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        String extVersionStr = networkContext.getExtensionVersion(ActivePowerControl.NAME)
                .orElseGet(() -> getVersion(networkContext.getVersion()));
        if ("1.1".compareTo(extVersionStr) <= 0) {
            context.getWriter().writeDoubleAttribute("legacyParticipationFactor", activePowerControl.getParticipationFactor());
        }
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/legacyActivePowerControlV1_1.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return List.of(getClass().getResourceAsStream("/xsd/legacyActivePowerControlV1_1.xsd"),
                getClass().getResourceAsStream("/xsd/legacyActivePowerControlV1_0.xsd"));
    }

    @Override
    public ActivePowerControl<T> read(T identifiable, DeserializerContext context) {
        boolean participate = context.getReader().readBooleanAttribute("legacyParticipate");
        double droop = context.getReader().readDoubleAttribute("legacyDroop");
        double participationFactor = Double.NaN;
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        String extVersionStr = networkContext.getExtensionVersion(this).orElseThrow(IllegalStateException::new);
        if ("1.1".compareTo(extVersionStr) <= 0) {
            participationFactor = context.getReader().readDoubleAttribute("legacyParticipationFactor");
        }
        context.getReader().readEndNode();
        ActivePowerControlAdder<T> activePowerControlAdder = identifiable.newExtension(ActivePowerControlAdder.class);
        return activePowerControlAdder.withParticipate(participate)
                .withDroop(droop)
                .withParticipationFactor(participationFactor)
                .add();
    }
}
