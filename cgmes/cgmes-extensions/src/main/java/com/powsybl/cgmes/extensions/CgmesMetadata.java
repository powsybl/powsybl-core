/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Optional;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public interface CgmesMetadata extends Extension<Network> {

    String NAME = "cgmesMetadata";

    interface Model {

        String getId();

        String getDescription();

        int getVersion();

        List<String> getDependencies();

        String getModelingAuthoritySet();
    }

    Model getEq();

    Optional<Model> getTp();

    Optional<Model> getSsh();

    Optional<Model> getSv();

    @Override
    default String getName() {
        return NAME;
    }
}
