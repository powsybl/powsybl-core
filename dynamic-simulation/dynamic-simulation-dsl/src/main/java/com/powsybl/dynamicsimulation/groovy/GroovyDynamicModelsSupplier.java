/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.dynamicsimulation.groovy;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.codehaus.groovy.control.CompilerConfiguration;

import com.powsybl.dsl.ExpressionDslLoader;
import com.powsybl.dsl.GroovyScripts;
import com.powsybl.dynamicsimulation.DynamicModel;
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.iidm.network.Network;

import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class GroovyDynamicModelsSupplier implements DynamicModelsSupplier {

    private final GroovyCodeSource codeSource;

    private final List<DynamicModelGroovyExtension> extensions;

    public GroovyDynamicModelsSupplier(Path path) {
        this(path, Collections.emptyList());
    }

    public GroovyDynamicModelsSupplier(Path path, List<DynamicModelGroovyExtension> extensions) {
        this.codeSource = GroovyScripts.load(path);
        this.extensions = Objects.requireNonNull(extensions);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<DynamicModel> get(Network network) {
        List<DynamicModel> dynamicModels = new ArrayList<>();

        Binding binding = new Binding();
        binding.setVariable("network", network);

        ExpressionDslLoader.prepareClosures(binding);
        extensions.forEach(e -> e.load(binding, dynamicModels::add));

        GroovyShell shell = new GroovyShell(binding, new CompilerConfiguration());
        shell.evaluate(codeSource);

        return dynamicModels;
    }
}
