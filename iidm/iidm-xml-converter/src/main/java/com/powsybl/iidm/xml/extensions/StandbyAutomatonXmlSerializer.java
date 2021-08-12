package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.iidm.network.extensions.StandbyAutomatonAdder;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class StandbyAutomatonXmlSerializer implements ExtensionXmlSerializer<StaticVarCompensator, StandbyAutomaton> {

    @Override
    public String getExtensionName() {
        return "standbyAutomaton";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super StandbyAutomaton> getExtensionClass() {
        return StandbyAutomaton.class;
    }

    @Override
    public boolean hasSubElements() {
        return false;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/standbyAutomaton.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.powsybl.org/schema/iidm/ext/standby_automaton/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "sa";
    }

    @Override
    public void write(StandbyAutomaton standbyAutomaton, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("b0", standbyAutomaton.getB0(), context.getWriter());
        context.getWriter().writeAttribute("standby", Boolean.toString(standbyAutomaton.isStandby()));
        XmlUtil.writeDouble("lowVoltageSetPoint", standbyAutomaton.getLowVoltageSetPoint(), context.getWriter());
        XmlUtil.writeDouble("highVoltageSetPoint", standbyAutomaton.getHighVoltageSetPoint(), context.getWriter());
        XmlUtil.writeDouble("lowVoltageThreshold", standbyAutomaton.getLowVoltageThreshold(), context.getWriter());
        XmlUtil.writeDouble("highVoltageThreshold", standbyAutomaton.getHighVoltageThreshold(), context.getWriter());
    }

    @Override
    public StandbyAutomaton read(StaticVarCompensator svc, XmlReaderContext context) {
        float b0 = XmlUtil.readFloatAttribute(context.getReader(), "b0");
        boolean standby = XmlUtil.readBoolAttribute(context.getReader(), "standby");
        float lowVoltageSetPoint = XmlUtil.readFloatAttribute(context.getReader(), "lowVoltageSetPoint");
        float highVoltageSetPoint = XmlUtil.readFloatAttribute(context.getReader(), "highVoltageSetPoint");
        float lowVoltageThreshold = XmlUtil.readFloatAttribute(context.getReader(), "lowVoltageThreshold");
        float highVoltageThreshold = XmlUtil.readFloatAttribute(context.getReader(), "highVoltageThreshold");
        svc.newExtension(StandbyAutomatonAdder.class)
                .withB0(b0)
                .withStandbyStatus(standby)
                .withLowVoltageSetPoint(lowVoltageSetPoint)
                .withHighVoltageSetPoint(highVoltageSetPoint)
                .withLowVoltageThreshold(lowVoltageThreshold)
                .withHighVoltageThreshold(highVoltageThreshold)
                .add();
        return svc.getExtension(StandbyAutomaton.class);
    }
}
