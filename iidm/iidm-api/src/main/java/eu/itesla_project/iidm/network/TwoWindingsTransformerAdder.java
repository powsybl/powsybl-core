/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TwoWindingsTransformerAdder extends TwoTerminalsConnectableAdder<TwoWindingsTransformerAdder> {

    TwoWindingsTransformerAdder setR(float r);

    TwoWindingsTransformerAdder setX(float x);

    TwoWindingsTransformerAdder setB(float b);

    TwoWindingsTransformerAdder setG(float g);

    TwoWindingsTransformerAdder setRatedU1(float ratedU1);

    TwoWindingsTransformerAdder setRatedU2(float ratedU2);

    TwoWindingsTransformer add();

}
