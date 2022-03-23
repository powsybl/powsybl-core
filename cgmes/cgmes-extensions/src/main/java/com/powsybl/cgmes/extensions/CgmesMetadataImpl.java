/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class CgmesMetadataImpl extends AbstractExtension<Network> implements CgmesMetadata {

    static class ModelImpl implements Model {

        private final String id;
        private final String description;
        private final int version;
        private final List<String> dependencies = new ArrayList<>();
        private final String modelingAuthoritySet;

        ModelImpl(String id, String description, int version, List<String> dependencies, String modelingAuthoritySet) {
            this.id = id;
            this.description = description;
            this.version = version;
            this.dependencies.addAll(dependencies);
            this.modelingAuthoritySet = modelingAuthoritySet;
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
    }

    private final Model eq;
    private final Model tp;
    private final Model ssh;
    private final Model sv;

    CgmesMetadataImpl(Model eq, Model tp, Model ssh, Model sv) {
        this.eq = eq;
        this.tp = tp;
        this.ssh = ssh;
        this.sv = sv;
    }

    @Override
    public Model getEq() {
        return eq;
    }

    @Override
    public Optional<Model> getTp() {
        return Optional.ofNullable(tp);
    }

    @Override
    public Optional<Model> getSsh() {
        return Optional.ofNullable(ssh);
    }

    @Override
    public Optional<Model> getSv() {
        return Optional.ofNullable(sv);
    }
}
