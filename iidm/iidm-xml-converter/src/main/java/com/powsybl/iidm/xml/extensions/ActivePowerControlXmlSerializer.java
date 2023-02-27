/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import java.io.InputStream;
import java.util.List;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class ActivePowerControlXmlSerializer<T extends Injection<T>> extends AbstractVersionableNetworkExtensionXmlSerializer<T, ActivePowerControl<T>> {

    public ActivePowerControlXmlSerializer() {
        super("activePowerControl", ActivePowerControl.class, "apc",
                new ImmutableMap.Builder<IidmXmlVersion, ImmutableSortedSet<String>>()
                        .put(IidmXmlVersion.V_1_3, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmXmlVersion.V_1_4, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmXmlVersion.V_1_5, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmXmlVersion.V_1_6, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmXmlVersion.V_1_7, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmXmlVersion.V_1_8, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmXmlVersion.V_1_9, ImmutableSortedSet.of("1.0", "1.1"))
                        .build(),
                new ImmutableMap.Builder<String, String>()
                        .put("1.0", "http://www.itesla_project.eu/schema/iidm/ext/active_power_control/1_0")
                        .put("1.1", "http://www.powsybl.org/schema/iidm/ext/active_power_control/1_1")
                        .build());
    }

    @Override
    public void write(ActivePowerControl<T> activePowerControl, XmlWriterContext context) {
            context.getWriter().writeBooleanAttribute("participate", activePowerControl.isParticipate());
            context.getWriter().writeDoubleAttribute("droop", activePowerControl.getDroop());
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        String extVersionStr = networkContext.getExtensionVersion(ConnectablePosition.NAME)
                .orElseGet(() -> getVersion(networkContext.getVersion()));
        if ("1.1".compareTo(extVersionStr) <= 0) {
            context.getWriter().writeDoubleAttribute("participationFactor", activePowerControl.getParticipationFactor());
        }
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/activePowerControl_V1_1.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return List.of(getClass().getResourceAsStream("/xsd/activePowerControl_V1_1.xsd"),
                getClass().getResourceAsStream("/xsd/activePowerControl_V1_0.xsd"));
    }

    @Override
    public ActivePowerControl<T> read(T identifiable, XmlReaderContext context) {
        boolean participate = context.getReader().readBooleanAttribute("participate");
        float droop = context.getReader().readFloatAttribute("droop");
        double participationFactor = Double.NaN;
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        String extVersionStr = networkContext.getExtensionVersion(this).orElseThrow(AssertionError::new);
        if ("1.1".compareTo(extVersionStr) <= 0) {
            participationFactor = context.getReader().readDoubleAttribute("participationFactor", 0.0);
        }
        ActivePowerControlAdder<T> activePowerControlAdder = identifiable.newExtension(ActivePowerControlAdder.class);
        return activePowerControlAdder.withParticipate(participate)
                .withDroop(droop)
                .withParticipationFactor(participationFactor)
                .add();
    }
}
