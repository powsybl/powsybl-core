/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
        faultOptions.forEach(option -> this.options.put(option.getId(), option));
    }

    public FaultOptions getOptions(String id) {
        return options.get(id);
    }
}
