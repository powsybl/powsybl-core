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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class CgmesMetadataAdderImpl extends AbstractExtensionAdder<Network, CgmesMetadata> implements CgmesMetadataAdder {

    enum Type {
        EQ, TP, SSH, SV
    }

    class ModelAdderImpl implements ModelAdder {

        private final Type type;

        private String id;
        private String description;
        private int version = 0;
        private final List<String> dependencies = new ArrayList<>();
        private String modelingAuthoritySet;

        ModelAdderImpl(Type type) {
            this.type = Objects.requireNonNull(type);
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
        public CgmesMetadataAdderImpl add() {
            if (id == null) {
                throw new PowsyblException(type + " id is undefined");
            }
            if (type != Type.EQ && dependencies.isEmpty()) {
                throw new PowsyblException(type + " dependencies must have at least one dependency");
            }
            if (modelingAuthoritySet == null) {
                throw new PowsyblException(type + " modelingAuthoritySet is undefined");
            }
            CgmesMetadataImpl.ModelImpl model = new CgmesMetadataImpl.ModelImpl(id, description, version, dependencies, modelingAuthoritySet);
            switch (type) {
                case EQ:
                    eq = model;
                    break;
                case TP:
                    tp = model;
                    break;
                case SSH:
                    ssh = model;
                case SV:
                    sv = model;
            }
            return CgmesMetadataAdderImpl.this;
        }
    }

    private CgmesMetadataImpl.ModelImpl eq;
    private CgmesMetadataImpl.ModelImpl tp;
    private CgmesMetadataImpl.ModelImpl ssh;
    private CgmesMetadataImpl.ModelImpl sv;

    CgmesMetadataAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    public ModelAdder newEq() {
        return new ModelAdderImpl(Type.EQ);
    }

    @Override
    public ModelAdder newTp() {
        return new ModelAdderImpl(Type.TP);
    }

    @Override
    public ModelAdder newSsh() {
        return new ModelAdderImpl(Type.SSH);
    }

    @Override
    public ModelAdder newSv() {
        return new ModelAdderImpl(Type.SV);
    }

    @Override
    protected CgmesMetadata createExtension(Network extendable) {
        if (eq == null) {
            throw new PowsyblException("EQ metadata cannot be null");
        }
        return new CgmesMetadataImpl(eq, tp, ssh, sv);
    }
}
