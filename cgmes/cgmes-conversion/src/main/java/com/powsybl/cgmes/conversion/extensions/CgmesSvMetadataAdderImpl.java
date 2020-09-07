/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class CgmesSvMetadataAdderImpl extends AbstractExtensionAdder<Network, CgmesSvMetadata> implements CgmesSvMetadataAdder {

    private String scenarioTime;
    private String description;
    private int svVersion;
    private final List<String> dependencies = new ArrayList<>();
    private String modelingAuthoritySet;

    public CgmesSvMetadataAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    public CgmesSvMetadataAdder setScenarioTime(String scenarioTime) {
        this.scenarioTime = scenarioTime;
        return this;
    }

    @Override
    public CgmesSvMetadataAdder setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public CgmesSvMetadataAdder setSvVersion(int svVersion) {
        this.svVersion = svVersion;
        return this;
    }

    @Override
    public CgmesSvMetadataAdder addDependency(String dependency) {
        this.dependencies.add(Objects.requireNonNull(dependency));
        return this;
    }

    @Override
    public CgmesSvMetadataAdder setModelingAuthoritySet(String modelingAuthoritySet) {
        this.modelingAuthoritySet = modelingAuthoritySet;
        return this;
    }

    @Override
    protected CgmesSvMetadata createExtension(Network extendable) {
        return new CgmesSvMetadataImpl(scenarioTime, description, svVersion, dependencies, modelingAuthoritySet);
    }
}
