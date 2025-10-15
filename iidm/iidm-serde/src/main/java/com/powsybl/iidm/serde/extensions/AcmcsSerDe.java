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
public class AcmcsSerDe extends AbstractExtensionSerDe<Network, Acmcs> {

    private static final String ACMC_ROOT_ELEMENT = "acmc";
    private static final String CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT = "controlVoltageLevel";
    private static final String MEASUREMENT_POINT_ELEMENT = "measurementPoint";
    private static final String BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT = "busbarSectionOrBusId";

    public AcmcsSerDe() {
        super(Acmcs.NAME, "network", Acmcs.class,
                "acmcs_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/acmcs/1_0", "acmc");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(ACMC_ROOT_ELEMENT, CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT,
                MEASUREMENT_POINT_ELEMENT, BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT);
    }

    @Override
    public void write(Acmcs acms, SerializerContext context) {
        TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes();
        for (Acmc acmc : acms.getAcmcs()) {
            writer.writeStartNode(getNamespaceUri(), ACMC_ROOT_ELEMENT);
            writer.writeStringAttribute("name", acmc.getName());
            writeMeasurementPoint(acmc, writer);
            writeControlVoltageLevel(acmc.getControlVoltageLevels(), writer);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    private void writeMeasurementPoint(Acmc acmc, TreeDataWriter writer) {
        writer.writeStartNode(getNamespaceUri(), MEASUREMENT_POINT_ELEMENT);
        writer.writeStartNodes();
        for (String busbarSectionOrBusId : acmc.getMeasurementPoint().getBusbarSectionsOrBusesIds()) {
            writer.writeStartNode(getNamespaceUri(), BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT);
            writer.writeNodeContent(busbarSectionOrBusId);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
        writer.writeEndNode();
    }

    private void writeControlVoltageLevel(List<ControlVoltageLevel> controlVoltageLevels, TreeDataWriter writer) {
        writer.writeStartNodes();
        for (ControlVoltageLevel controlVoltageLevel : controlVoltageLevels) {
            writer.writeStartNode(getNamespaceUri(), CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT);
            writer.writeNodeContent(controlVoltageLevel.getId());
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public Acmcs read(Network network, DeserializerContext context) {
        AcmcsAdder adder = network.newExtension(AcmcsAdder.class);
        context.getReader().readChildNodes(elementName -> {
            if (!elementName.equals(ACMC_ROOT_ELEMENT)) {
                throw new PowsyblException(getExceptionMessageUnknownElement(elementName, "acmcs"));
            }
            readAcmc(context, adder);
        });
        return adder.add();
    }

    private static void readAcmc(DeserializerContext context, AcmcsAdder adder) {
        String name = context.getReader().readStringAttribute("name");
        AcmcAdder acmcAdder = adder.newAcmc()
                .withName(name);
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case MEASUREMENT_POINT_ELEMENT -> readMeasurementPoint(context, acmcAdder);
                case CONTROL_VOLTAGE_LEVEL_ROOT_ELEMENT -> readControlVoltageLevel(context, acmcAdder);
                default -> throw new PowsyblException(getExceptionMessageUnknownElement(elementName, "'controlZone'"));
            }
        });
        acmcAdder.add();
    }

    private static void readMeasurementPoint(DeserializerContext context, AcmcAdder acmcAdder) {
        List<String> busbarSectionsOrBusesIds = new ArrayList<>();
        context.getReader().readChildNodes(elementName -> {
            if (!elementName.equals(BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT)) {
                throw new PowsyblException(getExceptionMessageUnknownElement(elementName, MEASUREMENT_POINT_ELEMENT));
            }
            busbarSectionsOrBusesIds.add(context.getReader().readContent());
        });
        acmcAdder.newMeasurementPoint()
                .withBusbarSectionsOrBusesIds(busbarSectionsOrBusesIds)
                .add();
    }

    private static void readControlVoltageLevel(DeserializerContext context, AcmcAdder acmcAdder) {
        String id = context.getReader().readContent();
        acmcAdder.newControlVoltageLevel()
                .withId(id)
                .add();
    }

    private static String getExceptionMessageUnknownElement(String elementName, String where) {
        return "Unknown element name '" + elementName + "' in '" + where + "'";
    }
}
