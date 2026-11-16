/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Jérémy LABOUS {@literal <jlabous at silicom.fr>}
 */
public interface CgmesModelExtensionAdder extends ExtensionAdder<Network, CgmesModelExtension> {

    @Override
    default Class<CgmesModelExtension> getExtensionClass() {
        return CgmesModelExtension.class;
    }

    CgmesModelExtensionAdder withModel(CgmesModel cgmes);
}
