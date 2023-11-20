/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableAdder;
import com.powsybl.iidm.serializer.util.IidmSerializerUtil;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractIdentifiableSerializer<T extends Identifiable<? super T>, A extends IdentifiableAdder<T, A>, P extends Identifiable> {

    protected abstract String getRootElementName();

    protected boolean isValid(T identifiable, P parent) {
        return true;
    }

    protected abstract void writeRootElementAttributes(T identifiable, P parent, NetworkSerializerWriterContext context);

    protected void writeSubElements(T identifiable, P parent, NetworkSerializerWriterContext context) {
    }

    public final void write(T identifiable, P parent, NetworkSerializerWriterContext context) {
        if (!isValid(identifiable, parent)) {
            return;
        }
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), getRootElementName());
        context.getWriter().writeStringAttribute("id", context.getAnonymizer().anonymizeString(identifiable.getId()));
        identifiable.getOptionalName().ifPresent(name -> context.getWriter().writeStringAttribute("name", context.getAnonymizer().anonymizeString(name)));

        IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_2, context, () -> context.getWriter().writeBooleanAttribute("fictitious", identifiable.isFictitious(), false));

        writeRootElementAttributes(identifiable, parent, context);

        IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> AliasesSerializer.write(identifiable, getRootElementName(), context));

        PropertiesSerializer.write(identifiable, context);

        writeSubElements(identifiable, parent, context);

        context.getWriter().writeEndNode();

        context.addExportedEquipment(identifiable);
    }

    protected abstract A createAdder(P parent);

    public abstract void read(P parent, NetworkSerializerReaderContext context);

    protected String readIdentifierAttributes(A adder, NetworkSerializerReaderContext context) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("id"));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("name"));
        adder.setId(id)
                .setName(name);
        IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_2, context, () -> {
            boolean fictitious = context.getReader().readBooleanAttribute("fictitious", false);
            adder.setFictitious(fictitious);
        });
        return id;
    }
}
