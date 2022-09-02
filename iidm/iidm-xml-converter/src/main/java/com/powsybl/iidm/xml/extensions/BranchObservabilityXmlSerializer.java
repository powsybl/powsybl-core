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
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.extensions.BranchObservability;
import com.powsybl.iidm.network.extensions.BranchObservabilityAdder;
import com.powsybl.iidm.network.extensions.ObservabilityQuality;

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
        writeOptionalQuality(context, branchObservability.getQualityP1(), QUALITY_P, Side.ONE);

        // qualityP2
        writeOptionalQuality(context, branchObservability.getQualityP2(), QUALITY_P, Side.TWO);

        // qualityQ1
        writeOptionalQuality(context, branchObservability.getQualityQ1(), QUALITY_Q, Side.ONE);

        // qualityQ2
        writeOptionalQuality(context, branchObservability.getQualityQ2(), QUALITY_Q, Side.TWO);
    }

    private void writeOptionalQuality(XmlWriterContext context, ObservabilityQuality<T> quality, String type, Side side) throws XMLStreamException {
        if (quality == null) {
            return;
        }
        context.getWriter().writeEmptyElement(getNamespaceUri(), type);
        context.getWriter().writeAttribute(SIDE, side.name());
        XmlUtil.writeDouble(STANDARD_DEVIATION, quality.getStandardDeviation(), context.getWriter());
        XmlUtil.writeOptionalBoolean(REDUNDANT, quality.isRedundant(), false, context.getWriter());
    }

    @Override
    public BranchObservability<T> read(T identifiable, XmlReaderContext context) throws XMLStreamException {
        boolean observable = XmlUtil.readOptionalBoolAttribute(context.getReader(), "observable", false);

        BranchObservabilityAdder<T> adder = identifiable.newExtension(BranchObservabilityAdder.class)
                .withObservable(observable);

        XmlUtil.readUntilEndElement(getExtensionName(), context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case QUALITY_P: {
                    var side = Side.valueOf(context.getReader().getAttributeValue(null, SIDE));
                    var standardDeviation = XmlUtil.readDoubleAttribute(context.getReader(), STANDARD_DEVIATION);
                    var redundant = context.getReader().getAttributeValue(null, REDUNDANT);
                    readQualityP(standardDeviation, redundant, side, adder);
                    break;
                }
                case QUALITY_Q: {
                    var side = Side.valueOf(context.getReader().getAttributeValue(null, SIDE));
                    var standardDeviation = XmlUtil.readDoubleAttribute(context.getReader(), STANDARD_DEVIATION);
                    var redundant = context.getReader().getAttributeValue(null, REDUNDANT);
                    readQualityQ(standardDeviation, redundant, side, adder);
                    break;
                }
                default: {
                    throw new PowsyblException("Unexpected element: " + context.getReader().getLocalName());
                }
            }
        });

        return adder.add();
    }

    private void readQualityP(double standardDeviation, String redundant, Side side, BranchObservabilityAdder<T> adder) {
        if (side == Side.ONE) {
            adder.withStandardDeviationP1(standardDeviation);
            if (redundant != null) {
                adder.withRedundantP1(Boolean.parseBoolean(redundant));
            }
        } else if (side == Side.TWO) {
            adder.withStandardDeviationP2(standardDeviation);
            if (redundant != null) {
                adder.withRedundantP2(Boolean.parseBoolean(redundant));
            }
        }
    }

    private void readQualityQ(double standardDeviation, String redundant, Side side, BranchObservabilityAdder<T> adder) {
        if (side == Side.ONE) {
            adder.withStandardDeviationQ1(standardDeviation);
            if (redundant != null) {
                adder.withRedundantQ1(Boolean.parseBoolean(redundant));
            }
        } else if (side == Side.TWO) {
            adder.withStandardDeviationQ2(standardDeviation);
            if (redundant != null) {
                adder.withRedundantQ2(Boolean.parseBoolean(redundant));
            }
        }
    }
}
