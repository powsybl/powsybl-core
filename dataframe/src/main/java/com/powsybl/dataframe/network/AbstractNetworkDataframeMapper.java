/**
 * Copyright (c) 2021-2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network;

import com.powsybl.dataframe.*;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public abstract class AbstractNetworkDataframeMapper<T> extends AbstractDataframeMapper<Network, T>
    implements NetworkDataframeMapper {

    private final boolean addProperties;

    protected AbstractNetworkDataframeMapper(List<SeriesMapper<T>> seriesMappers, boolean addProperties) {
        super(seriesMappers);
        this.addProperties = addProperties;
    }

    @Override
    public void createDataframe(Network network, DataframeHandler dataframeHandler, DataframeFilter dataframeFilter) {
        List<T> items = getFilteredItems(network, dataframeFilter);
        List<SeriesMapper<T>> mappers = new ArrayList<>(getSeriesMappers(dataframeFilter));
        if (addProperties) {
            mappers.addAll(getPropertiesSeries(items, dataframeFilter));
        }
        dataframeHandler.allocate(mappers.size());
        mappers.stream().forEach(mapper -> mapper.createSeries(items, dataframeHandler));
    }

    protected List<T> getFilteredItems(Network network, DataframeFilter dataframeFilter) {
        Optional<UpdatingDataframe> optSelectDf = dataframeFilter.getSelectingDataframe();
        if (optSelectDf.isEmpty()) {
            return getItems(network);
        } else {
            UpdatingDataframe selectedDataframe = optSelectDf.get();
            return IntStream.range(0, selectedDataframe.getRowCount())
                .mapToObj(i -> getItem(network, selectedDataframe, i))
                .toList();
        }
    }

    private List<? extends SeriesMapper<T>> getPropertiesSeries(List<T> items, DataframeFilter dataframeFilter) {
        Stream<String> propertyNames = items.stream()
            .map(Identifiable.class::cast)
            .filter(Identifiable::hasProperty)
            .flatMap(e -> e.getPropertyNames().stream())
            .distinct();
        return propertyNames
            .map(property -> new StringSeriesMapper<T>(property, t -> ((Identifiable) t).getProperty(property), false))
            .filter(mapper -> filterMapper(mapper, dataframeFilter))
            .toList();
    }
}
