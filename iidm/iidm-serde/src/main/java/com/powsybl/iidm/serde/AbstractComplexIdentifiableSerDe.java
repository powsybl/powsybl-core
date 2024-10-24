/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableAdder;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract class for serializing/deserializing equipment that need their sub-elements to be entirely created.
 *
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
abstract class AbstractComplexIdentifiableSerDe<T extends Identifiable<T>, A extends IdentifiableAdder<T, A>, P extends Identifiable> extends AbstractIdentifiableSerDe<T, A, P> {

    protected abstract void readRootElementAttributes(A adder, P parent, List<Consumer<T>> toApply, NetworkDeserializerContext context);

    protected void readSubElement(String elementName, String id, List<Consumer<T>> toApply, NetworkDeserializerContext context) {
        switch (elementName) {
            case PropertiesSerDe.ROOT_ELEMENT_NAME -> PropertiesSerDe.read(toApply, context);
            case AliasesSerDe.ROOT_ELEMENT_NAME -> {
                IidmSerDeUtil.assertMinimumVersion(getRootElementName(), AliasesSerDe.ROOT_ELEMENT_NAME, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
                AliasesSerDe.read(toApply, context);
            }
            default -> throw new PowsyblException("Unknown element name '" + elementName + "' in '" + id + "'");
        }
    }

    protected abstract void readSubElements(String id, A adder, List<Consumer<T>> toApply, NetworkDeserializerContext context);

    @Override
    public final void read(P parent, NetworkDeserializerContext context) {
        List<Consumer<T>> toApply = new ArrayList<>();
        A adder = createAdder(parent);
        String id = readIdentifierAttributes(adder, context);
        readRootElementAttributes(adder, parent, toApply, context);
        readSubElements(id, adder, toApply, context);
        createElement(adder, toApply, context, this.postponeValidation());
    }

    private static <T extends Identifiable<T>, A extends IdentifiableAdder<T, A>> void createElement(A adder, List<Consumer<T>> toApply, NetworkDeserializerContext context, boolean postponeValidation) {
        T identifiable = adder.add();
        if (postponeValidation) {
            for (Consumer<T> consumer : toApply) {
                context.getEndTasks().add(() -> consumer.accept(identifiable));
            }
        } else {
            toApply.forEach(consumer -> consumer.accept(identifiable));
        }
    }

    /**
     * <p>Should the current element's validation be postponed?</p>
     * <p>In some specific cases, the element could not be validated right after it is read, typically if it references
     * other network elements which may have not been yet created. (It is better to create the said
     * references and to fill them in later, but it is not always possible, or at high cost.)
     * If this method returns <code>true</code>, the element's <code>toApply</code> operations will be defined as an "end task" and
     * will be performed after the whole network is read.</p>
     * @return <code>true</code> if validation should be postponed, <code>false</code> otherwise.
     */
    protected boolean postponeValidation() {
        return false;
    }
}
