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
// import java.util.Optional;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class TapChangerBlockingsSerDe extends AbstractExtensionSerDe<Network, TapChangerBlockings> {

    private static final String TCB_ROOT_ELEMENT = "tcb";
    private static final String CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT = "controlVoltageLevel";
    private static final String MEASUREMENT_POINT_ELEMENT = "measurementPoint";
    private static final String BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT = "busbarSectionOrBusId";

    public TapChangerBlockingsSerDe() {
        super(TapChangerBlockings.NAME, "network", TapChangerBlockings.class,
                "tcbs_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/tcbs/1_0", "tcb");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(TCB_ROOT_ELEMENT, CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT,
                MEASUREMENT_POINT_ELEMENT, BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT);
    }

    @Override
    public void write(TapChangerBlockings tcbs, SerializerContext context) {
        TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes();
        for (TapChangerBlocking tcb : tcbs.getTapChangerBlockings()) {
            writer.writeStartNode(getNamespaceUri(), TCB_ROOT_ELEMENT);
            writer.writeStringAttribute("name", tcb.getName());
            writeMeasurementPoint(tcb, writer);
            writeControlVoltageLevel(tcb.getControlVoltageLevels(), writer);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    private void writeMeasurementPoint(TapChangerBlocking tcb, TreeDataWriter writer) {
        writer.writeStartNodes();
        for (MeasurementPoint measurementPoint : tcb.getMeasurementPoints()) {
            writer.writeStartNode(getNamespaceUri(), MEASUREMENT_POINT_ELEMENT);
            writer.writeStringAttribute("id", measurementPoint.getId());
            writer.writeStartNodes();
            for (String busbarSectionOrBusId : measurementPoint.getBusbarSectionsOrBusesIds()) {
                writer.writeStartNode(getNamespaceUri(), BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT);
                writer.writeNodeContent(busbarSectionOrBusId);
                writer.writeEndNode();
            }
            writer.writeEndNodes();
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    private void writeControlVoltageLevel(List<ControlVoltageLevel> controlVoltageLevels, TreeDataWriter writer) {
        writer.writeStartNodes();
        for (ControlVoltageLevel controlVoltageLevel : controlVoltageLevels) {
            writer.writeStartNode(getNamespaceUri(), CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT);
            if (controlVoltageLevel.forceOneTransformerLoads()) {
                writer.writeStringAttribute("forceOneTransformerLoads", "true");
            }
            writer.writeNodeContent(controlVoltageLevel.getId());
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public TapChangerBlockings read(Network network, DeserializerContext context) {
        TapChangerBlockingsAdder adder = network.newExtension(TapChangerBlockingsAdder.class);
        context.getReader().readChildNodes(elementName -> {
            if (!elementName.equals(TCB_ROOT_ELEMENT)) {
                throw new PowsyblException(getExceptionMessageUnknownElement(elementName, "tcbs"));
            }
            readTapChangerBlocking(context, adder);
        });
        return adder.add();
    }

    private static void readTapChangerBlocking(DeserializerContext context, TapChangerBlockingsAdder adder) {
        String name = context.getReader().readStringAttribute("name");
        TapChangerBlockingAdder tcbAdder = adder.newTapChangerBlocking()
                .withName(name);
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case MEASUREMENT_POINT_ELEMENT -> readMeasurementPoint(context, tcbAdder);
                case CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT -> readControlVoltageLevel(context, tcbAdder);
                default -> throw new PowsyblException(getExceptionMessageUnknownElement(elementName, "'controlZone'"));
            }
        });
        tcbAdder.add();
    }

    private static void readMeasurementPoint(DeserializerContext context, TapChangerBlockingAdder tcbAdder) {
        List<String> busbarSectionsOrBusesIds = new ArrayList<>();
        String id = context.getReader().readStringAttribute("id");
        context.getReader().readChildNodes(elementName -> {
            if (!elementName.equals(BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT)) {
                throw new PowsyblException(getExceptionMessageUnknownElement(elementName, MEASUREMENT_POINT_ELEMENT));
            }
            busbarSectionsOrBusesIds.add(context.getReader().readContent());
        });
        tcbAdder.newMeasurementPoint()
                .withBusbarSectionsOrBusesIds(busbarSectionsOrBusesIds)
                .withId(id)
                .add();
    }

    private static void readControlVoltageLevel(DeserializerContext context, TapChangerBlockingAdder tcbAdder) {
//        boolean forceOneTransformerLoads = context.getReader().readOptionalBooleanAttribute("forceOneTransformerLoads")).orElse(false);
        boolean forceOneTransformerLoads = context.getReader().readBooleanAttribute("forceOneTransformerLoads", false);
        String id = context.getReader().readContent();
        ControlVoltageLevelAdder<TapChangerBlockingAdder> vlAdder = tcbAdder.newControlVoltageLevel()
                .withId(id);
        if (forceOneTransformerLoads) {
            vlAdder.withForceOneTransformerLoads();
        }
        vlAdder.add();
    }

    private static String getExceptionMessageUnknownElement(String elementName, String where) {
        return "Unknown element name '" + elementName + "' in '" + where + "'";
    }
}
