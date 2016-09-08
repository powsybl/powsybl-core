/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

/**
 * Created with IntelliJ IDEA.
 * User: pduchesne
 * Date: 6/06/13
 * Time: 15:18
 * To change this template use File | Settings | File Templates.
 */
public class HarvestMonitorJob
    extends AbstractDatasourceJob
{

    WatchService watchService;
    File monitoredDir;

    public HarvestMonitorJob(File monitoredDir) throws IOException {
        this.monitoredDir = monitoredDir;

        watchService = FileSystems.getDefault().newWatchService();
    }


    @Override
    public void run() {
        try {


            Path path = monitoredDir.toPath();
            path.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE);

            //start an infinite loop
            while(true){

                //remove the next watch key
                final WatchKey key = watchService.take();

                //get list of events for the watch key
                for (WatchEvent<?> watchEvent : key.pollEvents()) {

                    //get the filename for the event
                    final WatchEvent<Path> ev = (WatchEvent<Path>)watchEvent;
                    final Path filename = ev.context();

                    //TODO
                }

                //reset the key
                boolean valid = key.reset();

                //exit loop if the key is not valid
                //e.g. if the directory was deleted
                if (!valid) {
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Harvest job ended unexpectedly", e);
        }
    }
}
