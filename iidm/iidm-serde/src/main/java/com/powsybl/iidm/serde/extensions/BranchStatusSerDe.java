/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.iidm.network.extensions.OperatingStatusAdder;

/**
 * This deserializer is only kept for backward compatibility, to be able to still read an old IIDM serialized file
 * containing the branch status extension.
 *
 * @author Nicolas Noir {@literal <nicolas.noir at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class BranchStatusSerDe<C extends Connectable<C>> extends AbstractExtensionSerDe<C, OperatingStatus<C>> {

    public BranchStatusSerDe() {
        super("branchStatus", "network", OperatingStatus.class,
                "branchStatus.xsd", "http://www.powsybl.org/schema/iidm/ext/branch_status/1_0",
                "bs");
    }

    @Override
    public void write(OperatingStatus<C> branchStatus, SerializerContext context) {
        throw new UnsupportedOperationException("This is a deprecated extension (replaced by `OperatingStatus`) and it should never be written anymore");
    }

    @Override
    public OperatingStatus<C> read(C connectable, DeserializerContext context) {
        OperatingStatus.Status status = OperatingStatus.Status.valueOf(context.getReader().readContent());
        OperatingStatusAdder<C> adder = connectable.newExtension(OperatingStatusAdder.class);
        return adder.withStatus(status)
                .add();
    }
}
