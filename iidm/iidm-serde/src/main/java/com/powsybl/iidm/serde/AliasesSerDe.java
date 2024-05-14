/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public final class AliasesSerDe {

    static final String ARRAY_ELEMENT_NAME = "aliases";
    static final String ROOT_ELEMENT_NAME = "alias";

    public static void write(Identifiable<?> identifiable, String rootElementName, NetworkSerializerContext context) {
        IidmSerDeUtil.assertMinimumVersionIfNotDefault(!identifiable.getAliases().isEmpty(), rootElementName, ROOT_ELEMENT_NAME, IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_3, context);
        context.getWriter().writeStartNodes();
        for (String alias : identifiable.getAliases()) {
            context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ROOT_ELEMENT_NAME);
            IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_4, context,
                    () -> context.getWriter().writeStringAttribute("type", identifiable.getAliasType(alias).orElse(null)));
            context.getWriter().writeNodeContent(context.getAnonymizer().anonymizeString(alias));
            context.getWriter().writeEndNode();
        }
        context.getWriter().writeEndNodes();
    }

    public static <T extends Identifiable> void read(T identifiable, NetworkDeserializerContext context) {
        read(context).accept(identifiable);
    }

    public static <T extends Identifiable> void read(List<Consumer<T>> toApply, NetworkDeserializerContext context) {
        toApply.add(read(context));
    }

    private static <T extends Identifiable> Consumer<T> read(NetworkDeserializerContext context) {
        String[] aliasType = new String[1];
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_4, context, () -> aliasType[0] = context.getReader().readStringAttribute("type"));
        String alias = context.getAnonymizer().deanonymizeString(context.getReader().readContent());
        return identifiable -> identifiable.addAlias(alias, aliasType[0]);
    }

    private AliasesSerDe() {
    }
}
