/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.update.CgmesUpdate;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Jérémy LABOUS <jlabous at silicom.fr>
 */
public class CgmesModelExtensionAdderImpl extends AbstractExtensionAdder<Network, CgmesModelExtension>
        implements CgmesModelExtensionAdder {

    private CgmesModel cgmes;
    private CgmesUpdate cgmesUpdate;

    protected CgmesModelExtensionAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    protected CgmesModelExtension createExtension(Network extendable) {
        return new CgmesModelExtensionImpl(cgmes, cgmesUpdate);
    }

    @Override
    public CgmesModelExtensionAdder withModelAndUpdate(CgmesModel cgmes, CgmesUpdate cgmesUpdate) {
        this.cgmes = cgmes;
        this.cgmesUpdate = cgmesUpdate;
        return this;
    }
}
