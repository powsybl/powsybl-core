package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.MergedDanglingLineAdder;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;

public class MergedDanglingLineXml {

    public static final void read(MergedDanglingLineAdder adder, NetworkXmlReaderContext context) throws XMLStreamException {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "name"));
        adder.setId(id)
                .setName(name);
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> {
            boolean fictitious = XmlUtil.readOptionalBoolAttribute(context.getReader(), "fictitious", false);
            adder.setFictitious(fictitious);
        });
        readElement(id, adder, context);
    }

    public static void readElement(String id, MergedDanglingLineAdder adder, NetworkXmlReaderContext context) throws XMLStreamException {
        DanglingLineXml.readRootElementAttributesInternal(adder, context);
        adder.add();
    }
}
