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
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

import java.util.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CgmesMetadataModelsImpl extends AbstractExtension<Network> implements CgmesMetadataModels {

    private final List<CgmesMetadataModel> models = new ArrayList<>();
    private final EnumMap<CgmesSubset, CgmesMetadataModel> subsetModel = new EnumMap<>(CgmesSubset.class);

    CgmesMetadataModelsImpl(Set<CgmesMetadataModel> models) {
        this.models.addAll(models);
        models.forEach(m -> subsetModel.put(m.getSubset(), m));
    }

    @Override
    public Optional<CgmesMetadataModel> getModelForSubset(CgmesSubset subset) {
        return Optional.ofNullable(subsetModel.get(subset));
    }

    @Override
    public Optional<CgmesMetadataModel> getModelForSubsetModelingAuthoritySet(CgmesSubset subset, String modelingAuthoritySet) {
        return models.stream()
                .filter(m -> m.getSubset().equals(subset) && m.getModelingAuthoritySet().equals(modelingAuthoritySet))
                .findFirst();
    }

    @Override
    public Collection<CgmesMetadataModel> getModels() {
        return Collections.unmodifiableCollection(models);
    }

    @Override
    public List<CgmesMetadataModel> getSortedModels() {
        return models.stream().sorted(
                Comparator.comparing(CgmesMetadataModel::getModelingAuthoritySet)
                        .thenComparing(CgmesMetadataModel::getSubset)
                        .thenComparing(CgmesMetadataModel::getVersion)
                        .thenComparing(CgmesMetadataModel::getId)
        ).toList();
    }
}
