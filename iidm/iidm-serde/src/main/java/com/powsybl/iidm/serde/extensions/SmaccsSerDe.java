package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class SmaccsSerDe extends AbstractExtensionSerDe<Network, Smaccs> {

    private static final String SMACC_ROOT_ELEMENT = "smacc";
    private static final String HIGH_VOLTAGE_CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT = "highVoltageControlVoltageLevel";
    private static final String LOW_VOLTAGE_CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT = "lowVoltageControlVoltageLevel";
    private static final String HIGH_VOLTAGE_MEASUREMENT_POINT_ELEMENT = "highVoltagemeasurementPoint";
    private static final String LOW_VOLTAGE_MEASUREMENT_POINT_ELEMENT = "lowVoltagemeasurementPoint";
    private static final String BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT = "busbarSectionOrBusId";

    public SmaccsSerDe() {
        super(Smaccs.NAME, "network", Smaccs.class,
                "smaccs_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/smaccs/1_0", "smacc");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(SMACC_ROOT_ELEMENT,
                HIGH_VOLTAGE_CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT,
                LOW_VOLTAGE_CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT,
                HIGH_VOLTAGE_MEASUREMENT_POINT_ELEMENT,
                LOW_VOLTAGE_MEASUREMENT_POINT_ELEMENT,
                BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT);
    }

    @Override
    public void write(Smaccs acms, SerializerContext context) {
        TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes();
        for (Smacc smacc : acms.getSmaccs()) {
            writer.writeStartNode(getNamespaceUri(), SMACC_ROOT_ELEMENT);
            writer.writeStringAttribute("name", smacc.getName());
            writeHighVoltageMeasurementPoint(smacc, writer);
            writeLowVoltageMeasurementPoint(smacc, writer);
            writeHighVoltageControlVoltageLevel(smacc.getHighVoltageControlVoltageLevels(), writer);
            writeLowVoltageControlVoltageLevel(smacc.getLowVoltageControlVoltageLevels(), writer);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    private void writeHighVoltageMeasurementPoint(Smacc smacc, TreeDataWriter writer) {
        writer.writeStartNode(getNamespaceUri(), HIGH_VOLTAGE_MEASUREMENT_POINT_ELEMENT);
        writer.writeStartNodes();
        for (String busbarSectionOrBusId : smacc.getHighVoltageMeasurementPoint().getBusbarSectionsOrBusesIds()) {
            writer.writeStartNode(getNamespaceUri(), BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT);
            writer.writeNodeContent(busbarSectionOrBusId);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
        writer.writeEndNode();
    }

    private void writeLowVoltageMeasurementPoint(Smacc smacc, TreeDataWriter writer) {
        writer.writeStartNode(getNamespaceUri(), LOW_VOLTAGE_MEASUREMENT_POINT_ELEMENT);
        writer.writeStartNodes();
        for (String busbarSectionOrBusId : smacc.getLowVoltageMeasurementPoint().getBusbarSectionsOrBusesIds()) {
            writer.writeStartNode(getNamespaceUri(), BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT);
            writer.writeNodeContent(busbarSectionOrBusId);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
        writer.writeEndNode();
    }

    private void writeHighVoltageControlVoltageLevel(List<ControlVoltageLevel> controlVoltageLevels, TreeDataWriter writer) {
        writer.writeStartNodes();
        for (ControlVoltageLevel controlVoltageLevel : controlVoltageLevels) {
            writer.writeStartNode(getNamespaceUri(), HIGH_VOLTAGE_CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT);
            writer.writeNodeContent(controlVoltageLevel.getId());
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    private void writeLowVoltageControlVoltageLevel(List<ControlVoltageLevel> controlVoltageLevels, TreeDataWriter writer) {
        writer.writeStartNodes();
        for (ControlVoltageLevel controlVoltageLevel : controlVoltageLevels) {
            writer.writeStartNode(getNamespaceUri(), LOW_VOLTAGE_CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT);
            writer.writeNodeContent(controlVoltageLevel.getId());
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public Smaccs read(Network network, DeserializerContext context) {
        SmaccsAdder adder = network.newExtension(SmaccsAdder.class);
        context.getReader().readChildNodes(elementName -> {
            if (!elementName.equals(SMACC_ROOT_ELEMENT)) {
                throw new PowsyblException(getExceptionMessageUnknownElement(elementName, "smaccs"));
            }
            readSmacc(context, adder);
        });
        return adder.add();
    }

    private static void readSmacc(DeserializerContext context, SmaccsAdder adder) {
        String name = context.getReader().readStringAttribute("name");
        SmaccAdder smaccAdder = adder.newSmacc()
                .withName(name);
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case HIGH_VOLTAGE_MEASUREMENT_POINT_ELEMENT -> readHighVoltageMeasurementPoint(context, smaccAdder);
                case LOW_VOLTAGE_MEASUREMENT_POINT_ELEMENT -> readLowVoltageMeasurementPoint(context, smaccAdder);
                case HIGH_VOLTAGE_CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT -> readHighVoltageControlVoltageLevel(context, smaccAdder);
                case LOW_VOLTAGE_CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT -> readLowVoltageControlVoltageLevel(context, smaccAdder);
                default -> throw new PowsyblException(getExceptionMessageUnknownElement(elementName, "'controlZone'"));
            }
        });
        smaccAdder.add();
    }

    private static void readHighVoltageMeasurementPoint(DeserializerContext context, SmaccAdder smaccAdder) {
        List<String> busbarSectionsOrBusesIds = new ArrayList<>();
        context.getReader().readChildNodes(elementName -> {
            if (!elementName.equals(BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT)) {
                throw new PowsyblException(getExceptionMessageUnknownElement(elementName, HIGH_VOLTAGE_MEASUREMENT_POINT_ELEMENT));
            }
            busbarSectionsOrBusesIds.add(context.getReader().readContent());
        });
        smaccAdder.newHighVoltageMeasurementPoint()
                .withBusbarSectionsOrBusesIds(busbarSectionsOrBusesIds)
                .add();
    }

    private static void readLowVoltageMeasurementPoint(DeserializerContext context, SmaccAdder smaccAdder) {
        List<String> busbarSectionsOrBusesIds = new ArrayList<>();
        context.getReader().readChildNodes(elementName -> {
            if (!elementName.equals(BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT)) {
                throw new PowsyblException(getExceptionMessageUnknownElement(elementName, LOW_VOLTAGE_MEASUREMENT_POINT_ELEMENT));
            }
            busbarSectionsOrBusesIds.add(context.getReader().readContent());
        });
        smaccAdder.newLowVoltageMeasurementPoint()
                .withBusbarSectionsOrBusesIds(busbarSectionsOrBusesIds)
                .add();
    }

    private static void readHighVoltageControlVoltageLevel(DeserializerContext context, SmaccAdder smaccAdder) {
        String id = context.getReader().readContent();
        smaccAdder.newHighVoltageControlVoltageLevel()
                .withId(id)
                .add();
    }

    private static void readLowVoltageControlVoltageLevel(DeserializerContext context, SmaccAdder smaccAdder) {
        String id = context.getReader().readContent();
        smaccAdder.newLowVoltageControlVoltageLevel()
                .withId(id)
                .add();
    }

    private static String getExceptionMessageUnknownElement(String elementName, String where) {
        return "Unknown element name '" + elementName + "' in '" + where + "'";
    }
}
