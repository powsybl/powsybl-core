/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serializer;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableAdder;
import com.powsybl.iidm.serializer.util.IidmXmlUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract class for equipment that need their sub-elements to be entirely created.
 *
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
abstract class AbstractComplexIdentifiableXml<T extends Identifiable<T>, A extends IdentifiableAdder<T, A>, P extends Identifiable> extends AbstractIdentifiableXml<T, A, P> {

    protected abstract void readRootElementAttributes(A adder, List<Consumer<T>> toApply, NetworkXmlReaderContext context);

    protected void readSubElement(String elementName, String id, List<Consumer<T>> toApply, NetworkXmlReaderContext context) {
        switch (elementName) {
            case PropertiesXml.ROOT_ELEMENT_NAME -> PropertiesXml.read(toApply, context);
            case AliasesXml.ROOT_ELEMENT_NAME -> {
                IidmXmlUtil.assertMinimumVersion(getRootElementName(), AliasesXml.ROOT_ELEMENT_NAME, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
                AliasesXml.read(toApply, context);
            }
            default -> throw new PowsyblException("Unknown element name '" + elementName + "' in '" + id + "'");
        }
    }

    protected abstract void readSubElements(String id, A adder, List<Consumer<T>> toApply, NetworkXmlReaderContext context);

    @Override
    public final void read(P parent, NetworkXmlReaderContext context) {
        List<Consumer<T>> toApply = new ArrayList<>();
        A adder = createAdder(parent);
        String id = readIdentifierAttributes(adder, context);
        readRootElementAttributes(adder, toApply, context);
        readSubElements(id, adder, toApply, context);
        T identifiable = adder.add();
        toApply.forEach(consumer -> consumer.accept(identifiable));
    }
}
