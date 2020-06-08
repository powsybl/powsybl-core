/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.dynamicsimulation.groovy;

import com.powsybl.dsl.ExpressionDslLoader;
import com.powsybl.dsl.GroovyScripts;
import com.powsybl.dynamicsimulation.Curve;
import com.powsybl.dynamicsimulation.CurvesSupplier;
import com.powsybl.iidm.network.Network;
import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class GroovyCurvesSupplier implements CurvesSupplier {

    private final GroovyCodeSource codeSource;

    private final List<CurveGroovyExtension> extensions;

    public GroovyCurvesSupplier(Path path) {
        this(path, Collections.emptyList());
    }

    public GroovyCurvesSupplier(Path path, List<CurveGroovyExtension> extensions) {
        this.codeSource = GroovyScripts.load(path);
        this.extensions = Objects.requireNonNull(extensions);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<Curve> get(Network network) {
        List<Curve> curves = new ArrayList<>();

        Binding binding = new Binding();
        binding.setVariable("network", network);

        ExpressionDslLoader.prepareClosures(binding);
        extensions.forEach(e -> e.load(binding, curves::add));

        GroovyShell shell = new GroovyShell(binding, new CompilerConfiguration());
        shell.evaluate(codeSource);

        return curves;
    }
}
