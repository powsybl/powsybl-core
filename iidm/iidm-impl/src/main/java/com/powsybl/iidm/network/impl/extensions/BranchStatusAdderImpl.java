/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.BranchStatus;
import com.powsybl.iidm.network.extensions.BranchStatusAdder;

import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class BranchStatusAdderImpl<C extends Connectable<C>> extends AbstractExtensionAdder<C, BranchStatus<C>>
        implements BranchStatusAdder<C> {

    private BranchStatus.Status status = BranchStatus.Status.IN_OPERATION;

    BranchStatusAdderImpl(C branch) {
        super(branch);
    }

    @Override
    protected BranchStatus<C> createExtension(Connectable branch) {
        return new BranchStatusImpl(branch, status);
    }

    @Override
    public BranchStatusAdder withStatus(BranchStatus.Status branchStatus) {
        Objects.requireNonNull(branchStatus);
        this.status = branchStatus;
        return this;
    }
}
