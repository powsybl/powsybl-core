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

    private static final String UNEXPECTED_VALIDATION_TYPE_VALUE = "Unexpected ValidationType value: ";

    public boolean check(Network network, ValidationConfig config, Path folder) throws IOException {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(folder);
        switch (this) {
            case FLOWS:
                return FlowsValidation.INSTANCE.checkFlows(network, config, folder.resolve(file));
            case GENERATORS:
                return GeneratorsValidation.INSTANCE.checkGenerators(network, config, folder.resolve(file));
            case BUSES:
                return BusesValidation.INSTANCE.checkBuses(network, config, folder.resolve(file));
            case SVCS:
                return StaticVarCompensatorsValidation.INSTANCE.checkSVCs(network, config, folder.resolve(file));
            case SHUNTS:
                return ShuntCompensatorsValidation.INSTANCE.checkShunts(network, config, folder.resolve(file));
            case TWTS:
                return TransformersValidation.INSTANCE.checkTransformers(network, config, folder.resolve(file));
            case TWTS3W:
                return Transformers3WValidation.INSTANCE.checkTransformers(network, config, folder.resolve(file));
            default:
                throw new IllegalStateException(UNEXPECTED_VALIDATION_TYPE_VALUE + this);
        }
    }

    public boolean check(Network network, ValidationConfig config, ValidationWriter validationWriter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(validationWriter);
        switch (this) {
            case FLOWS:
                return FlowsValidation.INSTANCE.checkFlows(network, config, validationWriter);
            case GENERATORS:
                return GeneratorsValidation.INSTANCE.checkGenerators(network, config, validationWriter);
            case BUSES:
                return BusesValidation.INSTANCE.checkBuses(network, config, validationWriter);
            case SVCS:
                return StaticVarCompensatorsValidation.INSTANCE.checkSVCs(network, config, validationWriter);
            case SHUNTS:
                return ShuntCompensatorsValidation.INSTANCE.checkShunts(network, config, validationWriter);
            case TWTS:
                return TransformersValidation.INSTANCE.checkTransformers(network, config, validationWriter);
            case TWTS3W:
                return Transformers3WValidation.INSTANCE.checkTransformers(network, config, validationWriter);
            default:
                throw new IllegalStateException(UNEXPECTED_VALIDATION_TYPE_VALUE + this);
        }
    }

    public Path getOutputFile(Path folder) {
        return folder.resolve(file);
    }

}
