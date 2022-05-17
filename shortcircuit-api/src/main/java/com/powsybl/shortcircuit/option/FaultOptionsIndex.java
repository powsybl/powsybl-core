/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.option;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FaultOptionsIndex {
    private final Map<String, FaultOptions> options = new HashMap<>();

    public FaultOptionsIndex(List<FaultOptions> faultOptions) {
        faultOptions.forEach(option -> {
            String id = option.getFaultContext().getId();
            if (id != null) {
                this.options.merge(id, option, FaultOptions::merge);
            }
        });
    }

    public Map<String, FaultOptions> getOptions() {
        return options;
    }
}
