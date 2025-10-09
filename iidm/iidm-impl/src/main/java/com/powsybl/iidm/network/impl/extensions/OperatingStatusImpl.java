/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.OperatingStatus;

import java.util.Objects;

/**
 * @author Nicolas Noir {@literal <nicolas.noir at rte-france.com>}
 */
public class OperatingStatusImpl<I extends Identifiable<I>> extends AbstractExtension<I> implements OperatingStatus<I> {

    private Status status;

    public OperatingStatusImpl(I identifiable, Status status) {
        super(identifiable);
        if (!OperatingStatus.isAllowedIdentifiable(identifiable)) {
            throw new PowsyblException("Operating status extension is not allowed on identifiable type: " + identifiable.getType());
        }
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public OperatingStatus<I> setStatus(Status branchStatus) {
        Objects.requireNonNull(branchStatus);
        this.status = branchStatus;
        return this;
    }
}
