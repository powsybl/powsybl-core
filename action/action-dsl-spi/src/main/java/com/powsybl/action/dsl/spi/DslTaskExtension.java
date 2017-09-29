/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.spi;

import com.powsybl.contingency.tasks.ModificationTask;
import groovy.lang.Binding;
import groovy.lang.MetaClass;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface DslTaskExtension {

    void addToSpec(MetaClass tasksSpecMetaClass, List<ModificationTask> tasks, Binding binding);
}

