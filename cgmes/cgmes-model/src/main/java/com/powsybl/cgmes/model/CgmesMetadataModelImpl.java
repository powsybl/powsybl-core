/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class CgmesMetadataModelImpl implements CgmesMetadataModel {

    private final CgmesSubset subset;
    private String id;
    private String description;
    private int version;
    private String modelingAuthoritySet;
    private final Set<String> profiles = new HashSet<>();
    private final Set<String> dependentOn = new HashSet<>();
    private final Set<String> supersedes = new HashSet<>();

    public CgmesMetadataModelImpl(CgmesSubset subset, String modelingAuthoritySet) {
        this.subset = subset;
        this.modelingAuthoritySet = modelingAuthoritySet;
        this.version = 1;
        this.description = subset.getIdentifier() + " Model";
    }

    @Override
    public CgmesSubset getSubset() {
        return subset;
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
        return profiles;
    }

    @Override
    public Set<String> getDependentOn() {
        return dependentOn;
    }

    @Override
    public Set<String> getSupersedes() {
        return supersedes;
    }

    @Override
    public CgmesMetadataModelImpl setVersion(int version) {
        this.version = version;
        return this;
    }

    @Override
    public CgmesMetadataModelImpl setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public CgmesMetadataModelImpl setProfile(String profile) {
        Objects.requireNonNull(profile);
        this.profiles.clear();
        this.profiles.add(profile);
        return this;
    }

    @Override
    public CgmesMetadataModelImpl addProfiles(Collection<String> profiles) {
        this.profiles.addAll(profiles);
        return this;
    }

    @Override
    public CgmesMetadataModelImpl setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public CgmesMetadataModelImpl addSupersedes(String id) {
        addIfNonEmpty(id, this.supersedes);
        return this;
    }

    @Override
    public CgmesMetadataModelImpl addDependentOn(String id) {
        addIfNonEmpty(id, this.dependentOn);
        return this;
    }

    @Override
    public CgmesMetadataModelImpl addDependentOn(Collection<String> dependentOn) {
        this.dependentOn.addAll(dependentOn);
        return this;
    }

    @Override
    public CgmesMetadataModelImpl addSupersedes(Collection<String> supersedes) {
        this.supersedes.addAll(supersedes);
        return this;
    }

    @Override
    public CgmesMetadataModelImpl setModelingAuthoritySet(String modelingAuthoritySet) {
        this.modelingAuthoritySet = modelingAuthoritySet;
        return this;
    }

    @Override
    public CgmesMetadataModelImpl clearDependencies() {
        this.dependentOn.clear();
        this.supersedes.clear();
        return this;
    }

    private static void addIfNonEmpty(String id, Collection<String> ids) {
        if (id != null && !id.isEmpty()) {
            ids.add(id);
        }
    }
}
