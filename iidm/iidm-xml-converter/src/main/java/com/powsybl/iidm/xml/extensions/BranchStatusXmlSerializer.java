/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.BranchStatus;
import com.powsybl.iidm.network.extensions.BranchStatusAdder;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class BranchStatusXmlSerializer<C extends Connectable<C>> implements ExtensionXmlSerializer<C, BranchStatus<C>> {

    @Override
    public String getExtensionName() {
        return  BranchStatus.NAME;
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super BranchStatus> getExtensionClass() {
        return BranchStatus.class;
    }

    @Override
    public boolean hasSubElements() {
        return true;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/branchStatus.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.powsybl.org/schema/iidm/ext/branch_status/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "bs";
    }

    @Override
    public void write(BranchStatus branchStatus, XmlWriterContext context) throws XMLStreamException {
        context.getExtensionsWriter().writeCharacters(branchStatus.getStatus().name());
    }

    @Override
    public BranchStatus read(Connectable connectable, XmlReaderContext context) throws XMLStreamException {
        BranchStatus.Status status = BranchStatus.Status.valueOf(XmlUtil.readText("branchStatus", context.getReader()));
        ((Connectable<?>) connectable).newExtension(BranchStatusAdder.class)
                .withStatus(status)
                .add();
        return ((Connectable<?>) connectable).getExtension(BranchStatus.class);
    }
}
