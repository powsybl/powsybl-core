/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public interface CgmesMetadataModels extends Extension<Network> {

    String NAME = "cgmesMetadataModels";

    interface Model {
        String getPart();

        String getId();

        String getDescription();

        int getVersion();

        String getModelingAuthoritySet();

        Set<String> getProfiles();

        Set<String> getDependentOn();

        Set<String> getSupersedes();
    }

    Collection<Model> getModels();

    Optional<Model> getModelForPart(String part);

    Optional<Model> getModelForProfile(String profile);

    @Override
    default String getName() {
        return NAME;
    }
}
