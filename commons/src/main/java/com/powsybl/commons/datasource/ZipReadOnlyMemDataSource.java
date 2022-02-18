package com.powsybl.commons.datasource;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.io.ForwardingInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipReadOnlyMemDataSource implements ReadOnlyDataSource {

    private final Map<String, byte[]> data = new HashMap<>();

    private final String baseName;

    public ZipReadOnlyMemDataSource() {
        this("");
    }

    public ZipReadOnlyMemDataSource(String baseName) {
        this.baseName = Objects.requireNonNull(baseName);
    }

    public byte[] getData(String suffix, String ext) {
        return getData(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    public byte[] getData(String fileName) {
        return data.get(fileName);
    }

    public void putData(String fileName, ZipInputStream data) {
        try {
            ZipEntry entry = data.getNextEntry();
            while (entry != null) {
                String filename = entry.getName();
                this.data.put(entry.getName(), ByteStreams.toByteArray(data));
                double length = this.data.size();
                entry = data.getNextEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void putData(String fileName, byte[] data) {
        this.data.put(fileName, data);

    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    @Override
    public boolean exists(String suffix, String ext) throws IOException {
        return exists(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        Objects.requireNonNull(fileName);
        return data.containsKey(fileName);
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        return newInputStream(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        Objects.requireNonNull(fileName);
        byte[] ba = data.get(fileName);
        if (ba == null) {
            throw new IOException(fileName + " does not exist");
        }
        return new ByteArrayInputStream(ba);
    }

    @Override
    public Set<String> listNames(String regex) throws IOException {
        Pattern p = Pattern.compile(regex);
        return data.keySet().stream()
                .filter(name -> p.matcher(name).matches())
                .collect(Collectors.toSet());
    }

    private static final class ZipEntryInputStream extends ForwardingInputStream<InputStream> {

        private final ZipFile zipFile;

        public ZipEntryInputStream(ZipFile zipFile, String fileName) throws IOException {
            super(zipFile.getInputStream(zipFile.getEntry(fileName)));
            this.zipFile = zipFile;
        }

        @Override
        public void close() throws IOException {
            super.close();

            zipFile.close();
        }
    }

}
