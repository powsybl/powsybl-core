/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableAdder;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractIdentifiableSerDe<T extends Identifiable<? super T>, A extends IdentifiableAdder<T, A>, P extends Identifiable> {

    protected abstract String getRootElementName();

    protected boolean isValid(T identifiable, P parent) {
        return true;
    }

    protected abstract void writeRootElementAttributes(T identifiable, P parent, NetworkSerializerContext context);

    protected void writeSubElements(T identifiable, P parent, NetworkSerializerContext context) {
    }

    public final void write(T identifiable, P parent, NetworkSerializerContext context) {
        if (!isValid(identifiable, parent)) {
            return;
        }
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), getRootElementName());
        context.getWriter().writeStringAttribute("id", context.getAnonymizer().anonymizeString(identifiable.getId()));
        context.getWriter().writeStringAttribute("name", identifiable.getOptionalName().map(context.getAnonymizer()::anonymizeString).orElse(null));

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_2, context, () -> context.getWriter().writeBooleanAttribute("fictitious", identifiable.isFictitious(), false));

        writeRootElementAttributes(identifiable, parent, context);

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> AliasesSerDe.write(identifiable, getRootElementName(), context));

        PropertiesSerDe.write(identifiable, context);

        writeSubElements(identifiable, parent, context);

        context.getWriter().writeEndNode();

        context.addExportedEquipment(identifiable);
    }

    protected abstract A createAdder(P parent);

    public abstract void read(P parent, NetworkDeserializerContext context);

    protected String readIdentifierAttributes(A adder, NetworkDeserializerContext context) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("id"));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("name"));
        if (adder != null) {
            adder.setId(id)
                    .setName(name);
        }
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_2, context, () -> {
            boolean fictitious = context.getReader().readBooleanAttribute("fictitious", false);
            if (adder != null) {
                adder.setFictitious(fictitious);
            }
        });
        return id;
    }
}
