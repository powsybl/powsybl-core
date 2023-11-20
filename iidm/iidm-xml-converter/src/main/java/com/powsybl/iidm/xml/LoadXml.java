/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import java.util.List;
import java.util.function.Consumer;

import static com.powsybl.iidm.xml.ConnectableXmlUtil.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LoadXml extends AbstractComplexIdentifiableXml<Load, LoadAdder, VoltageLevel> {

    static final LoadXml INSTANCE = new LoadXml();

    static final String ROOT_ELEMENT_NAME = "load";
    static final String ARRAY_ELEMENT_NAME = "loads";
    public static final String MODEL = "model";
    private static final String EXPONENTIAL_MODEL = "exponentialModel";
    private static final String ZIP_MODEL = "zipModel";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(Load l, VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeEnumAttribute("loadType", l.getLoadType());
        context.getWriter().writeDoubleAttribute("p0", l.getP0());
        context.getWriter().writeDoubleAttribute("q0", l.getQ0());
        writeNodeOrBus(null, l.getTerminal(), context);
        writePQ(null, l.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(Load load, VoltageLevel parent, NetworkXmlWriterContext context) {
        load.getModel().ifPresent(model -> {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, MODEL, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_10, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_10, context, () -> writeModel(model, context));
        });
    }

    private void writeModel(LoadModel model, NetworkXmlWriterContext context) {
        switch (model.getType()) {
            case EXPONENTIAL:
                ExponentialLoadModel expModel = (ExponentialLoadModel) model;
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), EXPONENTIAL_MODEL);
                context.getWriter().writeDoubleAttribute("np", expModel.getNp());
                context.getWriter().writeDoubleAttribute("nq", expModel.getNq());
                context.getWriter().writeEndNode();
                break;
            case ZIP:
                ZipLoadModel zipModel = (ZipLoadModel) model;
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ZIP_MODEL);
                context.getWriter().writeDoubleAttribute("c0p", zipModel.getC0p());
                context.getWriter().writeDoubleAttribute("c1p", zipModel.getC1p());
                context.getWriter().writeDoubleAttribute("c2p", zipModel.getC2p());
                context.getWriter().writeDoubleAttribute("c0q", zipModel.getC0q());
                context.getWriter().writeDoubleAttribute("c1q", zipModel.getC1q());
                context.getWriter().writeDoubleAttribute("c2q", zipModel.getC2q());
                context.getWriter().writeEndNode();
                break;
            default:
                throw new PowsyblException("Unexpected load model type: " + model.getType());
        }
    }

    @Override
    protected LoadAdder createAdder(VoltageLevel vl) {
        return vl.newLoad();
    }

    @Override
    protected void readRootElementAttributes(LoadAdder adder, List<Consumer<Load>> toApply, NetworkXmlReaderContext context) {
        LoadType loadType = context.getReader().readEnumAttribute("loadType", LoadType.class, LoadType.UNDEFINED);
        double p0 = context.getReader().readDoubleAttribute("p0");
        double q0 = context.getReader().readDoubleAttribute("q0");
        readNodeOrBus(adder, context);
        adder.setLoadType(loadType)
                .setP0(p0)
                .setQ0(q0);
        double p = context.getReader().readDoubleAttribute("p");
        double q = context.getReader().readDoubleAttribute("q");
        toApply.add(l -> l.getTerminal().setP(p).setQ(q));
    }

    @Override
    protected void readSubElements(String id, LoadAdder adder, List<Consumer<Load>> toApply, NetworkXmlReaderContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case EXPONENTIAL_MODEL -> {
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, EXPONENTIAL_MODEL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_10, context);
                    double np = context.getReader().readDoubleAttribute("np");
                    double nq = context.getReader().readDoubleAttribute("nq");
                    context.getReader().readEndNode();
                    adder.newExponentialModel()
                            .setNp(np)
                            .setNq(nq)
                            .add();
                }
                case ZIP_MODEL -> {
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, EXPONENTIAL_MODEL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_10, context);
                    double c0p = context.getReader().readDoubleAttribute("c0p");
                    double c1p = context.getReader().readDoubleAttribute("c1p");
                    double c2p = context.getReader().readDoubleAttribute("c2p");
                    double c0q = context.getReader().readDoubleAttribute("c0q");
                    double c1q = context.getReader().readDoubleAttribute("c1q");
                    double c2q = context.getReader().readDoubleAttribute("c2q");
                    context.getReader().readEndNode();
                    adder.newZipModel()
                            .setC0p(c0p)
                            .setC1p(c1p)
                            .setC2p(c2p)
                            .setC0q(c0q)
                            .setC1q(c1q)
                            .setC2q(c2q)
                            .add();
                }
                default -> readSubElement(elementName, id, toApply, context);
            }
        });
    }
}
