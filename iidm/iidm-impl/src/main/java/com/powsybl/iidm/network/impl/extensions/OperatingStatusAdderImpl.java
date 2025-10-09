/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.iidm.network.extensions.OperatingStatusAdder;

import java.util.Objects;

/**
 * @author Nicolas Noir {@literal <nicolas.noir at rte-france.com>}
 */
public class OperatingStatusAdderImpl<I extends Identifiable<I>> extends AbstractExtensionAdder<I, OperatingStatus<I>>
        implements OperatingStatusAdder<I> {

    private OperatingStatus.Status status = OperatingStatus.Status.IN_OPERATION;

    OperatingStatusAdderImpl(I branch) {
        super(branch);
    }

    @Override
    protected OperatingStatus<I> createExtension(Identifiable identifiable) {
        return new OperatingStatusImpl<>(identifiable, status);
    }

    @Override
    public OperatingStatusAdderImpl<I> withStatus(OperatingStatus.Status status) {
        this.status = Objects.requireNonNull(status);
        return this;
    }
}
