package com.powsybl.cgmes;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.triplestore.CgmesModelTripleStore;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.triplestore.TripleStore;
import com.powsybl.triplestore.TripleStoreException;
import com.powsybl.triplestore.TripleStoreFactory;

public final class CgmesModelFactory {

    private CgmesModelFactory() {
    }

    public static CgmesModelTripleStore create(ReadOnlyDataSource ds, String tripleStoreImpl) {
        CgmesOnDataSource cds = new CgmesOnDataSource(ds);
        TripleStore tripleStore = TripleStoreFactory.create(tripleStoreImpl);
        CgmesModelTripleStore cgmes = new CgmesModelTripleStore(cds.cimNamespace(), tripleStore);
        read(cgmes, cds);
        return cgmes;
    }

    private static void read(CgmesModelTripleStore cgmes, CgmesOnDataSource cds) {
        String base = cds.baseName();
        for (String name : cds.names()) {
            LOG.info("Reading [{}]", name);
            try (InputStream is = cds.dataSource().newInputStream(name)) {
                cgmes.read(base, name, is);
            } catch (IOException e) {
                String msg = String.format("Reading [%s]", name);
                LOG.warn(msg);
                throw new TripleStoreException(msg, e);
            }
        }
    }

    static final Logger LOG = LoggerFactory.getLogger(CgmesModelFactory.class);
}
