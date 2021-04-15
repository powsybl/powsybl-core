/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools.autocompletion;

import java.util.Objects;
import java.util.Optional;

/**
 * Simplified option model for completion script generation.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
 */
public class BashOption {

    private final String name;

    private final String argName;

    private OptionType type;

    public BashOption(String name) {
        this(name, null, null);
    }

    public BashOption(String name, String argName) {
        this(name, argName, null);
    }

    public BashOption(String name, String argName, OptionType type) {
        this.name = Objects.requireNonNull(name);
        this.argName = argName;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public boolean hasArg() {
        return argName != null;
    }

    public Optional<String> getArgName() {
        return Optional.ofNullable(argName);
    }

    public OptionType getType() {
        return type;
    }

    public void setType(OptionType type) {
        this.type = type;
    }
}
