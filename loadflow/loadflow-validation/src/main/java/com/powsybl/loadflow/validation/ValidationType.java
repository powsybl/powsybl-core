/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
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
    private static final String UNEXPECTED_VALIDATION_TYPE_ERROR = "Unexpected ValidationType value: ";
    private static final Supplier<TableFormatterConfig> TABLE_FORMATTER_CONFIG = Suppliers.memoize(TableFormatterConfig::load);


    ValidationType(String file) {
        this.file = Objects.requireNonNull(file);
    }

    public boolean check(Network network, ValidationConfig validationConfig, TableFormatterConfig tableFormatterConfig, Path folder) throws IOException {
        Objects.requireNonNull(network);
        Objects.requireNonNull(validationConfig);
        Objects.requireNonNull(folder);
        switch (this) {
            case FLOWS:
                return FlowsValidation.checkFlows(network, validationConfig, tableFormatterConfig, folder.resolve(file));
            case GENERATORS:
                return GeneratorsValidation.checkGenerators(network, validationConfig, tableFormatterConfig, folder.resolve(file));
            case BUSES:
                return BusesValidation.checkBuses(network, validationConfig, tableFormatterConfig, folder.resolve(file));
            case SVCS:
                return StaticVarCompensatorsValidation.checkSVCs(network, validationConfig, tableFormatterConfig, folder.resolve(file));
            case SHUNTS:
                return ShuntCompensatorsValidation.checkShunts(network, validationConfig, tableFormatterConfig, folder.resolve(file));
            case TWTS:
                return TransformersValidation.checkTransformers(network, validationConfig, tableFormatterConfig, folder.resolve(file));
            case TWTS3W:
                return Transformers3WValidation.checkTransformers(network, validationConfig, tableFormatterConfig, folder.resolve(file));
            default:
                throw new AssertionError(UNEXPECTED_VALIDATION_TYPE_ERROR + this);
        }
    }

    public boolean check(Network network, ValidationConfig config, Path folder) throws IOException {
        return check(network, config, TABLE_FORMATTER_CONFIG.get(), folder);
    }

    public boolean check(Network network, ValidationConfig config, ValidationWriter validationWriter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(validationWriter);
        switch (this) {
            case FLOWS:
                return FlowsValidation.checkFlows(network, config, validationWriter);
            case GENERATORS:
                return GeneratorsValidation.checkGenerators(network, config, validationWriter);
            case BUSES:
                return BusesValidation.checkBuses(network, config, validationWriter);
            case SVCS:
                return StaticVarCompensatorsValidation.checkSVCs(network, config, validationWriter);
            case SHUNTS:
                return ShuntCompensatorsValidation.checkShunts(network, config, validationWriter);
            case TWTS:
                return TransformersValidation.checkTransformers(network, config, validationWriter);
            case TWTS3W:
                return Transformers3WValidation.checkTransformers(network, config, validationWriter);
            default:
                throw new AssertionError(UNEXPECTED_VALIDATION_TYPE_ERROR + this);
        }
    }

    public Path getOutputFile(Path folder) {
        return folder.resolve(file);
    }

}
