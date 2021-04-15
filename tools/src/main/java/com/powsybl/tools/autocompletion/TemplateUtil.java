/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools.autocompletion;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
 */
public class TemplateUtil {

    public TemplateUtil() {
    }

    public Enum<?>[] getEnumOptionTypeValues(BashOption option) {
        return ((OptionType.Enumeration) option.getType()).getClazz().getEnumConstants();
    }
}
