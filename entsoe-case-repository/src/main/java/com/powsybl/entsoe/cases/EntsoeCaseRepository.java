/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.entsoe.cases;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import eu.itesla_project.cases.CaseRepository;
import eu.itesla_project.cases.CaseType;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.commons.datasource.ReadOnlyDataSource;
import eu.itesla_project.commons.datasource.ReadOnlyDataSourceFactory;
import eu.itesla_project.commons.datasource.GenericReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.entsoe.util.EntsoeFileName;
import eu.itesla_project.entsoe.util.EntsoeGeographicalCode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Common ENTSOE case repository layout:
 * <pre>
 * CIM/SN/2013/01/15/20130115_0620_SN2_FR0.zip
 *    /FO/...
 * UCT/SN/...
 *    /FO/...
 * </pre>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EntsoeCaseRepository implements CaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntsoeCaseRepository.class);

    static class EntsoeFormat {

        private final Importer importer;

        private final String dirName;

        EntsoeFormat(Importer importer, String dirName) {
            this.importer = Objects.requireNonNull(importer);
            this.dirName = Objects.requireNonNull(dirName);
        }

        Importer getImporter() {
            return importer;
        }

        String getDirName() {
            return dirName;
        }
    }

    private final EntsoeCaseRepositoryConfig config;

    private final List<EntsoeFormat> formats;

    private final ReadOnlyDataSourceFactory dataSourceFactory;

    public static CaseRepository create(ComputationManager computationManager) {
        return new EntsoeCaseRepository(EntsoeCaseRepositoryConfig.load(), computationManager);
    }

    EntsoeCaseRepository(EntsoeCaseRepositoryConfig config, List<EntsoeFormat> formats, ReadOnlyDataSourceFactory dataSourceFactory) {
        this.config = Objects.requireNonNull(config);
        this.formats = Objects.requireNonNull(formats);
        this.dataSourceFactory = Objects.requireNonNull(dataSourceFactory);
        LOGGER.info(config.toString());
    }

    EntsoeCaseRepository(EntsoeCaseRepositoryConfig config, ComputationManager computationManager) {
        this(config,
            Arrays.asList(new EntsoeFormat(Importers.getImporter("CIM1", computationManager), "CIM"),
                          new EntsoeFormat(Importers.getImporter("UCTE", computationManager), "UCT")), // official ENTSOE formats)
            (directory, baseName) -> new GenericReadOnlyDataSource(directory, baseName));
    }

    public EntsoeCaseRepositoryConfig getConfig() {
        return config;
    }

    private static final class ImportContext {
        private final Importer importer;
        private final ReadOnlyDataSource ds;

        private ImportContext(Importer importer, ReadOnlyDataSource ds) {
            this.importer = importer;
            this.ds = ds;
        }
    }

    // because D1 snapshot does not exist and forecast replacement is not yet implemented
    private static Collection<EntsoeGeographicalCode> forCountryHacked(Country country) {
        return EntsoeGeographicalCode.forCountry(country).stream()
                .filter(EntsoeGeographicalCode -> EntsoeGeographicalCode != EntsoeGeographicalCode.D1)
                .collect(Collectors.toList());
    }

    private <R> R scanRepository(DateTime date, CaseType type, Country country, Function<List<ImportContext>, R> handler) {
        Collection<EntsoeGeographicalCode> geographicalCodes = country != null ? forCountryHacked(country)
                                                                             : Collections.singleton(EntsoeGeographicalCode.UX);
        for (EntsoeFormat format : formats) {
            Path formatDir = config.getRootDir().resolve(format.getDirName());
            if (Files.exists(formatDir)) {
                Path typeDir = formatDir.resolve(type.name());
                if (Files.exists(typeDir)) {
                    Path dayDir = typeDir.resolve(String.format("%04d", date.getYear()))
                            .resolve(String.format("%02d", date.getMonthOfYear()))
                            .resolve(String.format("%02d", date.getDayOfMonth()));
                    if (Files.exists(dayDir)) {
                        List<ImportContext> importContexts = null;
                        for (EntsoeGeographicalCode geographicalCode : geographicalCodes) {
                            Collection<String> forbiddenFormats = config.getForbiddenFormatsByGeographicalCode().get(geographicalCode);
                            if (!forbiddenFormats.contains(format.getImporter().getFormat())) {
                                for (int i = 9; i >= 0; i--) {
                                    String baseName = String.format("%04d%02d%02d_%02d%02d_" + type + "%01d_" + geographicalCode.name() + "%01d",
                                            date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), date.getMinuteOfHour(),
                                            date.getDayOfWeek(), i);
                                    ReadOnlyDataSource ds = dataSourceFactory.create(dayDir, baseName);
                                    if (importContexts == null) {
                                        importContexts = new ArrayList<>();
                                    }
                                    if (format.getImporter().exists(ds)) {
                                        importContexts.add(new ImportContext(format.getImporter(), ds));
                                    }
                                }
                            }
                        }
                        if (importContexts != null) {
                            R result = handler.apply(importContexts);
                            if (result != null) {
                                return result;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static DateTime toCetDate(DateTime date) {
        DateTimeZone cet = DateTimeZone.forID("CET");
        if (!date.getZone().equals(cet)) {
            return date.toDateTime(cet);
        }
        return date;
    }

    @Override
    public List<Network> load(DateTime date, CaseType type, Country country) {
        Objects.requireNonNull(date);
        Objects.requireNonNull(type);
        List<Network> networks2 = scanRepository(toCetDate(date), type, country, importContexts -> {
            List<Network> networks = null;
            if (importContexts.size() > 0) {
                networks = new ArrayList<>();
                for (ImportContext importContext : importContexts) {
                    LOGGER.info("Loading {} in {} format", importContext.ds.getBaseName(), importContext.importer.getFormat());
                    networks.add(importContext.importer.importData(importContext.ds, null));
                }
            }
            return networks;
        });
        return networks2 == null ? Collections.emptyList() : networks2;
    }

    @Override
    public boolean isDataAvailable(DateTime date, CaseType type, Country country) {
        return isNetworkDataAvailable(date, type, country);
    }

    private boolean isNetworkDataAvailable(DateTime date, CaseType type, Country country) {
        Objects.requireNonNull(date);
        Objects.requireNonNull(type);
        return scanRepository(toCetDate(date), type, country, importContexts -> {
            if (importContexts.size() > 0) {
                for (ImportContext importContext : importContexts) {
                    if (importContext.importer.exists(importContext.ds)) {
                        return true;
                    }
                }
                return null;
            }
            return null;
        }) != null;
    }

    private void browse(Path dir, Consumer<Path> handler) {
        try (Stream<Path> stream = Files.list(dir)) {
            stream.sorted().forEach(child -> {
                if (Files.isDirectory(child)) {
                    browse(child, handler);
                } else {
                    try {
                        if (Files.size(child) > 0) {
                            handler.accept(child);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Set<DateTime> dataAvailable(CaseType type, Set<Country> countries, Interval interval) {
        Set<EntsoeGeographicalCode> geographicalCodes = new HashSet<>();
        if (countries == null) {
            geographicalCodes.add(EntsoeGeographicalCode.UX);
        } else {
            for (Country country : countries) {
                geographicalCodes.addAll(forCountryHacked(country));
            }
        }
        Multimap<DateTime, EntsoeGeographicalCode> dates = HashMultimap.create();
        for (EntsoeFormat format : formats) {
            Path formatDir = config.getRootDir().resolve(format.getDirName());
            if (Files.exists(formatDir)) {
                Path typeDir = formatDir.resolve(type.name());
                if (Files.exists(typeDir)) {
                    browse(typeDir, path -> {
                        EntsoeFileName entsoeFileName = EntsoeFileName.parse(path.getFileName().toString());
                        EntsoeGeographicalCode geographicalCode = entsoeFileName.getGeographicalCode();
                        if (geographicalCode != null
                                && !config.getForbiddenFormatsByGeographicalCode().get(geographicalCode).contains(format.getImporter().getFormat())
                                && interval.contains(entsoeFileName.getDate())) {
                            dates.put(entsoeFileName.getDate(), geographicalCode);
                        }
                    });
                }
            }
        }
        return dates.asMap().entrySet().stream()
                .filter(e -> new HashSet<>(e.getValue()).containsAll(geographicalCodes))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
