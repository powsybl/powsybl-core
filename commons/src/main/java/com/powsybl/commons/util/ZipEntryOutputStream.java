package com.powsybl.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.io.ForwardingOutputStream;

import net.java.truevfs.comp.zip.ZipEntry;
import net.java.truevfs.comp.zip.ZipFile;
import net.java.truevfs.comp.zip.ZipOutputStream;

public final class ZipEntryOutputStream extends ForwardingOutputStream<ZipOutputStream> {

    private final Path zipFilePath;

    private final String fileName;

    private boolean closed;

    public ZipEntryOutputStream(Path zipFilePath, String fileName) throws IOException {
        super(new ZipOutputStream(Files.newOutputStream(getTmpZipFilePath(zipFilePath))));
        this.zipFilePath = zipFilePath;
        this.fileName = fileName;
        this.closed = false;

        // create new entry
        os.putNextEntry(new ZipEntry(fileName));
    }

    private static Path getTmpZipFilePath(Path zipFilePath) {
        return zipFilePath.getParent().resolve(zipFilePath.getFileName() + ".tmp");
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            // close new entry
            os.closeEntry();

            // copy existing entries
            if (Files.exists(zipFilePath)) {
                try (ZipFile zipFile = new ZipFile(zipFilePath)) {
                    Enumeration<? extends ZipEntry> e = zipFile.entries();
                    while (e.hasMoreElements()) {
                        ZipEntry zipEntry = e.nextElement();
                        if (!zipEntry.getName().equals(fileName)) {
                            os.putNextEntry(zipEntry);
                            try (InputStream zis = zipFile.getInputStream(zipEntry.getName())) {
                                ByteStreams.copy(zis, os);
                            }
                            os.closeEntry();
                        }
                    }
                }
            }

            // close zip
            super.close();

            // swap with tmp zip
            Path tmpZipFilePath = getTmpZipFilePath(zipFilePath);
            Files.move(tmpZipFilePath, zipFilePath, StandardCopyOption.REPLACE_EXISTING);

            closed = true;
        }
    }
}
