/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.*;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A power network model.
 *
 * <p>
 *  Characteristics
 * </p>
 *
 * <table style="border: 1px solid black; border-collapse: collapse">
 *     <thead>
 *         <tr>
 *             <th style="border: 1px solid black">Attribute</th>
 *             <th style="border: 1px solid black">Type</th>
 *             <th style="border: 1px solid black">Unit</th>
 *             <th style="border: 1px solid black">Required</th>
 *             <th style="border: 1px solid black">Defaut value</th>
 *             <th style="border: 1px solid black">Description</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td style="border: 1px solid black">Id</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Unique identifier of the network</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the network</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">CaseDate</td>
 *             <td style="border: 1px solid black">DateTime</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> Now </td>
 *             <td style="border: 1px solid black">The date of the case</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">ForecastDistance</td>
 *             <td style="border: 1px solid black">integer</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> 0 </td>
 *             <td style="border: 1px solid black">The number of minutes between the date of the case generation and the date of the case</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * To create a new empty network with default implementation:
 *<pre>
 *    Network n = Network.create("test", "test");
 *</pre>
 *
 * <p>The network is initially created with one variant identified by
 * <code>VariantManagerConstants.INITIAL_VARIANT_ID</code>. {@link VariantManager} is
 * responsible for variant management and is accessible from the network thanks
 * to {@link #getVariantManager()}.
 *
 * <p>Instances of <code>Network</code> are not thread safe except for attributes
 * depending of the variant (always specified in the javadoc) if
 * {@link VariantManager#allowVariantMultiThreadAccess(boolean)} is set to true.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @see NetworkFactory
 * @see VariantManager
 */
public interface Network extends Container<Network> {

    default Collection<Network> getSubnetworks() {
        return Collections.emptyList();
    }

    default Network getSubnetwork(String id) {
        return null;
    }

    /**
     * Read a network from the specified file, trying to guess its format.
     *
     * @param file               The file to be loaded.
     * @param computationManager A computation manager which may be used by import post-processors
     * @param config             The import config, in particular definition of post processors
     * @param parameters         Import-specific parameters
     * @param networkFactory     Network factory
     * @param loader             Provides the list of available importers and post-processors
     * @param reportNode           The reportNode used for functional logs
     * @return                   The loaded network
     */
    static Network read(Path file, ComputationManager computationManager, ImportConfig config, Properties parameters, NetworkFactory networkFactory,
                        ImportersLoader loader, ReportNode reportNode) {
        ReadOnlyDataSource dataSource = DataSource.fromPath(file);
        Importer importer = Importer.find(dataSource, loader, computationManager, config);
        if (importer != null) {
            return importer.importData(dataSource, networkFactory, parameters, reportNode);
        }
        throw new PowsyblException(Importers.UNSUPPORTED_FILE_FORMAT_OR_INVALID_FILE);
    }

    static Network read(Path file, ComputationManager computationManager, ImportConfig config, Properties parameters,
                        ImportersLoader loader, ReportNode reportNode) {
        return read(file, computationManager, config, parameters, NetworkFactory.findDefault(), loader, reportNode);
    }

    /**
     * Read a network from the specified file, trying to guess its format.
     *
     * @param file               The file to be loaded.
     * @param computationManager A computation manager which may be used by import post-processors
     * @param config             The import config, in particular definition of post processors
     * @param parameters         Import-specific parameters
     * @param loader             Provides the list of available importers and post-processors
     * @return                   The loaded network
     */
    static Network read(Path file, ComputationManager computationManager, ImportConfig config, Properties parameters, ImportersLoader loader) {
        return read(file, computationManager, config, parameters, loader, ReportNode.NO_OP);
    }

    /**
     * Read a network from the specified file, trying to guess its format,
     * and using importers and post processors defined as services.
     *
     * @param file               The file to be loaded.
     * @param computationManager A computation manager which may be used by import post-processors
     * @param config             The import config, in particular definition of post processors
     * @param parameters         Import-specific parameters
     * @return                   The loaded network
     */
    static Network read(Path file, ComputationManager computationManager, ImportConfig config, Properties parameters) {
        return read(file, computationManager, config, parameters, new ImportersServiceLoader());
    }

    /**
     * Read a network from the specified file, trying to guess its format,
     * and using importers and post processors defined as services.
     * Import will be performed using import configuration defined in default platform config,
     * and with no importer-specific parameters.
     * Post processors will use the default {@link LocalComputationManager}, as defined in
     * default platform config.
     *
     * @param file               The file to be loaded.
     * @return                   The loaded network
     */
    static Network read(Path file) {
        return read(file, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), null);
    }

    /**
     * Loads a network from the specified file path, see {@link #read(Path)}.
     *
     * @param file               The file to be loaded.
     * @return                   The loaded network
     */
    static Network read(String file) {
        return read(Paths.get(file));
    }

    /**
     * Read a network from a raw input stream, trying to guess the format from the specified filename.
     * Please note that the input stream must be from a simple file, not a zipped one.
     *
     * @param filename           The name of the file to be imported.
     * @param data               The raw data from which the network should be loaded
     * @param computationManager A computation manager which may be used by import post-processors
     * @param config             The import config, in particular definition of post processors
     * @param parameters         Import-specific parameters
     * @param networkFactory     Network factory
     * @param loader             Provides the list of available importers and post-processors
     * @param reportNode           The reportNode used for functional logs
     * @return                   The loaded network
     */
    static Network read(String filename, InputStream data, ComputationManager computationManager, ImportConfig config, Properties parameters, NetworkFactory networkFactory, ImportersLoader loader, ReportNode reportNode) {
        ReadOnlyMemDataSource dataSource = new ReadOnlyMemDataSource(DataSourceUtil.getBaseName(filename));
        dataSource.putData(filename, data);
        Importer importer = Importer.find(dataSource, loader, computationManager, config);
        if (importer != null) {
            return importer.importData(dataSource, networkFactory, parameters, reportNode);
        }
        throw new PowsyblException(Importers.UNSUPPORTED_FILE_FORMAT_OR_INVALID_FILE);
    }

    /**
     * Read a network from a raw input stream, trying to guess the format from the specified filename.
     * Please note that the input stream must be from a simple file, not a zipped one.
     *
     * @param filename           The name of the file to be imported.
     * @param data               The raw data from which the network should be loaded
     * @param computationManager A computation manager which may be used by import post-processors
     * @param config             The import config, in particular definition of post processors
     * @param parameters         Import-specific parameters
     * @param loader             Provides the list of available importers and post-processors
     * @param reportNode           The reportNode used for functional logs
     * @return                   The loaded network
     */
    static Network read(String filename, InputStream data, ComputationManager computationManager, ImportConfig config, Properties parameters, ImportersLoader loader, ReportNode reportNode) {
        return read(filename, data, computationManager, config, parameters, NetworkFactory.findDefault(), loader, reportNode);
    }

    /**
     * Read a network from a raw input stream, trying to guess the format from the specified filename.
     * Please note that the input stream must be from a simple file, not a zipped one.
     *
     * @param filename           The name of the file to be imported.
     * @param data               The raw data from which the network should be loaded
     * @param computationManager A computation manager which may be used by import post-processors
     * @param config             The import config, in particular definition of post processors
     * @param parameters         Import-specific parameters
     * @param loader             Provides the list of available importers and post-processors
     * @return                   The loaded network
     */
    static Network read(String filename, InputStream data, ComputationManager computationManager, ImportConfig config, Properties parameters, ImportersLoader loader) {
        return read(filename, data, computationManager, config, parameters, loader, ReportNode.NO_OP);
    }

    /**
     * Read a network from a raw input stream, trying to guess the format from the specified filename,
     * and using importers and post processors defined as services.
     * Please note that the input stream must be from a simple file, not a zipped one.
     *
     * @param filename           The name of the file to be imported.
     * @param data               The raw data from which the network should be loaded
     * @param computationManager A computation manager which may be used by import post-processors
     * @param config             The import config, in particular definition of post processors
     * @param parameters         Import-specific parameters
     * @return                   The loaded network
     */
    static Network read(String filename, InputStream data, ComputationManager computationManager, ImportConfig config, Properties parameters) {
        return read(filename, data, computationManager, config, parameters, new ImportersServiceLoader());
    }

    /**
     * Read a network from a raw input stream, trying to guess the format from the specified filename,
     * and using importers and post processors defined as services.
     * Import will be performed using import configuration defined in default platform config,
     * and with no importer-specific parameters.
     * Please note that the input stream must be from a simple file, not a zipped one.
     *
     * @param filename           The name of the file to be imported.
     * @param data               The raw data from which the network should be loaded
     * @param computationManager A computation manager which may be used by import post-processors
     * @return                   The loaded network
     */
    static Network read(String filename, InputStream data, ComputationManager computationManager) {
        return read(filename, data, computationManager, ImportConfig.CACHE.get(), null);
    }

    /**
     * Read a network from a raw input stream, trying to guess the format from the specified filename,
     * and using importers and post processors defined as services.
     * Import will be performed using import configuration defined in default platform config,
     * and with no importer-specific parameters.
     * Post processors will use the default {@link LocalComputationManager}, as defined in
     * default platform config.
     * Please note that the input stream must be from a simple file, not a zipped one.
     *
     * @param filename           The name of the file to be imported.
     * @param data               The raw data from which the network should be loaded
     * @return                   The loaded network
     */
    static Network read(String filename, InputStream data) {
        return read(filename, data, LocalComputationManager.getDefault());
    }

    /**
     * Read a network from a raw input stream, trying to guess the format from the specified filename,
     * and using importers and post processors defined as services.
     * Import will be performed using import configuration defined in default platform config,
     * and with no importer-specific parameters.
     * Post processors will use the default {@link LocalComputationManager}, as defined in
     * default platform config.
     * Please note that the input stream must be from a simple file, not a zipped one.
     *
     * @param filename           The name of the file to be imported.
     * @param data               The raw data from which the network should be loaded
     * @param reportNode           The reportNode used for functional logs
     * @return                   The loaded network
     */
    static Network read(String filename, InputStream data, ReportNode reportNode) {
        return read(filename, data, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), null, new ImportersServiceLoader(), reportNode);
    }

    static Network read(ReadOnlyDataSource dataSource) {
        return read(dataSource, null);
    }

    static Network read(ReadOnlyDataSource dataSource, Properties properties) {
        return read(dataSource, properties, ReportNode.NO_OP);
    }

    static Network read(ReadOnlyDataSource dataSource, Properties properties, ReportNode reportNode) {
        Importer importer = Importer.find(dataSource);
        if (importer != null) {
            return importer.importData(dataSource, NetworkFactory.findDefault(), properties, reportNode);
        }
        throw new PowsyblException(Importers.UNSUPPORTED_FILE_FORMAT_OR_INVALID_FILE);
    }

    static Network read(ReadOnlyDataSource... dataSources) {
        return read(List.of(dataSources));
    }

    static Network read(Path... files) {
        List<ReadOnlyDataSource> dataSources = Arrays.stream(Objects.requireNonNull(files)).map(DataSource::fromPath).collect(Collectors.toList());
        return read(dataSources);
    }

    static Network read(List<ReadOnlyDataSource> dataSources) {
        return read(dataSources, null);
    }

    static Network read(List<ReadOnlyDataSource> dataSources, Properties properties) {
        return read(dataSources, properties, ReportNode.NO_OP);
    }

    static Network read(List<ReadOnlyDataSource> dataSources, Properties properties, ReportNode reportNode) {
        Objects.requireNonNull(dataSources);
        return read(new MultipleReadOnlyDataSource(dataSources), properties, reportNode);
    }

    static void readAll(Path dir, boolean parallel, ImportersLoader loader, ComputationManager computationManager, ImportConfig config, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener, NetworkFactory networkFactory, ReportNode reportNode) throws IOException, InterruptedException, ExecutionException {
        if (!Files.isDirectory(dir)) {
            throw new PowsyblException("Directory " + dir + " does not exist or is not a regular directory");
        }
        for (Importer importer : Importer.list(loader, computationManager, config)) {
            Importers.importAll(dir, importer, parallel, parameters, consumer, listener, networkFactory, reportNode);
        }
    }

    static void readAll(Path dir, boolean parallel, ImportersLoader loader, ComputationManager computationManager, ImportConfig config, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener, ReportNode reportNode) throws IOException, InterruptedException, ExecutionException {
        readAll(dir, parallel, loader, computationManager, config, parameters, consumer, listener, NetworkFactory.findDefault(), reportNode);
    }

    static void readAll(Path dir, boolean parallel, ImportersLoader loader, ComputationManager computationManager, ImportConfig config, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        readAll(dir, parallel, loader, computationManager, config, parameters, consumer, listener, ReportNode.NO_OP);
    }

    static void readAll(Path dir, boolean parallel, ImportersLoader loader, ComputationManager computationManager, ImportConfig config, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        readAll(dir, parallel, loader, computationManager, config, null, consumer, listener);
    }

    static void readAll(Path dir, boolean parallel, ComputationManager computationManager, ImportConfig config, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        readAll(dir, parallel, new ImportersServiceLoader(), computationManager, config, parameters, consumer, listener);
    }

    static void readAll(Path dir, boolean parallel, ComputationManager computationManager, ImportConfig config, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        readAll(dir, parallel, new ImportersServiceLoader(), computationManager, config, consumer, listener);
    }

    static void readAll(Path dir, boolean parallel, ComputationManager computationManager, ImportConfig config, Consumer<Network> consumer) throws IOException, InterruptedException, ExecutionException {
        readAll(dir, parallel, computationManager, config, consumer, null);
    }

    static void readAll(Path dir, boolean parallel, Consumer<Network> consumer) throws IOException, InterruptedException, ExecutionException {
        readAll(dir, parallel, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), consumer);
    }

    static void readAll(Path dir, boolean parallel, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        readAll(dir, parallel, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), consumer, listener);
    }

    static void readAll(Path dir, Consumer<Network> consumer) throws IOException, InterruptedException, ExecutionException {
        readAll(dir, false, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), consumer);
    }

    /**
     * A global bus/breaker view of the network.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    interface BusBreakerView {

        /**
         * Get all buses.
         * <p>
         * Depends on the working variant.
         * @see VariantManager
         */
        Iterable<Bus> getBuses();

        /**
         * Get all buses.
         * <p>
         * Depends on the working variant.
         * @see VariantManager
         */
        Stream<Bus> getBusStream();

        /**
         * Get the bus count.
         * <p>
         * Depends on the working variant.
         * @see VariantManager
         */
        int getBusCount();

        /**
         * Get all switches
         */
        Iterable<Switch> getSwitches();

        /**
         * Get all switches.
         */
        Stream<Switch> getSwitchStream();

        /**
         * Get the switch count.
         */
        int getSwitchCount();

        /**
         * Get a Bus.
         */
        default Bus getBus(String id) {
            throw new PowsyblException("Method should be overridden in the current implementation");
        }
    }

    /**
     * A global bus view of the network.
     */
    interface BusView {

        /**
         * Get all buses.
         * <p>
         * Depends on the working variant.
         * @see VariantManager
         */
        Iterable<Bus> getBuses();

        /**
         * Get all buses.
         * <p>
         * Depends on the working variant.
         * @see VariantManager
         */
        Stream<Bus> getBusStream();

        /**
         * Get a Bus.
         */
        default Bus getBus(String id) {
            throw new PowsyblException("Method should be overridden in the current implementation");
        }

        /**
         * Get all connected components.
         * <p>
         * Depends on the working variant.
         * @see VariantManager
         */
        Collection<Component> getConnectedComponents();

        /**
         * Get all synchronous components.
         * <p>
         * Depends on the working variant.
         * @see VariantManager
         */
        Collection<Component> getSynchronousComponents();
    }

    /**
     * Create an empty network using default implementation.
     *
     * @param id id of the network
     * @param sourceFormat source  format
     * @return an empty network
     */
    static Network create(String id, String sourceFormat) {
        return NetworkFactory.findDefault().createNetwork(id, sourceFormat);
    }

    /**
     * Create a network (using default implementation) as the result of the merge of the given networks. Each given
     * network is represented as a subnetwork in the resulting network. As a result of that merge, the given networks
     * are empty at the end of the call.
     * Note that, as no id is given, the id of the network created is generated.
     *
     * @return the merged network with subnetworks inside.
     */
    static Network merge(Network... networks) {
        return NetworkFactory.findDefault().merge(networks);
    }

    /**
     * Create a network (using default implementation) as the result of the merge of the given networks. Each given
     * network is represented as a subnetwork in the resulting network. As a result of that merge, the given networks
     * are empty at the end of the call.
     *
     * @param id id of the network to create
     * @return the merged network with subnetworks inside.
     */
    static Network merge(String id, Network... networks) {
        return NetworkFactory.findDefault().merge(id, networks);
    }

    /**
     * Just being able to name method create et not createNetwork. Create is not available in {@link NetworkFactory} for backward
     * compatibility reason. To cleanup when {@link NetworkFactory#create(String, String)} will be removed.
     */
    interface PrettyNetworkFactory {

        Network create(String id, String sourceFormat);
    }

    /**
     * Get network factory named {@code name}.
     *
     * @param name name of the {@link NetworkFactory}
     * @return network factory with  the given name
     */
    static PrettyNetworkFactory with(String name) {
        return (id, sourceFormat) -> NetworkFactory.find(name).createNetwork(id, sourceFormat);
    }

    /**
     * Get the date that the network represents.
     */
    ZonedDateTime getCaseDate();

    /**
     * Set the date that the network represents.
     * @throws IllegalArgumentException if date is null.
     */
    Network setCaseDate(ZonedDateTime date);

    /**
     * Get the forecast distance in minutes.
     * <p>Example: 0 for a snapshot, 6*60 to 30*60 for a DACF.
     */
    int getForecastDistance();

    Network setForecastDistance(int forecastDistance);

    /**
     * Get the source format.
     * @return the source format
     */
    String getSourceFormat();

    /**
     * Get the variant manager of the network.
     */
    VariantManager getVariantManager();

    /**
     * <p>Allows {@link ReportNodeContext} to be accessed simultaneously by different threads.</p>
     * <p>When this option is activated, the reportNode context can have a different content
     * for each thread.</p>
     * <p>Note that to avoid memory leaks when in multi-thread configuration: </p>
     * <ul>
     *     <li>each reportNode pushed in the ReportNodeContext should be popped in a "finally" section:
     * <pre>
     * {@code
     *     network.getReportNodeContext().pushReportNode(reportNode);
     *     try {
     *         // code that can throw an exception
     *     } finally {
     *         network.getReportNodeContext().popReportNode();
     *     }
     * }
     * </pre>
     * </li>
     * <li>the context should be set in mono-thread access when multi-threading policy is no more useful.</li>
     * </ul>
     * @param allow allow multi-thread access to the ReportNodeContext
     */
    void allowReportNodeContextMultiThreadAccess(boolean allow);

    /**
     * Get the {@link ReportNodeContext} of the network.
     */
    ReportNodeContext getReportNodeContext();

    /**
     * Get all countries.
     */
    Set<Country> getCountries();

    /**
     * Get the country count.
     */
    int getCountryCount();

    AreaTypeAdder newAreaType();

    Iterable<AreaType> getAreaTypes();

    Stream<AreaType> getAreaTypeStream();

    AreaType getAreaType(String id);

    AreaAdder newArea();

    /**
     * @return all existing areas, which may include several areas for each AreaType
     */
    Iterable<Area> getAreas();

    /**
     * @return all existing areas, which may include several areas for each AreaType
     */
    Stream<Area> getAreaStream();

    Area getArea(String id);

    /**
     * Get a builder to create a new substation.
     * @return a builder to create a new substation
     */
    SubstationAdder newSubstation();

    /**
     * Get all substations.
     */
    Iterable<Substation> getSubstations();

    /**
     * Get all substations.
     */
    Stream<Substation> getSubstationStream();

    /**
     * Get the substation count.
     */
    int getSubstationCount();

    /**
     * Get substation located in a specific county, TSO and marked with a list
     * of geographical tag.
     *
     * @param country the country, if <code>null</code> there is no
     *                  filtering on countries
     * @param tsoId the id of the TSO, if <code>null</code> there is no
     *                  filtering on TSOs
     * @param geographicalTags a list a geographical tags
     */
    Iterable<Substation> getSubstations(Country country, String tsoId, String... geographicalTags);

    /**
     * Get substation located in a specific county, TSO and marked with a list
     * of geographical tag.
     *
     * @param country the country name, if empty string, the filtering will be on
     *                  substations without country, if <code>null</code> there is no
     *                  filtering on countries
     * @param tsoId the id of the TSO, if <code>null</code> there is no
     *                  filtering on TSOs
     * @param geographicalTags a list a geographical tags
     */
    Iterable<Substation> getSubstations(String country, String tsoId, String... geographicalTags);

    /**
     * Get a substation.
     *
     * @param id the id or an alias of the substation
     */
    Substation getSubstation(String id);

    /**
     * Get a builder to create a new voltage level (without substation).
     * Note: if this method is not implemented, it will create an intermediary fictitious {@link Substation}.
     * @return a builder to create a new voltage level
     */
    default VoltageLevelAdder newVoltageLevel() {
        return newSubstation()
                .setId("FICTITIOUS_SUBSTATION")
                .setEnsureIdUnicity(true)
                .setFictitious(true)
                .add()
                .newVoltageLevel();
    }

    /**
     * Get all substation voltage levels.
     */
    Iterable<VoltageLevel> getVoltageLevels();

    /**
     * Get all substation voltage levels.
     */
    Stream<VoltageLevel> getVoltageLevelStream();

    /**
     * Get the voltage level count.
     */
    int getVoltageLevelCount();

    /**
     * Get a substation voltage level.
     *
     * @param id the id or an alias of the substation voltage level
     */
    VoltageLevel getVoltageLevel(String id);

    /**
     * Get a builder to create a new AC line.
     * @return a builder to create a new line
     */
    LineAdder newLine();

    /**
     * Get all AC lines.
     */
    Iterable<Line> getLines();

    /**
     * Get all tie lines.
     */
    Iterable<TieLine> getTieLines();

    /**
     * Get a branch
     * @param branchId the id of the branch
     */
    Branch getBranch(String branchId);

    /**
     * Get all branches
     */
    Iterable<Branch> getBranches();

    /**
     * Get all branches
     */
    Stream<Branch> getBranchStream();

    /**
     * Get the branch count.
     */
    int getBranchCount();

    /**
     * Get all AC lines.
     */
    Stream<Line> getLineStream();

    /**
     * Get all tie lines.
     */
    Stream<TieLine> getTieLineStream();

    /**
     * Get the AC line count.
     */
    int getLineCount();

    /**
     * Get the tie line count.
     */
    int getTieLineCount();

    /**
     * Get a AC line.
     *
     * @param id the id or an alias of the AC line
     */
    Line getLine(String id);

    /**
     * Get a tie line.
     *
     * @param id the id or an alias of the AC line
     */
    TieLine getTieLine(String id);

    /**
     * Get a builder to create a new AC tie line.
     * @return a builder to create a new AC tie line
     */
    TieLineAdder newTieLine();

    /**
     * Get all two windings transformers.
     */
    Iterable<TwoWindingsTransformer> getTwoWindingsTransformers();

    /**
     * Get all two windings transformers.
     */
    Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream();

    /**
     * Get the two windings transformer count.
     */
    int getTwoWindingsTransformerCount();

    /**
     * Get a two windings transformer.
     *
     * @param id the id or an alias of the two windings transformer
     */
    TwoWindingsTransformer getTwoWindingsTransformer(String id);

    /**
     * Get all 3 windings transformers.
     */
    Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers();

    /**
     * Get all 3 windings transformers.
     */
    Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream();

    /**
     * Get the 3 windings transformer count.
     */
    int getThreeWindingsTransformerCount();

    /**
     * Get a 3 windings transformer.
     *
     * @param id the id or an alias of the 3 windings transformer
     */
    ThreeWindingsTransformer getThreeWindingsTransformer(String id);

    /**
     * Get all overload management systems.
     */
    Iterable<OverloadManagementSystem> getOverloadManagementSystems();

    /**
     * Get all overload management systems.
     */
    Stream<OverloadManagementSystem> getOverloadManagementSystemStream();

    /**
     * Get the overload management system count.
     */
    int getOverloadManagementSystemCount();

    /**
     * Get an overload management system.
     *
     * @param id the id or an alias of the overload management system
     */
    OverloadManagementSystem getOverloadManagementSystem(String id);

    /**
     * Get all generators.
     */
    Iterable<Generator> getGenerators();

    /**
     * Get all generators.
     */
    Stream<Generator> getGeneratorStream();

    /**
     * Get the generator count.
     */
    int getGeneratorCount();

    /**
     * Get a generator.
     *
     * @param id the id or an alias of the generator
     */
    Generator getGenerator(String id);

    /**
     * Get all batteries.
     */
    Iterable<Battery> getBatteries();

    /**
     * Get all batteries.
     */
    Stream<Battery> getBatteryStream();

    /**
     * Get the battery count.
     */
    int getBatteryCount();

    /**
     * Get a battery.
     *
     * @param id the id or an alias of the battery
     */
    Battery getBattery(String id);

    /**
     * Get all loads.
     */
    Iterable<Load> getLoads();

    /**
     * Get all loads.
     */
    Stream<Load> getLoadStream();

    /**
     * Get the load count.
     */
    int getLoadCount();

    /**
     * Get a load.
     *
     * @param id the id or an alias of the load
     */
    Load getLoad(String id);

    /**
     * Get all compensator shunts.
     */
    Iterable<ShuntCompensator> getShuntCompensators();

    /**
     * Get all compensator shunts.
     */
    Stream<ShuntCompensator> getShuntCompensatorStream();

    /**
     * Get the shunt count.
     */
    int getShuntCompensatorCount();

    /**
     * Get a compensator shunt.
     *
     * @param id the id or an alias of the compensator shunt
     */
    ShuntCompensator getShuntCompensator(String id);

    /**
     * Get all dangling lines corresponding to given filter.
     */
    Iterable<DanglingLine> getDanglingLines(DanglingLineFilter danglingLineFilter);

    /**
     * Get all dangling lines.
     */
    default Iterable<DanglingLine> getDanglingLines() {
        return getDanglingLines(DanglingLineFilter.ALL);
    }

    /**
     * Get the dangling lines corresponding to given filter.
     */
    Stream<DanglingLine> getDanglingLineStream(DanglingLineFilter danglingLineFilter);

    /**
     * Get all the dangling lines.
     */
    default Stream<DanglingLine> getDanglingLineStream() {
        return getDanglingLineStream(DanglingLineFilter.ALL);
    }

    /**
     * Get the dangling line count.
     */
    int getDanglingLineCount();

    /**
     * Get a dangling line.
     *
     * @param id the id or an alias of the dangling line
     */
    DanglingLine getDanglingLine(String id);

    /**
     * Get all static var compensators.
     */
    Iterable<StaticVarCompensator> getStaticVarCompensators();

    /**
     * Get all static var compensators.
     */
    Stream<StaticVarCompensator> getStaticVarCompensatorStream();

    /**
     * Get the static var compensator count.
     */
    int getStaticVarCompensatorCount();

    /**
     * Get a static var compensator.
     *
     * @param id the id or an alias of the static var compensator
     */
    StaticVarCompensator getStaticVarCompensator(String id);

    /**
     * Get a switch from its id or an alias.
     * @param id id or an alias of the switch
     * @return the switch
     */
    Switch getSwitch(String id);

    /**
     * Get all switches.
     * @return all switches
     */
    Iterable<Switch> getSwitches();

    /**
     * Get all switches.
     * @return all switches
     */
    Stream<Switch> getSwitchStream();

    /**
     * Get the switch count.
     *
     * @return the switch count
     */
    int getSwitchCount();

    /**
     * Get a busbar section from its id or an alias.
     * @param id the id or an alias of the busbar section
     * @return the busbar section
     */
    BusbarSection getBusbarSection(String id);

    /**
     * Get all busbar sections.
     * @return all busbar sections
     */
    Iterable<BusbarSection> getBusbarSections();

    /**
     * Get all busbar sections.
     * @return all busbar sections
     */
    Stream<BusbarSection> getBusbarSectionStream();

    /**
     * Get the busbar section count.
     * @return the busbar section count.
     */
    int getBusbarSectionCount();

    /**
     * Get all HVDC converter stations.
     * @return all HVDC converter stations
     */
    Iterable<HvdcConverterStation<?>> getHvdcConverterStations();

    /**
     * Get all HVDC converter stations.
     * @return all HVDC converter stations
     */
    Stream<HvdcConverterStation<?>> getHvdcConverterStationStream();

    /**
     * Get HVDC converter stations count.
     * @return HVDC converter station count
     */
    int getHvdcConverterStationCount();

    /**
     * Get an HVDC converter station.
     * @param id the id or an alias of the HVDC converter station
     * @return the HVDC converter station or null if not found
     */
    HvdcConverterStation<?> getHvdcConverterStation(String id);

    /**
     * Get all LCC converter stations.
     * @return all LCC converter stations
     */
    Iterable<LccConverterStation> getLccConverterStations();

    /**
     * Get all LCC converter stations.
     * @return all LCC converter stations
     */
    Stream<LccConverterStation> getLccConverterStationStream();

    /**
     * Get LCC converter stations count.
     * @return LCC converter station count
     */
    int getLccConverterStationCount();

    /**
     * Get an LCC converter station.
     * @param id the id or an alias of the LCC converter station
     * @return the LCC converter station or null if not found
     */
    LccConverterStation getLccConverterStation(String id);

    /**
     * Get all VSC converter stations.
     * @return all VSC converter stations
     */
    Iterable<VscConverterStation> getVscConverterStations();

    /**
     * Get all VSC converter stations.
     * @return all VSC converter stations
     */
    Stream<VscConverterStation> getVscConverterStationStream();

    /**
     * Get VSC converter stations count.
     * @return VSC converter station count
     */
    int getVscConverterStationCount();

    /**
     * Get an VSC converter station.
     * @param id the id or an alias of the VSC converter station
     * @return the VSC converter station or null if not found
     */
    VscConverterStation getVscConverterStation(String id);

    /**
     * Get all HVDC lines.
     * @return all HVDC lines
     */
    Iterable<HvdcLine> getHvdcLines();

    /**
     * Get all HVDC lines.
     * @return all HVDC lines
     */
    Stream<HvdcLine> getHvdcLineStream();

    /**
     * Get HVDC lines count.
     * @return HVDC lines count
     */
    int getHvdcLineCount();

    /**
     * Get an HVDC line.
     * @param id the id or an alias of the HVDC line
     * @return the HVDC line or null if not found
     */
    HvdcLine getHvdcLine(String id);

    /**
     * Get an HVDC line from a converter station
     * @param converterStation a HVDC converter station
     * @return the HVDC line or null if not found
     */
    default HvdcLine getHvdcLine(HvdcConverterStation converterStation) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Get a builder to create a new HVDC line.
     * @return a builder to create a new HVDC line
     */
    HvdcLineAdder newHvdcLine();

    /**
     * Get all grounds.
     */
    Iterable<Ground> getGrounds();

    /**
     * Get all grounds.
     */
    Stream<Ground> getGroundStream();

    /**
     * Get the ground count.
     */
    int getGroundCount();

    /**
     * Get a ground.
     *
     * @param id the id or an alias of the ground
     */
    Ground getGround(String id);

    /**
     * * Get an identifiable by its ID or alias
     *
     * @param id the id or an alias of the identifiable
     */
    Identifiable<?> getIdentifiable(String id);

    /**
     * Get all identifiables of the network.
     *
     * @return all identifiables of the network
     */
    Collection<Identifiable<?>> getIdentifiables();

    /**
     * Get all connectables of the network for a given type
     *
     * @param clazz connectable type class
     * @return all the connectables of the given type
     */
    default <C extends Connectable> Iterable<C> getConnectables(Class<C> clazz) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a stream of all connectables of the network for a given type
     *
     * @param clazz connectable type class
     * @return a stream of all the connectables of the given type
     */
    default <C extends Connectable> Stream<C> getConnectableStream(Class<C> clazz) {
        throw new UnsupportedOperationException();
    }

    /**
     * Count the connectables of the network for a given type
     *
     * @param clazz connectable type class
     * @return the count of all the connectables of the given type
     */
    default <C extends Connectable> int getConnectableCount(Class<C> clazz) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get all connectables of the network
     *
     * @return all the connectables
     */
    default Iterable<Connectable> getConnectables() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a stream of all connectables of the network
     *
     * @return a stream of all the connectables
     */
    default Stream<Connectable> getConnectableStream() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a connectable by its ID or alias
     *
     * @param id the id or an alias of the equipment
     */
    default Connectable<?> getConnectable(String id) {
        Identifiable<?> identifiable = getIdentifiable(id);
        if (identifiable instanceof Connectable<?>) {
            return (Connectable<?>) identifiable;
        }
        return null;
    }

    /**
     * Count the connectables of the network
     *
     * @return the count of all the connectables
     */
    default int getConnectableCount() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a bus/breaker view of the network.
     */
    BusBreakerView getBusBreakerView();

    /**
     * Get a bus view of the network.
     */
    BusView getBusView();

    /**
     * Get a builder to create a new VoltageAngleLimit.
     */
    VoltageAngleLimitAdder newVoltageAngleLimit();

    /**
     * Get all voltageAngleLimits.
     */
    Iterable<VoltageAngleLimit> getVoltageAngleLimits();

    /**
     * Get all voltageAngleLimits.
     */
    Stream<VoltageAngleLimit> getVoltageAngleLimitsStream();

    /**
     * Get voltage angle limit with id
     */
    VoltageAngleLimit getVoltageAngleLimit(String id);

    /**
     * Create an empty subnetwork in the current network.
     *
     * @param subnetworkId id of the subnetwork
     * @param name subnetwork's name
     * @param sourceFormat source format
     * @return the created subnetwork
     */
    Network createSubnetwork(String subnetworkId, String name, String sourceFormat);

    /**
     * <p>Detach the current network (including its subnetworks) from its parent network.</p>
     * <p>Note that this operation is destructive: after it the current network's content
     * couldn't be accessed from the parent network anymore.</p>
     * <p>The boundary elements, i.e. linking this network to an external voltage level are split if possible.</br>
     * A {@link PowsyblException} is thrown if some un-splittable boundary elements are detected. This detection is processed
     * before any network modification. So if an un-splittable boundary element is detected, no destructive operation will be done.</p>
     *
     * @return a fully-independent network corresponding to the current network and its subnetworks.
     */
    Network detach();

    /**
     * <p>Check if the current network can be detached from its parent network (with {@link #detach()}).</p>
     *
     * @return True if the network can be detached from its parent network.
     */
    boolean isDetachable();

    /**
     * Return all the boundary elements of the current network, i.e. the elements which link or might link this network
     * to an external voltage level.
     *
     * @return a set containing the boundary elements of the network.
     */
    Set<Identifiable<?>> getBoundaryElements();

    /**
     * Check if an identifiable is a boundary element for the current network.
     *
     * @param identifiable the identifiable to check
     * @return True if the identifiable is a boundary element for the current network
     */
    boolean isBoundaryElement(Identifiable<?> identifiable);

    /**
     * <p>Add a listener on the network.</p>
     * @param listener the listener to add
     */
    void addListener(NetworkListener listener);

    /**
     * <p>Remove a listener from the network.</p>
     * @param listener the listener to remove
     */
    void removeListener(NetworkListener listener);

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.NETWORK;
    }

    /**
     * If network is valid, do nothing.<br>
     * If network not valid, check if each network component is valid. A {@link ValidationException} is thrown with an explicit message if one network component is not valid.<br>
     * If all network components are valid, network validation status is updated to true.
     * Return the network validation status.
     */
    default ValidationLevel runValidationChecks() {
        return runValidationChecks(true);
    }

    /**
     * If network is valid, do nothing.<br>
     * If network not valid and <code>throwsException</code> is <code>true</code>, check if each network component is valid. A {@link ValidationException} is thrown with an explicit message if one network component is not valid.<br>
     * If all network components are valid, network validation status is updated to true.
     * Return the network validation status.
     */
    default ValidationLevel runValidationChecks(boolean throwsException) {
        return runValidationChecks(throwsException, ReportNode.NO_OP);
    }

    /**
     * If network is valid, do nothing.<br>
     * If network not valid and <code>throwsException</code> is <code>true</code>, check if each network component is valid. A {@link ValidationException} is thrown with an explicit message if one network component is not valid.<br>
     * If all network components are valid, network validation status is updated to true.
     * Return the network validation status.
     */
    default ValidationLevel runValidationChecks(boolean throwsException, ReportNode reportNode) {
        return ValidationLevel.STEADY_STATE_HYPOTHESIS;
    }

    /**
     * Return the network validation status. Do <b>not</b> run any validation check.
     */
    default ValidationLevel getValidationLevel() {
        return ValidationLevel.STEADY_STATE_HYPOTHESIS;
    }

    default Network setMinimumAcceptableValidationLevel(ValidationLevel validationLevel) {
        if (validationLevel != ValidationLevel.STEADY_STATE_HYPOTHESIS) {
            throw new UnsupportedOperationException("Validation level below STEADY_STATE_HYPOTHESIS not supported");
        }
        return this;
    }

    default Stream<Identifiable<?>> getIdentifiableStream(IdentifiableType identifiableType) {
        return switch (identifiableType) {
            case SWITCH -> getSwitchStream().map(Function.identity());
            case TWO_WINDINGS_TRANSFORMER -> getTwoWindingsTransformerStream().map(Function.identity());
            case THREE_WINDINGS_TRANSFORMER -> getThreeWindingsTransformerStream().map(Function.identity());
            case DANGLING_LINE -> getDanglingLineStream(DanglingLineFilter.ALL).map(Function.identity());
            case LINE -> getLineStream().map(Function.identity());
            case TIE_LINE -> getTieLineStream().map(Function.identity());
            case LOAD -> getLoadStream().map(Function.identity());
            case BATTERY -> getBatteryStream().map(Function.identity());
            case GENERATOR -> getGeneratorStream().map(Function.identity());
            case HVDC_LINE -> getHvdcLineStream().map(Function.identity());
            case SUBSTATION -> getSubstationStream().map(Function.identity());
            case VOLTAGE_LEVEL -> getVoltageLevelStream().map(Function.identity());
            case BUSBAR_SECTION -> getBusbarSectionStream().map(Function.identity());
            case SHUNT_COMPENSATOR -> getShuntCompensatorStream().map(Function.identity());
            case HVDC_CONVERTER_STATION -> getHvdcConverterStationStream().map(Function.identity());
            case STATIC_VAR_COMPENSATOR -> getStaticVarCompensatorStream().map(Function.identity());
            case GROUND -> getGroundStream().map(Function.identity());
            default -> throw new PowsyblException("can get a stream of " + identifiableType + " from a network.");
        };
    }

    /**
     * Write the network to a given format.
     *
     * @param format the export format
     * @param parameters some properties to configure the export
     * @param dataSource data source
     * @param reportNode the reportNode used for functional logs
     */
    default void write(ExportersLoader loader, String format, Properties parameters, DataSource dataSource, ReportNode reportNode) {
        Exporter exporter = Exporter.find(loader, format);
        if (exporter == null) {
            throw new PowsyblException("Export format " + format + " not supported");
        }
        exporter.export(this, parameters, dataSource, reportNode);
    }

    default void write(ExportersLoader loader, String format, Properties parameters, DataSource dataSource) {
        write(loader, format, parameters, dataSource, ReportNode.NO_OP);
    }

    default void write(String format, Properties parameters, DataSource dataSource) {
        write(new ExportersServiceLoader(), format, parameters, dataSource);
    }

    /**
     * Write the network to a given format.
     *
     * @param format the export format
     * @param parameters some properties to configure the export
     * @param file the network file
     * @param reportNode the reportNode used for functional logs
     */
    default void write(ExportersLoader loader, String format, Properties parameters, Path file, ReportNode reportNode) {
        DataSource dataSource = Exporters.createDataSource(file);
        write(loader, format, parameters, dataSource, reportNode);
    }

    default void write(ExportersLoader loader, String format, Properties parameters, Path file) {
        write(loader, format, parameters, file, ReportNode.NO_OP);
    }

    default void write(String format, Properties parameters, Path file) {
        write(new ExportersServiceLoader(), format, parameters, file);
    }

    /**
     * Write the network to a given format.
     *
     * @param format the export format
     * @param parameters some properties to configure the export
     * @param directory the output directory where files are generated
     * @param baseName a base name for all generated files
     * @param reportNode the reportNode used for functional logs
     */
    default void write(ExportersLoader loader, String format, Properties parameters, String directory, String baseName, ReportNode reportNode) {
        write(loader, format, parameters, new FileDataSource(Paths.get(directory), baseName), reportNode);
    }

    default void write(ExportersLoader loader, String format, Properties parameters, String directory, String basename) {
        write(loader, format, parameters, directory, basename, ReportNode.NO_OP);
    }

    default void write(String format, Properties parameters, String directory, String baseName) {
        write(new ExportersServiceLoader(), format, parameters, directory, baseName);
    }
}
