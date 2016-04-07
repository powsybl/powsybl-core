/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.ucte.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteNetworkImpl implements UcteNetwork {

    private UcteFormatVersion version;

    private final List<String> comments = new ArrayList<>();

    private final Map<UcteNodeCode, UcteNode> nodes = new HashMap<>();

    private final Map<UcteElementId, UcteLine> lines = new HashMap<>();

    private final Map<UcteElementId, UcteTransformer> transformers = new HashMap<>();

    private final Map<UcteElementId, UcteRegulation> regulations = new HashMap<>();

    @Override
    public void setVersion(UcteFormatVersion version) {
        this.version = version;
    }

    @Override
    public UcteFormatVersion getVersion() {
        return version;
    }

    @Override
    public List<String> getComments() {
        return comments;
    }

    @Override
    public void addNode(UcteNode node) {
        nodes.put(node.getCode(), node);
    }

    @Override
    public Collection<UcteNode> getNodes() {
        return nodes.values();
    }

    @Override
    public UcteNode getNode(UcteNodeCode code) {
        return nodes.get(code);
    }

    @Override
    public void addLine(UcteLine line) {
        lines.put(line.getId(), line);
    }

    @Override
    public Collection<UcteLine> getLines() {
        return lines.values();
    }

    @Override
    public UcteLine getLine(UcteElementId id) {
        return lines.get(id);
    }

    @Override
    public void addTransformer(UcteTransformer transformer) {
        transformers.put(transformer.getId(), transformer);
    }

    @Override
    public Collection<UcteTransformer> getTransformers() {
        return transformers.values();
    }

    @Override
    public UcteTransformer getTransformer(UcteElementId id) {
        return transformers.get(id);
    }

    @Override
    public void addRegulation(UcteRegulation regulation) {
        regulations.put(regulation.getTransfoId(), regulation);
    }

    @Override
    public Collection<UcteRegulation> getRegulations() {
        return regulations.values();
    }

    @Override
    public UcteRegulation getRegulation(UcteElementId transfoId) {
        return regulations.get(transfoId);
    }

    @Override
    public void fix() {
        for (UcteNode node : nodes.values()) {
            node.fix();
        }
        for (UcteLine line : lines.values()) {
            line.fix();
        }
        for (UcteTransformer transfo : transformers.values()) {
            transfo.fix();
        }
        for (UcteRegulation regulation : regulations.values()) {
            regulation.fix();
        }
    }

}
