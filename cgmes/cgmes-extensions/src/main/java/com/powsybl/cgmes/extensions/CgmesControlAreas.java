/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import java.util.Collection;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public interface CgmesControlAreas extends Extension<Network> {

    String NAME = "cgmesControlAreas";

    CgmesControlAreaAdder newCgmesControlArea();

    Collection<CgmesControlArea> getCgmesControlAreas();

    CgmesControlArea getCgmesControlArea(String controlAreaId);

    boolean containsCgmesControlAreaId(String controlAreaId);

    default void cleanIfEmpty() {
        if (getCgmesControlAreas().isEmpty()) {
            getExtendable().removeExtension(CgmesControlAreas.class);
        }
    }

    @Override
    default String getName() {
        return NAME;
    }

}
