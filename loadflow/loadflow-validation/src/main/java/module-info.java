/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.loadflow.validation {
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires transitive com.powsybl.iidm.api;
    requires com.powsybl.iidm.extensions;
    requires transitive com.powsybl.loadflow.api;
    requires transitive com.powsybl.tools;
    requires transitive org.apache.commons.cli;
    requires org.apache.commons.lang3;
    requires org.slf4j;

    exports com.powsybl.loadflow.validation;
    exports com.powsybl.loadflow.validation.extension;
    exports com.powsybl.loadflow.validation.io;

    uses com.powsybl.loadflow.validation.CandidateComputation;
    uses com.powsybl.loadflow.validation.extension.ExtensionValidation;

    provides com.powsybl.commons.plugins.PluginInfo with
        com.powsybl.loadflow.validation.CandidateComputationPluginInfo;
    provides com.powsybl.loadflow.validation.CandidateComputation with
        com.powsybl.loadflow.validation.LoadFlowComputation;
    provides com.powsybl.tools.Tool with
        com.powsybl.loadflow.validation.ValidationTool;
}
