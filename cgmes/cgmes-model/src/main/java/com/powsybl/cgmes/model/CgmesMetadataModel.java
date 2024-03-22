/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model;

import java.util.*;

/**
 * <p>In each CGMES instance file there is a unique <code>Model</code> object that holds metadata information.</p>
 * <p>The model contents are specified by profiles: equipment, power flow initial values, power flow results, etc.</p>
 * <p>Each model can contain data from multiple profiles and is produced by a modeling authority.</p>
 * <p>A model may have dependencies on other models. Two kind of dependencies are kept: "dependent on" and "supersedes".</p>
 * <p>A "dependent on" is a reference to the other required model. As an example: a load flow solution depends on the topology model it was computed from.</p>
 * <p>When a model is updated, the resulting model "supersedes" the models that were used as basis for the update.</p>
 * <p>
 * As an example: when building a Common Grid Model (CGM) from Individual Grid Models (IGM),
 * the power flow initial assumptions (SSH) of each IGM might be adjusted as part of the merging process and area interchange control,
 * thus updated SSH instance files are created.
 * Each updated SSH model "supersedes" the original one.
 * </p>
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class CgmesMetadataModel {

    private final CgmesSubset subset;
    private String id;
    private String description;
    private int version;
    private String modelingAuthoritySet;
    private final Set<String> profiles = new HashSet<>();
    private final Set<String> dependentOn = new HashSet<>();
    private final Set<String> supersedes = new HashSet<>();

    public CgmesMetadataModel(CgmesSubset subset, String modelingAuthoritySet) {
        this.subset = subset;
        this.modelingAuthoritySet = modelingAuthoritySet;
        this.version = 1;
        this.description = subset.getIdentifier() + " Model";
    }

    /**
     * The CGMES instance file (subset) that the model refers to: EQ, SSH, ...
     *
     * @return the subset of the CGMES the model refers to
     */
    public CgmesSubset getSubset() {
        return subset;
    }

    /**
     * The unique identifier for the model.
     * A model identifier should change if the data contained in the model has changed.
     *
     * @return the identifier of the model
     */
    public String getId() {
        return id;
    }

    /**
     * A description for the model.
     *
     * @return the identifier of the model
     */
    public String getDescription() {
        return description;
    }

    /**
     * The version number for the model.
     * The version number should change if the data contained in the model has changed.
     *
     * @return the version number of the model
     */
    public int getVersion() {
        return version;
    }

    /**
     * A reference to the organisation role / modeling authority set responsible for producing the model contents.
     * It is a URN/URI.
     *
     * @return the URN/URI of the modeling authority set producing the model
     */
    public String getModelingAuthoritySet() {
        return modelingAuthoritySet;
    }

    /**
     * <p>References to the profiles used in the model.
     * Each profile defines semantics data that may appear inside the model.
     * A model may contain data from multiple profiles.</p>
     * <p>As an example, "http://iec.ch/TC57/61970-456/SteadyStateHypothesis/2/0" refers to power flow inputs in CGMES 3.</p>
     * <p>In CGMES 2.4, the model for the EQ subset may contain two profiles: "http://iec.ch/TC57/2013/61970-452/EquipmentCore/4" to describe the equipment core and "http://iec.ch/TC57/2013/61970-452/EquipmentOperation/4" if the model is defined at node/braker level.</p>
     *
     * @return the URN/URIs of profiles describing the data in the model
     */
    public Set<String> getProfiles() {
        return profiles;
    }

    /**
     * References to other models that the model depends on.
     *
     * @return the identifiers of the models the model depends on
     */
    public Set<String> getDependentOn() {
        return dependentOn;
    }

    /**
     * References to other models that are superseded by this model.
     *
     * @return the identifiers of the models this model supersedes
     */
    public Set<String> getSupersedes() {
        return supersedes;
    }

    public CgmesMetadataModel setVersion(int version) {
        this.version = version;
        return this;
    }

    public CgmesMetadataModel setId(String id) {
        this.id = id;
        return this;
    }

    public CgmesMetadataModel setProfile(String profile) {
        Objects.requireNonNull(profile);
        this.profiles.clear();
        this.profiles.add(profile);
        return this;
    }

    public CgmesMetadataModel addProfiles(Collection<String> profiles) {
        this.profiles.addAll(profiles);
        return this;
    }

    public CgmesMetadataModel setDescription(String description) {
        this.description = description;
        return this;
    }

    public CgmesMetadataModel addSupersedes(String id) {
        addIfNonEmpty(id, this.supersedes);
        return this;
    }

    public CgmesMetadataModel addDependentOn(String id) {
        addIfNonEmpty(id, this.dependentOn);
        return this;
    }

    public CgmesMetadataModel addDependentOn(Collection<String> dependentOn) {
        this.dependentOn.addAll(dependentOn);
        return this;
    }

    public CgmesMetadataModel addSupersedes(Collection<String> supersedes) {
        this.supersedes.addAll(supersedes);
        return this;
    }

    public CgmesMetadataModel setModelingAuthoritySet(String modelingAuthoritySet) {
        this.modelingAuthoritySet = modelingAuthoritySet;
        return this;
    }

    public CgmesMetadataModel clearDependencies() {
        this.dependentOn.clear();
        return this;
    }

    private static void addIfNonEmpty(String id, Collection<String> ids) {
        if (id != null && !id.isEmpty()) {
            ids.add(id);
        }
    }
}
