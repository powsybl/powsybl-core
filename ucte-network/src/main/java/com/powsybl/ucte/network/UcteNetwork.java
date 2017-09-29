/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface UcteNetwork {

    void setVersion(UcteFormatVersion version);

    UcteFormatVersion getVersion();

    List<String> getComments();

    void addNode(UcteNode node);

    Collection<UcteNode> getNodes();

    UcteNode getNode(UcteNodeCode code);

    void addLine(UcteLine line);

    Collection<UcteLine> getLines();

    UcteLine getLine(UcteElementId id);

    void addTransformer(UcteTransformer transformer);

    Collection<UcteTransformer> getTransformers();

    UcteTransformer getTransformer(UcteElementId id);

    void addRegulation(UcteRegulation regulation);

    Collection<UcteRegulation> getRegulations();

    UcteRegulation getRegulation(UcteElementId transfoId);

    void fix();

}
