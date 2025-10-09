/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public interface CgmesMetadataModelsAdder extends ExtensionAdder<Network, CgmesMetadataModels> {

    interface ModelAdder {
        ModelAdder setSubset(CgmesSubset subset);

        ModelAdder setId(String id);

        ModelAdder setDescription(String description);

        ModelAdder setVersion(int sshVersion);

        ModelAdder setModelingAuthoritySet(String modelingAuthoritySet);

        ModelAdder addProfile(String profile);

        ModelAdder addDependentOn(String dependentOn);

        ModelAdder addSupersedes(String supersedes);

        CgmesMetadataModelsAdder add();
    }

    ModelAdder newModel();

    @Override
    default Class<CgmesMetadataModels> getExtensionClass() {
        return CgmesMetadataModels.class;
    }
}
