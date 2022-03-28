/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public interface CgmesModelDescriptions extends Extension<Network> {

    String NAME = "cgmesModelDescriptions";

    interface Model {

        String getId();

        String getDescription();

        int getVersion();

        List<String> getDependencies();

        String getModelingAuthoritySet();

        Set<String> getProfiles();
    }

    Optional<Model> getModel(String profile);

    Collection<Model> getModels();

    @Override
    default String getName() {
        return NAME;
    }
}
