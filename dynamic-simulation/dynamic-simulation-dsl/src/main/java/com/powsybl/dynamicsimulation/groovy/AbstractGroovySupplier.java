/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.groovy;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.dsl.ExpressionDslLoader;
import com.powsybl.iidm.network.Network;
import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
abstract class AbstractGroovySupplier<T, R extends GroovyExtension<T>> {

    private final GroovyCodeSource codeSource;
    private final List<R> extensions;

    public AbstractGroovySupplier(GroovyCodeSource codeSource, List<R> extensions) {
        this.codeSource = codeSource;
        this.extensions = extensions;
    }

    protected List<T> evaluateScript(Network network, ReportNode reportNode) {
        List<T> outputVariables = new ArrayList<>();
        Binding binding = new Binding();
        binding.setVariable("network", network);
        ExpressionDslLoader.prepareClosures(binding);
        extensions.forEach(e -> e.load(binding, outputVariables::add, reportNode));
        GroovyShell shell = new GroovyShell(binding, new CompilerConfiguration());
        shell.evaluate(codeSource);
        return outputVariables;
    }

    protected ReportNode createReportNode(ReportNode reportNode, String key, String message) {
        return reportNode.newReportNode()
                .withMessageTemplate(key, message)
                .add();
    }
}
