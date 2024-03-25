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
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class CgmesSshMetadataImpl extends AbstractExtension<Network> implements CgmesSshMetadata {

    private final String id;
    private final String description;
    private final int sshVersion;
    private final List<String> dependencies = new ArrayList<>();
    private final String modelingAuthoritySet;

    public CgmesSshMetadataImpl(String id, String description, int sshVersion, List<String> dependencies, String modelingAuthoritySet) {
        this.id = id;
        this.description = description;
        this.sshVersion = sshVersion;
        this.dependencies.addAll(Objects.requireNonNull(dependencies));
        this.modelingAuthoritySet = Objects.requireNonNull(modelingAuthoritySet);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getSshVersion() {
        return sshVersion;
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
