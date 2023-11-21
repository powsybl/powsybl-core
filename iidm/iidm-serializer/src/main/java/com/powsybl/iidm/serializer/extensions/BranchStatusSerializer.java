/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerializer;
import com.powsybl.commons.extensions.ExtensionSerializer;
import com.powsybl.commons.io.ReaderContext;
import com.powsybl.commons.io.WriterContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.BranchStatus;
import com.powsybl.iidm.network.extensions.BranchStatusAdder;

/**
 * @author Nicolas Noir {@literal <nicolas.noir at rte-france.com>}
 */
@AutoService(ExtensionSerializer.class)
public class BranchStatusSerializer<C extends Connectable<C>> extends AbstractExtensionSerializer<C, BranchStatus<C>> {

    public BranchStatusSerializer() {
        super(BranchStatus.NAME, "network", BranchStatus.class,
                "branchStatus.xsd", "http://www.powsybl.org/schema/iidm/ext/branch_status/1_0",
                "bs");
    }

    @Override
    public void write(BranchStatus<C> branchStatus, WriterContext context) {
        context.getWriter().writeNodeContent(branchStatus.getStatus().name());
    }

    @Override
    public BranchStatus<C> read(C connectable, ReaderContext context) {
        BranchStatus.Status status = BranchStatus.Status.valueOf(context.getReader().readContent());
        BranchStatusAdder<C> adder = connectable.newExtension(BranchStatusAdder.class);
        return adder.withStatus(status)
                .add();
    }
}
