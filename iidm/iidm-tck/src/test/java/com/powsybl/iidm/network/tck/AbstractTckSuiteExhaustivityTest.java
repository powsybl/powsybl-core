package com.powsybl.iidm.network.tck;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public abstract class AbstractTckSuiteExhaustivityTest {

    @Test
    void assertAllTckTestsAreInstanced() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String tckPackageName = "com.powsybl.iidm.network.tck";
        List<String> tckTestClasses = getPackageClasses(classLoader, tckPackageName)
                .filter(clazz -> !isAnonymousClass(clazz) && containsTest(classLoader, clazz))
                .toList();
        assertNotEquals(0, tckTestClasses.size(), "Tck package is not loaded in this build");

        String implementationPackageName = this.getClass().getPackageName();
        Stream<String> implementationTestClasses = getPackageClasses(classLoader, implementationPackageName);

        List<String> tckTestClassInheritors = implementationTestClasses
                .map(res -> getAncestorFromPackage(classLoader, tckPackageName, res))
                .filter(Optional::isPresent).map(Optional::get)
                .toList();

        List<String> unimplementedTckTestClasses = tckTestClasses.stream()
                .filter(clazz -> !tckTestClassInheritors.contains(clazz))
                .toList();

        assertEquals(List.of(), unimplementedTckTestClasses,
                "Some TCK test classes are not extended, so their test won't run");
    }

    private static boolean isAnonymousClass(String clazz) {
        return clazz.contains("$");
    }

    private boolean containsTest(ClassLoader classLoader, String className) {
        try {
            return Arrays.stream(classLoader.loadClass(className)
                    .getDeclaredMethods())
                    .map(method -> method.isAnnotationPresent(Test.class)
                            || method.isAnnotationPresent(TestFactory.class)
                            || method.isAnnotationPresent(ParameterizedTest.class))
                    .reduce(false, (a, b) -> a || b);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Optional<String> getAncestorFromPackage(ClassLoader classLoader, String packageName, String className) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            while (clazz.getSuperclass() != Object.class) {
                clazz = clazz.getSuperclass();
                if (clazz.getPackageName().startsWith(packageName)) {
                    return Optional.of(clazz.getCanonicalName());
                }
            }
            return Optional.empty();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Stream<String> getPackageClasses(ClassLoader classLoader, String packageName) {
        String packagePath = packageName.replace('.', '/');
        try {
            final URI pkgUri = requireNonNull(classLoader.getResource(packagePath)).toURI();
            switch (pkgUri.getScheme()) {
                case "file": {
                    try (InputStream tckTestResources = classLoader.getResourceAsStream(packagePath)) {
                        return new BufferedReader(new InputStreamReader(requireNonNull(tckTestResources))).lines()
                                .flatMap(str -> str.endsWith(".class") ?
                                        Stream.of(packageName + '.' + str) :
                                        getPackageClasses(classLoader, packageName + '.' + str))
                                .map(clazz -> clazz.replace(".class", ""));
                    }
                }
                case "jar": {
                    URL jarUrl = ((JarURLConnection) pkgUri.toURL().openConnection()).getJarFileURL();
                    List<String> files;
                    try (ZipInputStream zipEntries = new ZipInputStream(jarUrl.openStream())) {
                        files = new ArrayList<>();
                        ZipEntry entry;
                        while ((entry = zipEntries.getNextEntry()) != null) {
                            files.add(entry.getName());
                        }
                    }
                    return files.stream()
                            .filter(str -> str.endsWith(".class"))
                            .map(path -> path.replace('/', '.'))
                            .map(path -> path.replace(".class", ""));
                }
                default: return Stream.empty();
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
