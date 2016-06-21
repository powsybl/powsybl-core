/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import com.google.auto.service.AutoService;
import eu.itesla_project.iidm.network.Load;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.test.LoadZipModel;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXml.class)
public class LoadZipModelXml implements ExtensionXml<Load, LoadZipModel> {

    @Override
    public String getExtensionName() {
        return "loadZipModel";
    }

    @Override
    public void write(LoadZipModel zipModel, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeEmptyElement("loadZipModel");
        context.getWriter().writeAttribute("id", zipModel.getIdentifiable().getId());
        IdentifiableXml.writeFloat("a1", zipModel.getA1(), context.getWriter());
        IdentifiableXml.writeFloat("a2", zipModel.getA2(), context.getWriter());
        IdentifiableXml.writeFloat("a3", zipModel.getA3(), context.getWriter());
        IdentifiableXml.writeFloat("a4", zipModel.getA4(), context.getWriter());
        IdentifiableXml.writeFloat("a5", zipModel.getA5(), context.getWriter());
        IdentifiableXml.writeFloat("a6", zipModel.getA6(), context.getWriter());
        IdentifiableXml.writeFloat("v0", zipModel.getV0(), context.getWriter());
    }

    @Override
    public void read(Network network, XMLStreamReader reader) {
        String id = reader.getAttributeValue(null, "id");
        Load load = network.getLoad(id);
        if (load == null) {
            throw new RuntimeException("Load " + id + " not found");
        }
        float a1 = IdentifiableXml.readFloatAttribute(reader, "a1");
        float a2 = IdentifiableXml.readFloatAttribute(reader, "a2");
        float a3 = IdentifiableXml.readFloatAttribute(reader, "a3");
        float a4 = IdentifiableXml.readFloatAttribute(reader, "a4");
        float a5 = IdentifiableXml.readFloatAttribute(reader, "a5");
        float a6 = IdentifiableXml.readFloatAttribute(reader, "a6");
        float v0 = IdentifiableXml.readFloatAttribute(reader, "v0");
        load.addExtension(LoadZipModel.class, new LoadZipModel(load, a1, a2, a3, a4, a5, a6, v0));
    }
}
