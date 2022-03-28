/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;

import java.util.*;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class CgmesModelDescriptionsAdderImpl extends AbstractExtensionAdder<Network, CgmesModelDescriptions> implements CgmesModelDescriptionsAdder {

    class ModelAdderImpl implements ModelAdder {

        private String id;
        private String description;
        private int version = 0;
        private final List<String> dependencies = new ArrayList<>();
        private String modelingAuthoritySet;
        private final Set<String> profiles = new HashSet<>();

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
        public ModelAdderImpl addDependency(String dependency) {
            this.dependencies.add(Objects.requireNonNull(dependency));
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
        public ModelAdder addProfiles(Set<String> profiles) {
            if (profiles.stream().anyMatch(Objects::isNull)) {
                throw new PowsyblException("Profiles cannot be null");
            }
            this.profiles.addAll(Objects.requireNonNull(profiles));
            return this;
        }

        @Override
        public CgmesModelDescriptionsAdderImpl add() {
            if (id == null) {
                throw new PowsyblException("Model id is undefined");
            }
            if (modelingAuthoritySet == null) {
                throw new PowsyblException("Model modelingAuthoritySet is undefined");
            }
            if (profiles.isEmpty()) {
                throw new PowsyblException("Model must contain at least one profile");
            }
            CgmesModelDescriptionsImpl.ModelImpl model = new CgmesModelDescriptionsImpl.ModelImpl(id, description, version, dependencies, modelingAuthoritySet, profiles);
            profiles.forEach(profile -> models.put(profile, model));
            return CgmesModelDescriptionsAdderImpl.this;
        }
    }

    private final Map<String, CgmesModelDescriptions.Model> models = new HashMap<>();

    CgmesModelDescriptionsAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    public ModelAdder newModel() {
        return new ModelAdderImpl();
    }

    @Override
    protected CgmesModelDescriptions createExtension(Network extendable) {
        if (models.isEmpty()) {
            throw new PowsyblException("Must contain at least one model");
        }
        return new CgmesModelDescriptionsImpl(models);
    }
}
