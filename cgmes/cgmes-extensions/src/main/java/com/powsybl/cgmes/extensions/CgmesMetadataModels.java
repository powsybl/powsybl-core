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
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
public interface CgmesMetadataModels extends Extension<Network> {

    String NAME = "cgmesMetadataModels";

    Collection<CgmesMetadataModel> getModels();

    List<CgmesMetadataModel> getSortedModels();

    Optional<CgmesMetadataModel> getModelForSubset(CgmesSubset subset);

    Optional<CgmesMetadataModel> getModelForSubsetModelingAuthoritySet(CgmesSubset subset, String modelingAuthoritySet);

    @Override
    default String getName() {
        return NAME;
    }
}
