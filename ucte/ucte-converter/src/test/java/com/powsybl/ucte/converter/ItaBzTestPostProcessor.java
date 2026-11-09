/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.Network;
import com.powsybl.ucte.network.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Antoine Limouzin {@literal <antoine.limouzin at rte.com>}
 */
@AutoService(UcteImportPostProcessor.class)
public class ItaBzTestPostProcessor implements UcteImportPostProcessor {

    private static final String COMMENTS_REGEX = "(?<zone>[A-Z_.]*) (?<count>\\d*) Nodes First Node: (?<first>[A-Z1-9_.]*), Last Node: (?<last>[A-Z1-9_.]*)";
    private static final Pattern BZ_INFO_REGEX = Pattern.compile(COMMENTS_REGEX);
    private final Map<String, Pair<String, String>> firstAndLastNodesByBiddingZone = new HashMap<>();
    private final Map<String, Integer> nodeCountByBiddingZones = new HashMap<>();

    @Override
    public String getName() {
        return "test-ita-bz";
    }

    @Override
    public void process(Network network, UcteNetwork ucteNetwork) {
        ucteNetwork.getComments().forEach(line -> {
            final Matcher bzInfo = BZ_INFO_REGEX.matcher(line);
            if (bzInfo.matches()) {
                firstAndLastNodesByBiddingZone.putIfAbsent(bzInfo.group("zone"),
                        Pair.of(bzInfo.group("first"), bzInfo.group("last")));
                nodeCountByBiddingZones.putIfAbsent(bzInfo.group("zone"),
                        Integer.parseInt(bzInfo.group("count")));
            }
        });

        final List<UcteNode> italianNodes = ucteNetwork.getNodes().stream()
                .filter(ItaBzTestPostProcessor::isItalian)
                .toList();

        if (italianNodes.isEmpty()) {
            return;
        }
        if (nodeCountByBiddingZones.values().stream().mapToInt(n -> n).sum() != italianNodes.size()) {
            throw new UcteException("Unexpected number of nodes");
        }

        final Network.BusBreakerView busBreakerView = network.getBusBreakerView();

        int startOfBlock = 0;

        while (startOfBlock < italianNodes.size()) {
            final UcteNode firstNodeOfBlock = italianNodes.get(startOfBlock);
            final String currentBiddingZone = getBiddingZone(firstNodeOfBlock);
            int expectedCount = nodeCountByBiddingZones.get(currentBiddingZone);

            if (!isLastOfZone(italianNodes.get(startOfBlock + expectedCount - 1), currentBiddingZone)) {
                throw new UcteException("Invalid bidding zone grouping");
            }

            IntStream.range(0, expectedCount)
                    .mapToObj(italianNodes::get)
                    .map(UcteNode::getCode)
                    .map(UcteNodeCode::toString)
                    .map(busBreakerView::getBus)
                    .forEach(bus -> bus.setProperty("italianBiddingZone", currentBiddingZone));

            startOfBlock += expectedCount + 1;
        }
    }

    private static boolean isItalian(final UcteNode ucteNode) {
        return ucteNode.getCode().getUcteCountryCode().equals(UcteCountryCode.IT);
    }

    private boolean isLastOfZone(final UcteNode ucteNode, final String biddingZone) {
        return firstAndLastNodesByBiddingZone.get(biddingZone).getRight().equals(ucteNode.getCode().toString());
    }

    private String getBiddingZone(final UcteNode firstOfBlock) {
        final String name = firstOfBlock.getCode().toString();
        return firstAndLastNodesByBiddingZone.entrySet().stream()
                .filter(e -> e.getValue().getLeft().equals(name))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new UcteException("%s does not belong to a bidding zone".formatted(name)));
    }

}
