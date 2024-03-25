/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class CgmesSvMetadataImpl extends AbstractExtension<Network> implements CgmesSvMetadata {

    private final String description;
    private final int svVersion;
    private final List<String> dependencies = new ArrayList<>();
    private final String modelingAuthoritySet;

    public CgmesSvMetadataImpl(String description, int svVersion, List<String> dependencies, String modelingAuthoritySet) {
        this.description = description;
        this.svVersion = svVersion;
        this.dependencies.addAll(Objects.requireNonNull(dependencies));
        this.modelingAuthoritySet = Objects.requireNonNull(modelingAuthoritySet);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getSvVersion() {
        return svVersion;
    }

    @Override
    public List<String> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    @Override
    public String getModelingAuthoritySet() {
        return modelingAuthoritySet;
    }
}
