/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class CgmesSshMetadataAdderImpl extends AbstractExtensionAdder<Network, CgmesSshMetadata> implements CgmesSshMetadataAdder {

    private String id;
    private String description;
    private int sshVersion = 0;
    private final List<String> dependencies = new ArrayList<>();
    private String modelingAuthoritySet;

    public CgmesSshMetadataAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    public CgmesSshMetadataAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public CgmesSshMetadataAdder setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public CgmesSshMetadataAdder setSshVersion(int sshVersion) {
        this.sshVersion = sshVersion;
        return this;
    }

    @Override
    public CgmesSshMetadataAdder addDependency(String dependency) {
        this.dependencies.add(Objects.requireNonNull(dependency));
        return this;
    }

    @Override
    public CgmesSshMetadataAdder setModelingAuthoritySet(String modelingAuthoritySet) {
        this.modelingAuthoritySet = modelingAuthoritySet;
        return this;
    }

    @Override
    protected CgmesSshMetadata createExtension(Network extendable) {
        if (dependencies.isEmpty()) {
            throw new PowsyblException("cgmesSshMetadata.dependencies must have at least one dependency");
        }
        if (modelingAuthoritySet == null) {
            throw new PowsyblException("cgmesSshMetadata.modelingAuthoritySet is undefined");
        }
        return new CgmesSshMetadataImpl(id, description, sshVersion, dependencies, modelingAuthoritySet);
    }
}
