/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * An AC line.
 * <p>
 * The equivalent &#960; model used is:
 * <div>
 *    <object data="doc-files/line.svg" type="image/svg+xml"></object>
 * </div>
 * To create a line, see {@link LineAdder}
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see LineAdder
 */
public interface Line extends Branch<Line>, LineCharacteristics<Line> {

    boolean isTieLine();

}
