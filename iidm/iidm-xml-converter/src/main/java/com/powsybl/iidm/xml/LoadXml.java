/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.function.Consumer;

import static com.powsybl.iidm.xml.ConnectableXmlUtil.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LoadXml extends AbstractComplexIdentifiableXml<Load, LoadAdder, VoltageLevel> {

    static final LoadXml INSTANCE = new LoadXml();

    static final String ROOT_ELEMENT_NAME = "load";
    private static final String EXPONENTIAL_MODEL = "exponentialModel";
    private static final String ZIP_MODEL = "zipModel";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(Load l) {
        return l.getModel().isPresent();
    }

    @Override
    protected void writeRootElementAttributes(Load l, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("loadType", l.getLoadType().name());
        XmlUtil.writeDouble("p0", l.getP0(), context.getWriter());
        XmlUtil.writeDouble("q0", l.getQ0(), context.getWriter());
        writeNodeOrBus(null, l.getTerminal(), context);
        writePQ(null, l.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(Load load, VoltageLevel parent, NetworkXmlWriterContext context) throws XMLStreamException {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_9, context, () -> writeModel(load, context));
    }

    private void writeModel(Load load, NetworkXmlWriterContext context) {
        load.getModel().ifPresent(model -> {
            try {
                switch (model.getType()) {
                    case EXPONENTIAL:
                        ExponentialLoadModel expModel = (ExponentialLoadModel) model;
                        context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(context.isValid()), EXPONENTIAL_MODEL);
                        XmlUtil.writeDouble("np", expModel.getNp(), context.getWriter());
                        XmlUtil.writeDouble("nq", expModel.getNq(), context.getWriter());
                        break;
                    case ZIP:
                        ZipLoadModel zipModel = (ZipLoadModel) model;
                        context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(context.isValid()), ZIP_MODEL);
                        XmlUtil.writeDouble("pp", zipModel.getPp(), context.getWriter());
                        XmlUtil.writeDouble("ip", zipModel.getIp(), context.getWriter());
                        XmlUtil.writeDouble("zp", zipModel.getZp(), context.getWriter());
                        XmlUtil.writeDouble("pq", zipModel.getPq(), context.getWriter());
                        XmlUtil.writeDouble("iq", zipModel.getIq(), context.getWriter());
                        XmlUtil.writeDouble("zq", zipModel.getZq(), context.getWriter());
                        break;
                    default:
                        throw new PowsyblException("Unexpected load model type: " + model.getType());
                }
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
    }

    @Override
    protected LoadAdder createAdder(VoltageLevel vl) {
        return vl.newLoad();
    }

    @Override
    protected void readRootElementAttributes(LoadAdder adder, List<Consumer<Load>> toApply, NetworkXmlReaderContext context) {
        String loadTypeStr = context.getReader().getAttributeValue(null, "loadType");
        LoadType loadType = loadTypeStr == null ? LoadType.UNDEFINED : LoadType.valueOf(loadTypeStr);
        double p0 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "p0");
        double q0 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "q0");
        readNodeOrBus(adder, context);
        adder.setLoadType(loadType)
                .setP0(p0)
                .setQ0(q0);
        double p = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "p");
        double q = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "q");
        toApply.add(l -> l.getTerminal().setP(p).setQ(q));
    }

    @Override
    protected void readSubElements(String id, LoadAdder adder, List<Consumer<Load>> toApply, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case EXPONENTIAL_MODEL:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, EXPONENTIAL_MODEL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_9, context);
                    double np = XmlUtil.readDoubleAttribute(context.getReader(), "np");
                    double nq = XmlUtil.readDoubleAttribute(context.getReader(), "nq");
                    adder.newExponentialModel()
                            .setNp(np)
                            .setNq(nq)
                            .add();
                    break;
                case ZIP_MODEL:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, EXPONENTIAL_MODEL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_9, context);
                    double pp = XmlUtil.readDoubleAttribute(context.getReader(), "pp");
                    double ip = XmlUtil.readDoubleAttribute(context.getReader(), "ip");
                    double zp = XmlUtil.readDoubleAttribute(context.getReader(), "zp");
                    double pq = XmlUtil.readDoubleAttribute(context.getReader(), "pq");
                    double iq = XmlUtil.readDoubleAttribute(context.getReader(), "iq");
                    double zq = XmlUtil.readDoubleAttribute(context.getReader(), "zq");
                    adder.newZipModel()
                            .setPp(pp)
                            .setIp(ip)
                            .setZp(zp)
                            .setPq(pq)
                            .setIq(iq)
                            .setZq(zq)
                            .add();
                    break;
                default:
                    super.readSubElements(id, toApply, context);
            }
        });
    }
}
