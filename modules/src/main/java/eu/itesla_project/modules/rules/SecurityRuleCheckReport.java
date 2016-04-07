/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules;

import eu.itesla_project.modules.histo.HistoDbAttributeId;

import java.io.Serializable;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityRuleCheckReport implements Serializable {

    private final boolean safe;

    private final List<HistoDbAttributeId> missingAttributes;

    public SecurityRuleCheckReport(boolean safe, List<HistoDbAttributeId> missingAttributes) {
        this.safe = safe;
        this.missingAttributes = missingAttributes;
    }

    public boolean isSafe() {
        return safe;
    }

    public List<HistoDbAttributeId> getMissingAttributes() {
        return missingAttributes;
    }

}
