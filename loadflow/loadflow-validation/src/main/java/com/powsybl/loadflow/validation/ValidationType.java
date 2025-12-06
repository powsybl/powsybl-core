/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public enum ValidationType {
    FLOWS("branches_flows.csv"),
    GENERATORS("generators.csv"),
    BUSES("buses.csv"),
    SVCS("svcs.csv"),
    SHUNTS("shunts.csv"),
    TWTS("twt.csv"),
    TWTS3W("twt3w.csv");

    private final String file;

    ValidationType(String file) {
        this.file = Objects.requireNonNull(file);
    }

    public boolean check(Network network, ValidationConfig config, Path folder) throws IOException {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(folder);
        return switch (this) {
            case FLOWS -> FlowsValidation.checkFlows(network, config, folder.resolve(file));
            case GENERATORS -> GeneratorsValidation.checkGenerators(network, config, folder.resolve(file));
            case BUSES -> BusesValidation.checkBuses(network, config, folder.resolve(file));
            case SVCS -> StaticVarCompensatorsValidation.checkSVCs(network, config, folder.resolve(file));
            case SHUNTS -> ShuntCompensatorsValidation.checkShunts(network, config, folder.resolve(file));
            case TWTS -> TransformersValidation.checkTransformers(network, config, folder.resolve(file));
            case TWTS3W -> Transformers3WValidation.checkTransformers(network, config, folder.resolve(file));
        };
    }

    public boolean check(Network network, ValidationConfig config, ValidationWriter validationWriter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(validationWriter);
        return switch (this) {
            case FLOWS -> FlowsValidation.checkFlows(network, config, validationWriter);
            case GENERATORS -> GeneratorsValidation.checkGenerators(network, config, validationWriter);
            case BUSES -> BusesValidation.checkBuses(network, config, validationWriter);
            case SVCS -> StaticVarCompensatorsValidation.checkSVCs(network, config, validationWriter);
            case SHUNTS -> ShuntCompensatorsValidation.checkShunts(network, config, validationWriter);
            case TWTS -> TransformersValidation.checkTransformers(network, config, validationWriter);
            case TWTS3W -> Transformers3WValidation.checkTransformers(network, config, validationWriter);
        };
    }

    public Path getOutputFile(Path folder) {
        return folder.resolve(file);
    }

}
