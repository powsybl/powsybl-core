/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.BranchStatus;

import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class BranchStatusImpl<C extends Connectable<C>> extends AbstractExtension<C> implements BranchStatus<C> {

    private Status status;

    public BranchStatusImpl(C branch, Status branchStatus) {
        super(branch);
        this.status = branchStatus;
    }

    public Status getStatus() {
        return status;
    }

    public BranchStatus<C> setStatus(Status branchStatus) {
        Objects.requireNonNull(branchStatus);
        this.status = branchStatus;
        return this;
    }

}
