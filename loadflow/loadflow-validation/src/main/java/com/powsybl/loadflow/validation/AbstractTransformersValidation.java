/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.validation.io.ValidationWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractTransformersValidation {

    protected static final Supplier<TableFormatterConfig> TABLE_FORMATTER_CONFIG = Suppliers.memoize(TableFormatterConfig::load);

    public boolean checkTransformers(Network network, ValidationConfig validationConfig, TableFormatterConfig formatterConfig, Path file) throws IOException {
        Objects.requireNonNull(network);
        Objects.requireNonNull(validationConfig);
        Objects.requireNonNull(formatterConfig);
        Objects.requireNonNull(file);

        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkTransformers(network, validationConfig, formatterConfig, writer);
        }
    }

    public boolean checkTransformers(Network network, ValidationConfig config, Path file) throws IOException {
        return checkTransformers(network, config, TABLE_FORMATTER_CONFIG.get(), file);
    }

    public boolean checkTransformers(Network network, ValidationConfig validationConfig, TableFormatterConfig formatterConfig, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(validationConfig);
        Objects.requireNonNull(formatterConfig);
        Objects.requireNonNull(writer);

        try (ValidationWriter twtsWriter = ValidationUtils.createValidationWriter(network.getId(), validationConfig, formatterConfig, writer, ValidationType.TWTS3W)) {
            return checkTransformers(network, validationConfig, twtsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean checkTransformers(Network network, ValidationConfig config, Writer writer) {
        return checkTransformers(network, config, TABLE_FORMATTER_CONFIG.get(), writer);
    }

    public abstract boolean checkTransformers(Network network, ValidationConfig config, ValidationWriter twtsWriter);
}
