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
 * <p>A model may have relationships to other models. Two kind of relationships are considered: "dependent on" and "supersedes".</p>
 * <p>A "dependent on" is a reference to an other required model. As an example: a load flow solution depends on the topology model it was computed from.</p>
 * <p>When a model is updated, the resulting model "supersedes" the models that were used as basis for the update.</p>
 * <p>
 * As an example: when building a Common Grid Model (CGM) from Individual Grid Models (IGM),
 * the power flow initial assumptions (SSH) of each IGM might be adjusted as part of the merging process and area interchange control,
 * thus updated SSH instance files are created.
 * Each updated SSH model "supersedes" the original one.
 * </p>
 * <p>More information can be found in the IEC-61970-552.</p>
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public interface CgmesMetadataModel {

    /**
     * The CGMES instance file (subset) that the model refers to: EQ, SSH, ...
     *
     * @return the subset of the CGMES the model refers to
     */
    CgmesSubset getSubset();

    /**
     * The unique identifier for the model.
     * A model identifier should change if the data contained in the model has changed.
     *
     * @return the identifier of the model
     */
    String getId();

    /**
     * A description for the model.
     *
     * @return the description of the model
     */
    String getDescription();

    /**
     * The version number for the model.
     * The version number should change if the data contained in the model has changed.
     *
     * @return the version number of the model
     */
    int getVersion();

    /**
     * A reference to the organisation role / modeling authority set responsible for producing the model contents.
     * It is a URN/URI.
     *
     * @return the URN/URI of the modeling authority set producing the model
     */
    String getModelingAuthoritySet();

    /**
     * <p>References to the profiles used in the model.
     * Each profile defines semantics data that may appear inside the model.
     * A model may contain data from multiple profiles.</p>
     * <p>As an example, "http://iec.ch/TC57/61970-456/SteadyStateHypothesis/2/0" refers to power flow inputs in CGMES 3.</p>
     * <p>In CGMES 2.4, the model for the EQ subset may contain two profiles: "http://iec.ch/TC57/2013/61970-452/EquipmentCore/4" to describe the equipment core and "http://iec.ch/TC57/2013/61970-452/EquipmentOperation/4" if the model is defined at node/braker level.</p>
     *
     * @return the URN/URIs of profiles describing the data in the model
     */
    Set<String> getProfiles();

    /**
     * References to other models that the model depends on.
     *
     * @return the identifiers of the models the model depends on
     */
    Set<String> getDependentOn();

    /**
     * References to other models that are superseded by this model.
     *
     * @return the identifiers of the models this model supersedes
     */
    Set<String> getSupersedes();

    /**
     * Set the given model version.
     * @param version The version to set for the model.
     * @return The model with the new version set.
     */
    CgmesMetadataModelImpl setVersion(int version);

    /**
     * Set the given model id.
     * @param id The id to set for the model.
     * @return The model with the new id set.
     */
    CgmesMetadataModelImpl setId(String id);

    /**
     * Remove any existing profile and set the given model profile.
     * @param profile The profile to set for the model.
     * @return The model with the new profile set.
     */
    CgmesMetadataModelImpl setProfile(String profile);

    /**
     * Extend model profiles with the given ones.
     * @param profiles The profiles to add for the model.
     * @return The model with the new profiles added.
     */
    CgmesMetadataModelImpl addProfiles(Collection<String> profiles);

    /**
     * Set the given model description.
     * @param description The description to set for the model.
     * @return The model with the new description set.
     */
    CgmesMetadataModelImpl setDescription(String description);

    /**
     * Add the given model id to the set of ids this model supersedes
     * @param id The additional model id this model should supersede.
     * @return The model with an updated set of values this model supersedes.
     */
    CgmesMetadataModelImpl addSupersedes(String id);

    /**
     * Add the given model id to the set of ids this model depends on.
     * @param id The additional model id this model should depend on.
     * @return The model with an updated set of values this model depends on.
     */
    CgmesMetadataModelImpl addDependentOn(String id);

    /**
     * Add the given model ids to the set of ids this model depends on.
     * @param dependentOn The additional model ids this model should depend on.
     * @return The model with an updated set of values this model depends on.
     */
    CgmesMetadataModelImpl addDependentOn(Collection<String> dependentOn);

    /**
     * Add the given model ids to the set of ids this model supersedes.
     * @param supersedes The additional model ids this model should supersede.
     * @return The model with an updated set of values this model supersedes.
     */
    CgmesMetadataModelImpl addSupersedes(Collection<String> supersedes);

    /**
     * Set the given modeling authority set.
     * @param modelingAuthoritySet The modeling authority set to set for the model.
     * @return The model with the new modeling authority set defined.
     */
    CgmesMetadataModelImpl setModelingAuthoritySet(String modelingAuthoritySet);

    /**
     * Remove all the model ids this model depends on.
     * @return The model with an empty set of values this model depends on.
     */
    CgmesMetadataModelImpl clearDependencies();

    /**
     * Remove all the model ids this model supersedes.
     * @return The model with an empty set of values this model supersedes.
     */
    CgmesMetadataModelImpl clearSupersedes();
}
