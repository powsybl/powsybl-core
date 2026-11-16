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
        DeserializationEndTask.Step creationStep = getPostponedCreationStep();
        if (creationStep != null) {
            context.addEndTask(creationStep, () -> createElement(adder, toApply));
        } else {
            createElement(adder, toApply);
        }
    }

    /**
     * <p>Return <code>null</code> if the element should be created immediately after it is read from the serialized network.
     * Else, return the step ({@link DeserializationEndTask.Step}) when the element should be created.</p>
     * <p>In some specific cases, the element could not be created right after it is read, typically if it references
     * other network elements which may have not been yet created. (It is better to create the element without the said
     * references and to fill them in later, but it is not always possible, or at high cost.)
     * If this method returns a non-null value, the element's creation will be defined as an "end task" and
     * will be performed after the network is read (before the extensions creation or at the very end).</p>
     * @return the wanted {@link DeserializationEndTask.Step} if the creation should be postponed, <code>null</code> otherwise.
     */
    protected DeserializationEndTask.Step getPostponedCreationStep() {
        return null;
    }

    private static <T extends Identifiable<T>, A extends IdentifiableAdder<T, A>> void createElement(A adder, List<Consumer<T>> toApply) {
        T identifiable = adder.add();
        toApply.forEach(consumer -> consumer.accept(identifiable));
    }
}
