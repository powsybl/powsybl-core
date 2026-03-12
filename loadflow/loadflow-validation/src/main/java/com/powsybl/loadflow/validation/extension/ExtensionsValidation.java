/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation.extension;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.validation.ValidationConfig;
import com.powsybl.tools.ToolRunningContext;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
public class ExtensionsValidation {

    private static final Supplier<List<ExtensionValidation>> EXTENSIONS = Suppliers.memoize(() -> Lists.newArrayList(
            ServiceLoader.load(ExtensionValidation.class, ExtensionsValidation.class.getClassLoader())));

    public static List<ExtensionValidation> getExtensions() {
        return EXTENSIONS.get();
    }

    public static List<String> getExtensionsNames() {
        return getExtensions().stream().map(ExtensionValidation::getName).toList();
    }

    public static Optional<ExtensionValidation> getExtension(String name) {
        return getExtensions().stream().filter(v -> v.getName().equals(name)).findFirst();
    }

    public void runExtensionValidations(Network network, ValidationConfig config, Path outputFolder, ToolRunningContext context) {

        getExtensions().forEach(extensionValidation -> {
            try {
                boolean success = extensionValidation.check(network, config, extensionValidation.getOutputFile(outputFolder));
                String message = "Validate extension behaviour of network " + network.getId()
                        + " - extension validation type: " + extensionValidation.getType()
                        + " - result: " + (success ? "success" : "fail");
                context.getOutputStream().println(message);
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        });
    }
}
