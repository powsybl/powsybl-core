/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * <p>Interface for adders that should handle properties.</p>
 *
 * @param <T> the type of the object that is created by the adder. Properties are added to this object.
 * @param <S> the type of the current adder
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface AdderWithProperties<T, S> extends AdderWithPostCreationTasks<T> {
    S addProperty(String key, String value);
}
