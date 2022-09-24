/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.TreeDataReader;
import com.powsybl.commons.xml.TreeDataWriter;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesTapChangersXmlSerializer<C extends Connectable<C>> extends AbstractExtensionXmlSerializer<C, CgmesTapChangers<C>> {

    public CgmesTapChangersXmlSerializer() {
        super("cgmesTapChangers", "network", CgmesTapChangers.class,
                true, "cgmesTapChangers.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_tap_changers/1_0", "ctc");
    }

    @Override
    public void write(CgmesTapChangers<C> extension, XmlWriterContext context) {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        TreeDataWriter writer = networkContext.getWriter();
        for (CgmesTapChanger tapChanger : extension.getTapChangers()) {
            writer.writeStartNode(getNamespaceUri(), "tapChanger");
            writer.writeStringAttribute("id", tapChanger.getId());
            writer.writeStringAttribute("combinedTapChangerId", tapChanger.getCombinedTapChangerId());
            writer.writeStringAttribute("type", tapChanger.getType());
            if (tapChanger.isHidden()) {
                writer.writeBooleanAttribute("hidden", true);
                writer.writeIntAttribute("step", tapChanger.getStep()
                        .orElseThrow(() -> new PowsyblException("Step should be defined")));
            }
            writer.writeStringAttribute("controlId", tapChanger.getControlId());
            writer.writeEndNode();
        }
    }

    @Override
    public CgmesTapChangers<C> read(C extendable, XmlReaderContext context) {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        TreeDataReader reader = networkContext.getReader();
        extendable.newExtension(CgmesTapChangersAdder.class).add();
        CgmesTapChangers<C> tapChangers = extendable.getExtension(CgmesTapChangers.class);
        reader.readUntilEndNode(getExtensionName(), () -> {
            if (reader.getNodeName().equals("tapChanger")) {
                CgmesTapChangerAdder adder = tapChangers.newTapChanger()
                        .setId(reader.readStringAttribute("id"))
                        .setCombinedTapChangerId(reader.readStringAttribute("combinedTapChangerId"))
                        .setType(reader.readStringAttribute("type"))
                        .setHiddenStatus(reader.readBooleanAttribute("hidden", false))
                        .setControlId(reader.readStringAttribute("controlId"));
                Integer step = reader.readIntAttribute("step");
                if (step != null) {
                    adder.setStep(step);
                }
                adder.add();
            } else {
                throw new PowsyblException("Unknown element name <" + reader.getNodeName() + "> in <cgmesTapChangers>");
            }
        });
        return extendable.getExtension(CgmesTapChangers.class);
    }
}
