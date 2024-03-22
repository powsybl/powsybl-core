/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;

import java.util.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CgmesMetadataModelsAdderImpl extends AbstractExtensionAdder<Network, CgmesMetadataModels> implements CgmesMetadataModelsAdder {

    class ModelAdderImpl implements ModelAdder {

        private CgmesSubset subset;
        private String id;
        private String description;
        private int version = 0;
        private String modelingAuthoritySet;
        private final Set<String> profiles = new HashSet<>();
        private final Set<String> dependentOn = new HashSet<>();
        private final Set<String> supersedes = new HashSet<>();

        @Override
        public ModelAdder setSubset(CgmesSubset subset) {
            this.subset = subset;
            return this;
        }

        @Override
        public ModelAdder setId(String id) {
            this.id = id;
            return this;
        }

        @Override
        public ModelAdderImpl setDescription(String description) {
            this.description = description;
            return this;
        }

        @Override
        public ModelAdderImpl setVersion(int version) {
            this.version = version;
            return this;
        }

        @Override
        public ModelAdderImpl setModelingAuthoritySet(String modelingAuthoritySet) {
            this.modelingAuthoritySet = modelingAuthoritySet;
            return this;
        }

        @Override
        public ModelAdder addProfile(String profile) {
            profiles.add(Objects.requireNonNull(profile));
            return this;
        }

        @Override
        public ModelAdderImpl addDependentOn(String dependentOn) {
            this.dependentOn.add(Objects.requireNonNull(dependentOn));
            return this;
        }

        @Override
        public ModelAdderImpl addSupersedes(String supersedes) {
            this.supersedes.add(Objects.requireNonNull(supersedes));
            return this;
        }

        @Override
        public CgmesMetadataModelsAdderImpl add() {
            if (subset == null) {
                throw new PowsyblException("Model subset is undefined");
            }
            if (id == null) {
                throw new PowsyblException("Model id is undefined");
            }
            if (modelingAuthoritySet == null) {
                throw new PowsyblException("Model modelingAuthoritySet is undefined");
            }
            if (profiles.isEmpty()) {
                throw new PowsyblException("Model must contain at least one profile");
            }
            models.add(new CgmesMetadataModel(subset, modelingAuthoritySet)
                    .setId(id)
                    .setDescription(description)
                    .setVersion(version)
                    .addProfiles(profiles)
                    .addDependentOn(dependentOn)
                    .addSupersedes(supersedes));
            return CgmesMetadataModelsAdderImpl.this;
        }
    }

    private final Set<CgmesMetadataModel> models = new HashSet<>();

    CgmesMetadataModelsAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    public ModelAdder newModel() {
        return new ModelAdderImpl();
    }

    @Override
    protected CgmesMetadataModels createExtension(Network extendable) {
        if (models.isEmpty()) {
            throw new PowsyblException("Must contain at least one model");
        }
        return new CgmesMetadataModelsImpl(models);
    }
}
