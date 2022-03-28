/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Network;

import java.util.Set;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public interface CgmesModelDescriptionsAdder extends ExtensionAdder<Network, CgmesModelDescriptions> {

    interface ModelAdder {
        ModelAdder setId(String id);

        ModelAdder setDescription(String description);

        ModelAdder setVersion(int sshVersion);

        ModelAdder addDependency(String dependency);

        ModelAdder setModelingAuthoritySet(String modelingAuthoritySet);

        ModelAdder addProfile(String profile);

        ModelAdder addProfiles(Set<String> profiles);

        CgmesModelDescriptionsAdder add();
    }

    ModelAdder newModel();

    @Override
    default Class<CgmesModelDescriptions> getExtensionClass() {
        return CgmesModelDescriptions.class;
    }
}
