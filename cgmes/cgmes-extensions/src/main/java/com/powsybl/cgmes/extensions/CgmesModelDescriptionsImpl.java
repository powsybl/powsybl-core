/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

import java.util.*;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class CgmesModelDescriptionsImpl extends AbstractExtension<Network> implements CgmesModelDescriptions {

    static class ModelImpl implements Model {

        private final String id;
        private final String description;
        private final int version;
        private final List<String> dependencies = new ArrayList<>();
        private final String modelingAuthoritySet;
        private final Set<String> profiles = new HashSet<>();

        ModelImpl(String id, String description, int version, List<String> dependencies, String modelingAuthoritySet, Set<String> profiles) {
            this.id = id;
            this.description = description;
            this.version = version;
            this.dependencies.addAll(dependencies);
            this.modelingAuthoritySet = modelingAuthoritySet;
            this.profiles.addAll(profiles);
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
        public int getVersion() {
            return version;
        }

        @Override
        public List<String> getDependencies() {
            return Collections.unmodifiableList(dependencies);
        }

        @Override
        public String getModelingAuthoritySet() {
            return modelingAuthoritySet;
        }

        @Override
        public Set<String> getProfiles() {
            return Collections.unmodifiableSet(profiles);
        }
    }

    private final Map<String, Model> models = new HashMap<>();

    CgmesModelDescriptionsImpl(Map<String, Model> models) {
        this.models.putAll(models);
    }

    @Override
    public Optional<Model> getModel(String profile) {
        return Optional.ofNullable(models.get(profile));
    }

    @Override
    public Collection<Model> getModels() {
        return Collections.unmodifiableCollection(new HashSet<>(models.values()));
    }

}
