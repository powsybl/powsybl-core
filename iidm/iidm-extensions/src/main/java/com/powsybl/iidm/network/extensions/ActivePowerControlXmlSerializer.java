/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Injection;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class ActivePowerControlXmlSerializer<T extends Injection> implements ExtensionXmlSerializer<T, ActivePowerControl<T>> {

    @Override
    public String getExtensionName() {
        return "activePowerControl";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super ActivePowerControl> getExtensionClass() {
        return ActivePowerControl.class;
    }

    @Override
    public boolean hasSubElements() {
        return false;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/activePowerControl.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.itesla_project.eu/schema/iidm/ext/active_power_control/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "apc";
    }

    @Override
    public void write(ActivePowerControl activePowerControl, XmlWriterContext context) throws XMLStreamException {
        context.getExtensionsWriter().writeAttribute("participate", Boolean.toString(activePowerControl.isParticipate()));
        XmlUtil.writeFloat("droop", activePowerControl.getDroop(), context.getExtensionsWriter());
    }

    @Override
    public ActivePowerControl<T> read(T identifiable, XmlReaderContext context) {
        boolean participate = XmlUtil.readBoolAttribute(context.getReader(), "participate");
        float droop = XmlUtil.readFloatAttribute(context.getReader(), "droop");
        return new ActivePowerControl<>(identifiable, participate, droop);
    }
}
