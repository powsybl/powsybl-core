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
import com.powsybl.iidm.serializer.util.IidmSerializerUtil;

/**
 * Abstract class for equipment that can and/or must be entirely created before reading their sub-elements.
 *
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
abstract class AbstractSimpleIdentifiableSerializer<T extends Identifiable<? super T>, A extends IdentifiableAdder<T, A>, P extends Identifiable> extends AbstractIdentifiableSerializer<T, A, P> {

    protected abstract T readRootElementAttributes(A adder, P parent, NetworkSerializerReaderContext context);

    protected void readSubElement(String elementName, T identifiable, NetworkSerializerReaderContext context) {
        switch (elementName) {
            case PropertiesSerializer.ROOT_ELEMENT_NAME -> PropertiesSerializer.read(identifiable, context);
            case AliasesSerializer.ROOT_ELEMENT_NAME -> {
                IidmSerializerUtil.assertMinimumVersion(getRootElementName(), AliasesSerializer.ROOT_ELEMENT_NAME, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
                AliasesSerializer.read(identifiable, context);
            }
            default -> throw new PowsyblException("Unknown element name '" + elementName + "' in '" + identifiable.getId() + "'");
        }
    }

    @Override
    public final void read(P parent, NetworkSerializerReaderContext context) {
        A adder = createAdder(parent);
        readIdentifierAttributes(adder, context);
        T identifiable = readRootElementAttributes(adder, parent, context);
        if (identifiable != null) {
            readSubElements(identifiable, context);
        } else {
            context.getReader().skipChildNodes();
        }
    }

    protected abstract void readSubElements(T identifiable, NetworkSerializerReaderContext context);
}
