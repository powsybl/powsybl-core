/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.xml.util.IidmXmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class BusBreakerViewSwitchXml extends AbstractSwitchXml<VoltageLevel.BusBreakerView.SwitchAdder> {

    static final BusBreakerViewSwitchXml INSTANCE = new BusBreakerViewSwitchXml();

    @Override
    protected boolean isValid(Switch s, VoltageLevel vl) {
        VoltageLevel.BusBreakerView v = vl.getBusBreakerView();
        if (v.getBus1(s.getId()).getId().equals(v.getBus2(s.getId()).getId())) {
            LOGGER.warn("Discard switch with same bus at both ends. Id: {}", s.getId());
            return false;
        }
        return true;
    }

    @Override
    protected void writeRootElementAttributes(Switch s, VoltageLevel vl, NetworkXmlWriterContext context) {
        super.writeRootElementAttributes(s, vl, context);
        VoltageLevel.BusBreakerView v = vl.getBusBreakerView();
        Bus bus1 = v.getBus1(s.getId());
        Bus bus2 = v.getBus2(s.getId());
        context.getWriter().writeStringAttribute("bus1", context.getAnonymizer().anonymizeString(bus1.getId()));
        context.getWriter().writeStringAttribute("bus2", context.getAnonymizer().anonymizeString(bus2.getId()));
    }

    @Override
    protected VoltageLevel.BusBreakerView.SwitchAdder createAdder(VoltageLevel vl) {
        return vl.getBusBreakerView().newSwitch();
    }

    @Override
    protected Switch readRootElementAttributes(VoltageLevel.BusBreakerView.SwitchAdder adder, VoltageLevel voltageLevel, NetworkXmlReaderContext context) {
        context.getReader().readEnumAttribute("kind", SwitchKind.class);
        context.getReader().readBooleanAttribute("retained");
        boolean open = context.getReader().readBooleanAttribute("open");
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> {
            boolean fictitious = context.getReader().readBooleanAttribute("fictitious", false);
            adder.setFictitious(fictitious);
        });
        String bus1 = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("bus1"));
        String bus2 = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("bus2"));
        if (bus1.equals(bus2) && context.getVersion().compareTo(IidmXmlVersion.V_1_8) < 0) {
            // Discard switches with same bus at both ends instead of throwing exception in adder to support old xiidm files
            LOGGER.warn("Discard switch with same bus {} at both ends", bus1);
            return null;
        }
        return adder.setOpen(open)
                .setBus1(bus1)
                .setBus2(bus2)
                .add();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BusBreakerViewSwitchXml.class);
}
