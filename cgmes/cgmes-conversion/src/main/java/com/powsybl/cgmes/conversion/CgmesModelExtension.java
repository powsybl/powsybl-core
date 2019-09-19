/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import java.util.Objects;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.CgmesUpdater;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesModelExtension extends AbstractExtension<Network> {

    private final CgmesModel cgmes;
    private final CgmesUpdater cgmesUpdater;

    public CgmesModelExtension(CgmesModel cgmes, CgmesUpdater cgmesUpdater) {
        this.cgmes = Objects.requireNonNull(cgmes);
        this.cgmesUpdater = Objects.requireNonNull(cgmesUpdater);
    }

    public CgmesModel getCgmesModel() {
        return cgmes;
    }
    
    public CgmesUpdater getCgmesUpdater() {
        return cgmesUpdater;
    }

    @Override
    public String getName() {
        return "CgmesModel";
    }
}
