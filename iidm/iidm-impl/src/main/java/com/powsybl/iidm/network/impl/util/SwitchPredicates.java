/*
 * Copyright (c) 2023. , RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.network.impl.util;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;

import java.util.Objects;
import java.util.function.Predicate;

public final class SwitchPredicates {

    private SwitchPredicates() { }

    public static final Predicate<Switch> IS_NONFICTIONAL_CLOSED_BREAKER = switchObject -> switchObject != null && switchObject.getKind() == SwitchKind.BREAKER && !switchObject.isOpen() && !switchObject.isFictitious();
    public static final Predicate<Switch> IS_NONFICTIONAL_BREAKER = switchObject -> switchObject != null && switchObject.getKind() == SwitchKind.BREAKER && !switchObject.isFictitious();
    public static final Predicate<Switch> IS_CLOSED_BREAKER = switchObject -> switchObject != null && switchObject.getKind() == SwitchKind.BREAKER && !switchObject.isOpen();
    public static final Predicate<Switch> IS_BREAKER_OR_DISCONNECTOR = switchObject -> switchObject != null && (switchObject.getKind() == SwitchKind.BREAKER || switchObject.getKind() == SwitchKind.DISCONNECTOR);
    public static final Predicate<Switch> IS_NON_NULL = Objects::nonNull;
}
