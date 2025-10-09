/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Connectable;

import java.util.Set;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface CgmesTapChangers<C extends Connectable<C>> extends Extension<C> {

    String NAME = "cgmesTapChangers";

    Set<CgmesTapChanger> getTapChangers();

    CgmesTapChanger getTapChanger(String id);

    CgmesTapChangerAdder newTapChanger();

    @Override
    default String getName() {
        return NAME;
    }
}
