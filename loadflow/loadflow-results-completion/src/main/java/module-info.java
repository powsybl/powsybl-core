/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.loadflow.results.completion {
    requires static com.google.auto.service;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires transitive com.powsybl.iidm.api;
    requires com.powsybl.iidm.extensions;
    requires transitive com.powsybl.loadflow.api;
    requires transitive com.powsybl.loadflow.validation;
    requires transitive org.jgrapht.core;
    requires org.slf4j;

    exports com.powsybl.loadflow.resultscompletion;
    exports com.powsybl.loadflow.resultscompletion.z0flows;

    provides com.powsybl.iidm.network.ImportPostProcessor with
        com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletionPostProcessor;
    provides com.powsybl.loadflow.validation.CandidateComputation with
        com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletion;
}
