/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;

import java.io.InputStream;
import java.util.List;

/**
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class ActivePowerControlSerDe<T extends Injection<T>> extends AbstractVersionableNetworkExtensionSerDe<T, ActivePowerControl<T>> {

    public ActivePowerControlSerDe() {
        super("activePowerControl", ActivePowerControl.class, "apc",
                new ImmutableMap.Builder<IidmVersion, ImmutableSortedSet<String>>()
                        .put(IidmVersion.V_1_0, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmVersion.V_1_1, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmVersion.V_1_2, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmVersion.V_1_3, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmVersion.V_1_4, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmVersion.V_1_5, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmVersion.V_1_6, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmVersion.V_1_7, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmVersion.V_1_8, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmVersion.V_1_9, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmVersion.V_1_10, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmVersion.V_1_11, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmVersion.V_1_12, ImmutableSortedSet.of("1.0", "1.1", "1.2"))
                        .build(),
                new ImmutableMap.Builder<String, String>()
                        .put("1.0", "http://www.itesla_project.eu/schema/iidm/ext/active_power_control/1_0")
                        .put("1.1", "http://www.powsybl.org/schema/iidm/ext/active_power_control/1_1")
                        .put("1.2", "http://www.powsybl.org/schema/iidm/ext/active_power_control/1_2")
                        .build());
    }

    @Override
    public void write(ActivePowerControl<T> activePowerControl, SerializerContext context) {
        context.getWriter().writeBooleanAttribute("participate", activePowerControl.isParticipate());
        context.getWriter().writeDoubleAttribute("droop", activePowerControl.getDroop());
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        String extVersionStr = networkContext.getExtensionVersion(ConnectablePosition.NAME)
                .orElseGet(() -> getVersion(networkContext.getVersion()));
        if ("1.1".compareTo(extVersionStr) <= 0) {
            context.getWriter().writeDoubleAttribute("participationFactor", activePowerControl.getParticipationFactor());
        }
        if ("1.2".compareTo(extVersionStr) <= 0) {
            context.getWriter().writeOptionalDoubleAttribute("maxPOverride", activePowerControl.getMaxPOverride().orElse(null));
            context.getWriter().writeOptionalDoubleAttribute("minPOverride", activePowerControl.getMinPOverride().orElse(null));
        }
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/activePowerControl_V1_2.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return List.of(getClass().getResourceAsStream("/xsd/activePowerControl_V1_2.xsd"),
                getClass().getResourceAsStream("/xsd/activePowerControl_V1_1.xsd"),
                getClass().getResourceAsStream("/xsd/activePowerControl_V1_0.xsd"));
    }

    @Override
    public ActivePowerControl<T> read(T identifiable, DeserializerContext context) {
        boolean participate = context.getReader().readBooleanAttribute("participate");
        double droop = context.getReader().readDoubleAttribute("droop");
        double participationFactor = Double.NaN;
        double minPOverride = Double.NaN;
        double maxPOverride = Double.NaN;
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        String extVersionStr = networkContext.getExtensionVersion(this).orElseThrow(IllegalStateException::new);
        if ("1.1".compareTo(extVersionStr) <= 0) {
            participationFactor = context.getReader().readDoubleAttribute("participationFactor");
        }
        if ("1.2".compareTo(extVersionStr) <= 0) {
            maxPOverride = context.getReader().readOptionalDoubleAttribute("maxPOverride").orElse(Double.NaN);
            minPOverride = context.getReader().readOptionalDoubleAttribute("minPOverride").orElse(Double.NaN);
        }
        context.getReader().readEndNode();
        ActivePowerControlAdder<T> activePowerControlAdder = identifiable.newExtension(ActivePowerControlAdder.class);
        return activePowerControlAdder.withParticipate(participate)
                .withDroop(droop)
                .withParticipationFactor(participationFactor)
                .withMinPOverride(minPOverride)
                .withMaxPOverride(maxPOverride)
                .add();
    }
}
