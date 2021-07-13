/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.extensions.BranchObservability;
import com.powsybl.iidm.network.extensions.BranchObservabilityAdder;

import javax.xml.stream.XMLStreamException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
@AutoService(ExtensionXmlSerializer.class)
public class BranchObservabilityXmlSerializer<T extends Branch<T>> extends AbstractExtensionXmlSerializer<T, BranchObservability<T>> {

    private static final String QUALITY_P = "qualityP";
    private static final String QUALITY_Q = "qualityQ";
    private static final String QUALITY_V = "qualityV";

    private static final String SIDE = "side";
    private static final String STANDARD_DEVIATION = "standardDeviation";
    private static final String REDUNDANT = "redundant";

    public BranchObservabilityXmlSerializer() {
        super("branchObservability", "network", BranchObservability.class, true, "branchObservability.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/branch_observability/1_0", "bo");
    }

    @Override
    public void write(BranchObservability<T> branchObservability, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("observable", Boolean.toString(branchObservability.isObservable()));

        // qualityP
        // ONE
        context.getWriter().writeEmptyElement(getNamespaceUri(), QUALITY_P);
        context.getWriter().writeAttribute(SIDE, Branch.Side.ONE.name());
        XmlUtil.writeFloat(STANDARD_DEVIATION, branchObservability.getStandardDeviationP(Branch.Side.ONE), context.getWriter());
        context.getWriter().writeAttribute(REDUNDANT, Boolean.toString(branchObservability.isRedundantP(Branch.Side.ONE)));
        // TWO
        context.getWriter().writeEmptyElement(getNamespaceUri(), QUALITY_P);
        context.getWriter().writeAttribute(SIDE, Branch.Side.TWO.name());
        XmlUtil.writeFloat(STANDARD_DEVIATION, branchObservability.getStandardDeviationP(Branch.Side.TWO), context.getWriter());
        context.getWriter().writeAttribute(REDUNDANT, Boolean.toString(branchObservability.isRedundantP(Branch.Side.TWO)));

        // qualityQ
        // ONE
        context.getWriter().writeEmptyElement(getNamespaceUri(), QUALITY_Q);
        context.getWriter().writeAttribute(SIDE, Branch.Side.ONE.name());
        XmlUtil.writeFloat(STANDARD_DEVIATION, branchObservability.getStandardDeviationQ(Branch.Side.ONE), context.getWriter());
        context.getWriter().writeAttribute(REDUNDANT, Boolean.toString(branchObservability.isRedundantQ(Branch.Side.ONE)));
        // TWO
        context.getWriter().writeEmptyElement(getNamespaceUri(), QUALITY_Q);
        context.getWriter().writeAttribute(SIDE, Branch.Side.TWO.name());
        XmlUtil.writeFloat(STANDARD_DEVIATION, branchObservability.getStandardDeviationQ(Branch.Side.TWO), context.getWriter());
        context.getWriter().writeAttribute(REDUNDANT, Boolean.toString(branchObservability.isRedundantQ(Branch.Side.TWO)));

        // qualityV
        // ONE
        context.getWriter().writeEmptyElement(getNamespaceUri(), QUALITY_V);
        context.getWriter().writeAttribute(SIDE, Branch.Side.ONE.name());
        XmlUtil.writeFloat(STANDARD_DEVIATION, branchObservability.getStandardDeviationV(Branch.Side.ONE), context.getWriter());
        context.getWriter().writeAttribute(REDUNDANT, Boolean.toString(branchObservability.isRedundantV(Branch.Side.ONE)));
        // TWO
        context.getWriter().writeEmptyElement(getNamespaceUri(), QUALITY_V);
        context.getWriter().writeAttribute(SIDE, Branch.Side.TWO.name());
        XmlUtil.writeFloat(STANDARD_DEVIATION, branchObservability.getStandardDeviationV(Branch.Side.TWO), context.getWriter());
        context.getWriter().writeAttribute(REDUNDANT, Boolean.toString(branchObservability.isRedundantV(Branch.Side.TWO)));
    }

    @Override
    public BranchObservability<T> read(T identifiable, XmlReaderContext context) throws XMLStreamException {
        boolean observable = XmlUtil.readBoolAttribute(context.getReader(), "observable");

        BranchObservabilityAdder<T> adder = identifiable.newExtension(BranchObservabilityAdder.class)
                .withObservable(observable);

        XmlUtil.readUntilEndElement(getExtensionName(), context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case QUALITY_P: {
                    var side = Branch.Side.valueOf(context.getReader().getAttributeValue(null, SIDE));
                    var standardDeviation = XmlUtil.readFloatAttribute(context.getReader(), STANDARD_DEVIATION);
                    var redundant = XmlUtil.readBoolAttribute(context.getReader(), REDUNDANT);
                    adder.withStandardDeviationP(standardDeviation, side)
                            .withRedundantP(redundant, side);
                    break;
                }
                case QUALITY_Q: {
                    var side = Branch.Side.valueOf(context.getReader().getAttributeValue(null, SIDE));
                    var standardDeviation = XmlUtil.readFloatAttribute(context.getReader(), STANDARD_DEVIATION);
                    var redundant = XmlUtil.readBoolAttribute(context.getReader(), REDUNDANT);
                    adder.withStandardDeviationQ(standardDeviation, side)
                            .withRedundantQ(redundant, side);
                    break;
                }
                case QUALITY_V: {
                    var side = Branch.Side.valueOf(context.getReader().getAttributeValue(null, SIDE));
                    var standardDeviation = XmlUtil.readFloatAttribute(context.getReader(), STANDARD_DEVIATION);
                    var redundant = XmlUtil.readBoolAttribute(context.getReader(), REDUNDANT);
                    adder.withStandardDeviationV(standardDeviation, side)
                            .withRedundantV(redundant, side);
                    break;
                }
                default: {
                    throw new PowsyblException("Unexpected element: " + context.getReader().getLocalName());
                }
            }
        });

        adder.add();
        return identifiable.getExtension(BranchObservability.class);
    }
}
