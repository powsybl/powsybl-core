/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.update.CgmesUpdate;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public class CgmesModelExtensionImpl extends AbstractExtension<Network> implements CgmesModelExtension {

    private final CgmesModel cgmes;
    private final CgmesUpdate cgmesUpdate;

    public CgmesModelExtensionImpl(CgmesModel cgmes, CgmesUpdate cgmesUpdate) {
        this.cgmes = Objects.requireNonNull(cgmes);
        this.cgmesUpdate = Objects.requireNonNull(cgmesUpdate);
    }

    @Override
    public CgmesModel getCgmesModel() {
        return cgmes;
    }

    @Override
    public CgmesUpdate getCgmesUpdate() {
        return cgmesUpdate;
    }
}
