/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

import java.util.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CgmesMetadataModelsImpl extends AbstractExtension<Network> implements CgmesMetadataModels {

    static class ModelImpl implements Model {

        private final String part;
        private final String id;
        private final String description;
        private final int version;
        private final String modelingAuthoritySet;
        private final Set<String> profiles = new HashSet<>();
        private final Set<String> dependentOn = new HashSet<>();
        private final Set<String> supersedes = new HashSet<>();

        ModelImpl(String part, String id, String description, int version, String modelingAuthoritySet,
                  Set<String> profiles, Set<String> dependentOn, Set<String> supersedes) {
            this.part = part;
            this.id = id;
            this.description = description;
            this.version = version;
            this.modelingAuthoritySet = modelingAuthoritySet;
            this.profiles.addAll(profiles);
            this.dependentOn.addAll(dependentOn);
            this.supersedes.addAll(supersedes);
        }

        @Override
        public String getPart() {
            return part;
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
        public String getModelingAuthoritySet() {
            return modelingAuthoritySet;
        }

        @Override
        public Set<String> getProfiles() {
            return Collections.unmodifiableSet(profiles);
        }

        @Override
        public Set<String> getDependentOn() {
            return Collections.unmodifiableSet(dependentOn);
        }

        @Override
        public Set<String> getSupersedes() {
            return Collections.unmodifiableSet(supersedes);
        }
    }

    private final List<Model> models = new ArrayList<>();
    private final Map<String, Model> partModel = new HashMap<>();
    private final Map<String, Model> profileModel = new HashMap<>();

    CgmesMetadataModelsImpl(Set<Model> models) {
        this.models.addAll(models);
        models.forEach(m -> partModel.put(m.getPart(), m));
        models.forEach(m -> m.getProfiles().forEach(profile -> profileModel.put(profile, m)));
    }

    @Override
    public Optional<Model> getModelForPart(String part) {
        return Optional.ofNullable(partModel.get(part));
    }

    @Override
    public Optional<Model> getModelForPartModellingAuthoritySet(String part, String modelingAuthoritySet) {
        return models.stream()
                .filter(m -> m.getPart().equals(part) && m.getModelingAuthoritySet().equals(modelingAuthoritySet))
                .findFirst();
    }

    @Override
    public Optional<Model> getModelForProfile(String profile) {
        return Optional.ofNullable(profileModel.get(profile));
    }

    @Override
    public Collection<Model> getModels() {
        return Collections.unmodifiableCollection(models);
    }

    @Override
    public List<Model> getSortedModels() {
        return models.stream().sorted(
                Comparator.comparing(CgmesMetadataModels.Model::getModelingAuthoritySet)
                        .thenComparing(CgmesMetadataModels.Model::getPart)
                        .thenComparing(CgmesMetadataModels.Model::getVersion)
                        .thenComparing(CgmesMetadataModels.Model::getId)
        ).toList();
    }
}
