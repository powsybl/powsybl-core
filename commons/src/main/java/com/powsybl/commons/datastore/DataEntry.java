/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class DataEntry {

    private final String name;

    private final List<String> tags = new ArrayList<>();

    public DataEntry(String name, String... tags) {
        this.name = Objects.requireNonNull(name);
        if (tags != null)  {
            this.tags.addAll(Arrays.asList(tags));
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

}
