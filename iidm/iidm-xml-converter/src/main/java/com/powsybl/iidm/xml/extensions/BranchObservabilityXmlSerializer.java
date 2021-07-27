/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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

    private static final String SIDE = "side";
    private static final String STANDARD_DEVIATION = "standardDeviation";
    private static final String REDUNDANT = "redundant";

    public BranchObservabilityXmlSerializer() {
        super("branchObservability", "network", BranchObservability.class, true, "branchObservability.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/branch_observability/1_0", "bo");
    }

    @Override
    public void write(BranchObservability<T> branchObservability, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeOptionalBoolean("observable", branchObservability.isObservable(), false, context.getWriter());

        // qualityP1
        context.getWriter().writeEmptyElement(getNamespaceUri(), QUALITY_P);
        context.getWriter().writeAttribute(SIDE, Branch.Side.ONE.name());
        XmlUtil.writeDouble(STANDARD_DEVIATION, branchObservability.getQualityP1().getStandardDeviation(), context.getWriter());
        XmlUtil.writeOptionalBoolean(REDUNDANT, branchObservability.getQualityP1().isRedundant(), false, context.getWriter());
        // qualityP2
        context.getWriter().writeEmptyElement(getNamespaceUri(), QUALITY_P);
        context.getWriter().writeAttribute(SIDE, Branch.Side.TWO.name());
        XmlUtil.writeDouble(STANDARD_DEVIATION, branchObservability.getQualityP2().getStandardDeviation(), context.getWriter());
        XmlUtil.writeOptionalBoolean(REDUNDANT, branchObservability.getQualityP2().isRedundant(), false, context.getWriter());

        // qualityQ1
        context.getWriter().writeEmptyElement(getNamespaceUri(), QUALITY_Q);
        context.getWriter().writeAttribute(SIDE, Branch.Side.ONE.name());
        XmlUtil.writeDouble(STANDARD_DEVIATION, branchObservability.getQualityQ1().getStandardDeviation(), context.getWriter());
        XmlUtil.writeOptionalBoolean(REDUNDANT, branchObservability.getQualityQ1().isRedundant(), false, context.getWriter());
        // qualityQ2
        context.getWriter().writeEmptyElement(getNamespaceUri(), QUALITY_Q);
        context.getWriter().writeAttribute(SIDE, Branch.Side.TWO.name());
        XmlUtil.writeDouble(STANDARD_DEVIATION, branchObservability.getQualityQ2().getStandardDeviation(), context.getWriter());
        XmlUtil.writeOptionalBoolean(REDUNDANT, branchObservability.getQualityQ2().isRedundant(), false, context.getWriter());
    }

    @Override
    public BranchObservability<T> read(T identifiable, XmlReaderContext context) throws XMLStreamException {
        boolean observable = XmlUtil.readOptionalBoolAttribute(context.getReader(), "observable", false);

        BranchObservabilityAdder<T> adder = identifiable.newExtension(BranchObservabilityAdder.class)
                .withObservable(observable);

        XmlUtil.readUntilEndElement(getExtensionName(), context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case QUALITY_P: {
                    var side = Branch.Side.valueOf(context.getReader().getAttributeValue(null, SIDE));
                    var standardDeviation = XmlUtil.readDoubleAttribute(context.getReader(), STANDARD_DEVIATION);
                    var redundant = XmlUtil.readBoolAttribute(context.getReader(), REDUNDANT);
                    readQualityP(standardDeviation, redundant, side, adder);
                    break;
                }
                case QUALITY_Q: {
                    var side = Branch.Side.valueOf(context.getReader().getAttributeValue(null, SIDE));
                    var standardDeviation = XmlUtil.readDoubleAttribute(context.getReader(), STANDARD_DEVIATION);
                    var redundant = XmlUtil.readBoolAttribute(context.getReader(), REDUNDANT);
                    readQualityQ(standardDeviation, redundant, side, adder);
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

    private void readQualityP(double standardDeviation, boolean redundant, Branch.Side side, BranchObservabilityAdder<T> adder) {
        if (side == Branch.Side.ONE) {
            adder.withStandardDeviationP1(standardDeviation)
                    .withRedundantP1(redundant);
        } else if (side == Branch.Side.TWO) {
            adder.withStandardDeviationP2(standardDeviation)
                    .withRedundantP2(redundant);
        }
    }

    private void readQualityQ(double standardDeviation, boolean redundant, Branch.Side side, BranchObservabilityAdder<T> adder) {
        if (side == Branch.Side.ONE) {
            adder.withStandardDeviationQ1(standardDeviation)
                    .withRedundantQ1(redundant);
        } else if (side == Branch.Side.TWO) {
            adder.withStandardDeviationQ2(standardDeviation)
                    .withRedundantQ2(redundant);
        }
    }
}
