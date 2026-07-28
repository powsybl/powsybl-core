/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc;

import com.powsybl.iidm.network.Branch;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class ImporterContext {
    private final Map<String, AssessedElementContext> importedAssessedElements;

    public ImporterContext() {
        this.importedAssessedElements = new HashMap<>();
    }

    public void addImportedAssessedElement(String id, Branch<?> branch, boolean inBaseCase, Set<String> contingencies) {
        this.importedAssessedElements.put(id, new AssessedElementContext(id, branch, inBaseCase, contingencies));
    }

    public Map<String, AssessedElementContext> getImportedAssessedElements() {
        return importedAssessedElements;
    }

    public record AssessedElementContext(String id, Branch<?> branch, boolean inBaseCase, Set<String> contingencies) {
    }
}
