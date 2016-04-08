/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.RatioTapChanger;
import eu.itesla_project.iidm.network.Substation;
import eu.itesla_project.iidm.network.ThreeWindingsTransformer;
import eu.itesla_project.iidm.network.ThreeWindingsTransformerAdder;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ThreeWindingsTransformerXml extends TransformerXml<ThreeWindingsTransformer, ThreeWindingsTransformerAdder> {

    static final ThreeWindingsTransformerXml INSTANCE = new ThreeWindingsTransformerXml();

    static final String ROOT_ELEMENT_NAME = "threeWindingsTransformer";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(ThreeWindingsTransformer twt) {
        return twt.getLeg2().getRatioTapChanger() != null
                || twt.getLeg3().getRatioTapChanger() != null
                || twt.getLeg1().getCurrentLimits() != null
                || twt.getLeg2().getCurrentLimits() != null
                || twt.getLeg3().getCurrentLimits() != null;
    }

    @Override
    protected void writeRootElementAttributes(ThreeWindingsTransformer twt, Substation s, XmlWriterContext context) throws XMLStreamException {
        writeFloat("r1", twt.getLeg1().getR(), context.getWriter());
        writeFloat("x1", twt.getLeg1().getX(), context.getWriter());
        writeFloat("g1", twt.getLeg1().getG(), context.getWriter());
        writeFloat("b1", twt.getLeg1().getB(), context.getWriter());
        writeFloat("ratedU1", twt.getLeg1().getRatedU(), context.getWriter());
        writeFloat("r2", twt.getLeg2().getR(), context.getWriter());
        writeFloat("x2", twt.getLeg2().getX(), context.getWriter());
        writeFloat("ratedU2", twt.getLeg2().getRatedU(), context.getWriter());
        writeFloat("r3", twt.getLeg3().getR(), context.getWriter());
        writeFloat("x3", twt.getLeg3().getX(), context.getWriter());
        writeFloat("ratedU3", twt.getLeg3().getRatedU(), context.getWriter());
        writeNodeOrBus(1, twt.getLeg1().getTerminal(), context);
        writeNodeOrBus(2, twt.getLeg2().getTerminal(), context);
        writeNodeOrBus(3, twt.getLeg3().getTerminal(), context);
        if (context.getOptions().isWithBranchSV()) {
            writePQ(1, twt.getLeg1().getTerminal(), context.getWriter());
            writePQ(2, twt.getLeg2().getTerminal(), context.getWriter());
            writePQ(3, twt.getLeg3().getTerminal(), context.getWriter());
        }
    }

    @Override
    protected void writeSubElements(ThreeWindingsTransformer twt, Substation s, XmlWriterContext context) throws XMLStreamException {
        RatioTapChanger rtc2 = twt.getLeg2().getRatioTapChanger();
        if (rtc2 != null) {
            writeRatioTapChanger("ratioTapChanger2", rtc2, context);
        }
        RatioTapChanger rtc3 = twt.getLeg3().getRatioTapChanger();
        if (rtc3 != null) {
            writeRatioTapChanger("ratioTapChanger3", rtc3, context);
        }
        if (twt.getLeg1().getCurrentLimits() != null) {
            writeCurrentLimits(1, twt.getLeg1().getCurrentLimits(), context.getWriter());
        }
        if (twt.getLeg2().getCurrentLimits() != null) {
            writeCurrentLimits(2, twt.getLeg2().getCurrentLimits(), context.getWriter());
        }
        if (twt.getLeg3().getCurrentLimits() != null) {
            writeCurrentLimits(3, twt.getLeg3().getCurrentLimits(), context.getWriter());
        }
    }

    @Override
    protected ThreeWindingsTransformerAdder createAdder(Substation s) {
        return s.newThreeWindingsTransformer();
    }

    @Override
    protected ThreeWindingsTransformer readRootElementAttributes(ThreeWindingsTransformerAdder adder, XMLStreamReader reader, List<Runnable> endTasks) {
        throw new AssertionError("TODO"); // FIXME
    }

    @Override
    protected void readSubElements(ThreeWindingsTransformer identifiable, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        throw new AssertionError("TODO"); // FIXME
    }
}
