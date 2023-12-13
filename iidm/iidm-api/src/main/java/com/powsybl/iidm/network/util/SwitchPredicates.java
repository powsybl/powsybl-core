/*
 * Copyright (c) 2023. , RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class SwitchPredicates {

    private SwitchPredicates() { }

    public static final Predicate<Switch> IS_NONFICTIONAL_CLOSED_BREAKER = switchObject -> switchObject != null && switchObject.getKind() == SwitchKind.BREAKER && !switchObject.isOpen() && !switchObject.isFictitious();
    public static final Predicate<Switch> IS_NONFICTIONAL_BREAKER = switchObject -> switchObject != null && switchObject.getKind() == SwitchKind.BREAKER && !switchObject.isFictitious();
    public static final Predicate<Switch> IS_CLOSED_BREAKER = switchObject -> switchObject != null && switchObject.getKind() == SwitchKind.BREAKER && !switchObject.isOpen();
    public static final Predicate<Switch> IS_BREAKER_OR_DISCONNECTOR = switchObject -> switchObject != null && (switchObject.getKind() == SwitchKind.BREAKER || switchObject.getKind() == SwitchKind.DISCONNECTOR);
    public static final Predicate<Switch> IS_OPEN_DISCONNECTOR = switchObject -> switchObject != null && switchObject.getKind() == SwitchKind.DISCONNECTOR && switchObject.isOpen();
    public static final Predicate<Switch> IS_BREAKER = switchObject -> switchObject != null && switchObject.getKind() == SwitchKind.BREAKER;
    public static final Predicate<Switch> IS_NONFICTIONAL = switchObject -> switchObject != null && !switchObject.isFictitious();
    public static final Predicate<Switch> IS_OPEN = switchObject -> switchObject != null && switchObject.isOpen();
    public static final Predicate<Switch> IS_NON_NULL = Objects::nonNull;
}
