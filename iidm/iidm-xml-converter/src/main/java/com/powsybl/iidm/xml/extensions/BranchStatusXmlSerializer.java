/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.BranchStatus;
import com.powsybl.iidm.network.extensions.BranchStatusAdder;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class BranchStatusXmlSerializer<C extends Connectable<C>> extends AbstractExtensionXmlSerializer<C, BranchStatus<C>> {

    public BranchStatusXmlSerializer() {
        super(BranchStatus.NAME, "network", BranchStatus.class, true,
                "branchStatus.xsd", "http://www.powsybl.org/schema/iidm/ext/branch_status/1_0",
                "bs");
    }

    @Override
    public void write(BranchStatus<C> branchStatus, XmlWriterContext context) {
        context.getWriter().writeNodeContent(branchStatus.getStatus().name());
    }

    @Override
    public BranchStatus<C> read(C connectable, XmlReaderContext context) {
        BranchStatus.Status status = BranchStatus.Status.valueOf(context.getReader().readUntilEndNode("branchStatus", null));
        BranchStatusAdder<C> adder = connectable.newExtension(BranchStatusAdder.class);
        return adder.withStatus(status)
                .add();
    }
}
