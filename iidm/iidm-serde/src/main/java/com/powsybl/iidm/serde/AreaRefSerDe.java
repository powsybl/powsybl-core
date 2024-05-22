package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;

import java.util.function.Consumer;

public final class AreaRefSerDe {
    private static final String ID = "id";

    public static final String ROOT_ELEMENT_NAME = "areaRef";

    public static void readAreaRef(NetworkDeserializerContext context, Network network, Consumer<Area> endTaskTerminalConsumer) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(ID));
        context.getReader().readEndNode();
        context.getEndTasks().add(() -> {
            Area area = network.getArea(id);
            endTaskTerminalConsumer.accept(area);
        });
    }

    public static void writeAreaRef(Area area, NetworkSerializerContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ROOT_ELEMENT_NAME);
        context.getWriter().writeStringAttribute(ID, context.getAnonymizer().anonymizeString(area.getId()));
        context.getWriter().writeEndNode();
    }

    private AreaRefSerDe() {
    }
}
