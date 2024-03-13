/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.dynamicsimulation.groovy;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.powsybl.commons.report.ReportNode;
import org.codehaus.groovy.control.CompilerConfiguration;

import com.powsybl.dsl.ExpressionDslLoader;
import com.powsybl.dsl.GroovyScripts;
import com.powsybl.dynamicsimulation.EventModelsSupplier;
import com.powsybl.dynamicsimulation.EventModel;
import com.powsybl.iidm.network.Network;

import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class GroovyEventModelsSupplier implements EventModelsSupplier {

    private final GroovyCodeSource codeSource;

    private final List<EventModelGroovyExtension> extensions;

    public GroovyEventModelsSupplier(Path path) {
        this(path, Collections.emptyList());
    }

    public GroovyEventModelsSupplier(Path path, List<EventModelGroovyExtension> extensions) {
        this.codeSource = GroovyScripts.load(path);
        this.extensions = Objects.requireNonNull(extensions);
    }

    public GroovyEventModelsSupplier(InputStream is, List<EventModelGroovyExtension> extensions) {
        this.codeSource = GroovyScripts.load(is);
        this.extensions = Objects.requireNonNull(extensions);
    }

    @Override
    public List<EventModel> get(Network network, ReportNode reportNode) {
        List<EventModel> eventModels = new ArrayList<>();
        ReportNode groovyReportNode = reportNode.newReportNode().withMessageTemplate("groovyEventModels", "Groovy Event Models Supplier").add();

        Binding binding = new Binding();
        binding.setVariable("network", network);

        ExpressionDslLoader.prepareClosures(binding);
        extensions.forEach(e -> e.load(binding, eventModels::add, groovyReportNode));

        GroovyShell shell = new GroovyShell(binding, new CompilerConfiguration());
        shell.evaluate(codeSource);

        return eventModels;
    }
}
