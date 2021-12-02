package io.github.ulisse1996.jaorm.tools.cache;

import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

class FileHashCacheTest {

    @BeforeEach
    @SuppressWarnings("unchecked")
    void reset() {
        try {
            Field field = FileHashCache.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            Singleton<FileHashCache> cache = (Singleton<FileHashCache>) field.get(null);
            cache.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_not_load_store() throws IOException {
        Path tempDirectory = Files.createTempDirectory(randomName());
        try {
            FileHashCache instance = FileHashCache.getInstance(tempDirectory.toString());
            Assertions.assertFalse(tempDirectory.resolve("store.xml").toFile().exists());
            Assertions.assertTrue(instance.getHashCache().isEmpty());
        } finally {
            removeFolder(tempDirectory);
        }
    }

    @Test
    void should_load_store() throws IOException, URISyntaxException {
        Path tempDirectory = Files.createTempDirectory(randomName());
        try {
            FileHashCache instance = createAndGetCache(tempDirectory);
            Assertions.assertFalse(instance.getHashCache().isEmpty());
            Assertions.assertNotNull(instance.getHashCache().get("text.txt"));
        } finally {
            removeFolder(tempDirectory);
        }
    }

    @Test
    void should_read_current_store_hash() throws IOException, URISyntaxException {
        Path tempDirectory = Files.createTempDirectory(randomName());
        try {
            FileHashCache instance = createAndGetCache(tempDirectory);
            Assertions.assertEquals("hash-test", instance.getCurrentHash("text.txt"));
        } finally {
            removeFolder(tempDirectory);
        }
    }

    @Test
    void should_update_hash() throws IOException, URISyntaxException {
        Path tempDirectory = Files.createTempDirectory(randomName());
        try {
            FileHashCache instance = createAndGetCache(tempDirectory);
            Assertions.assertEquals("hash-test", instance.getCurrentHash("text.txt"));
            instance.updateHash("text.txt", "next-hash");
            Assertions.assertEquals("next-hash", instance.getCurrentHash("text.txt"));
        } finally {
            removeFolder(tempDirectory);
        }
    }

    @Test
    void should_read_simple_file_hash() throws IOException, URISyntaxException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        Path tempDirectory = Files.createTempDirectory(randomName());
        Path tempFile = Files.createTempFile("file-test", ".java");
        Files.write(tempFile, Collections.singleton("my-line"));
        try {
            FileHashCache instance = createAndGetCache(tempDirectory);
            digest.update("my-line".getBytes(StandardCharsets.UTF_8));
            String result = DatatypeConverter
                    .printHexBinary(digest.digest());
            Assertions.assertEquals(result, instance.calculateHash(tempFile.toString()));
        } finally {
            removeFolder(tempDirectory);
            deleteFile(tempFile.toFile());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "linux"})
    void should_read_file_hash_from_jar(String osName) throws NoSuchAlgorithmException, IOException, URISyntaxException {
        String oldOs = System.getProperty("os.name");
        if (!osName.isEmpty()) {
            System.setProperty("os.name", "linux");
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        Path tempDirectory = Files.createTempDirectory(randomName());
        Path tempFile = createJar();
        URI uri = URI.create("jar:file:" + tempFile.toAbsolutePath());
        FileSystem fs = FileSystems.newFileSystem(uri, new HashMap<>());
        try {
            Path klassPath = fs.getPath("test-file.class");
            Files.write(klassPath, "my-text".getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
            FileHashCache instance = createAndGetCache(tempDirectory);
            digest.update("my-text".getBytes(StandardCharsets.UTF_8));
            String result = DatatypeConverter
                    .printHexBinary(digest.digest());
            fs.close(); // Need close for create new file system
            Assertions.assertEquals(result, instance.calculateHash(klassPath.toUri().toString()));
        } finally {
            removeFolder(tempDirectory);
            deleteFile(tempFile.toFile());
            if (!osName.isEmpty()) {
                System.setProperty("os.name", oldOs);
            }
        }
    }

    @Test
    void should_save_store() throws IOException, URISyntaxException {
        Path tempDirectory = Files.createTempDirectory(randomName());
        try {
            FileHashCache instance = createAndGetCache(tempDirectory);
            Assertions.assertEquals("hash-test", instance.getCurrentHash("text.txt"));
            instance.updateHash("text.txt", "next-hash");
            Assertions.assertEquals("next-hash", instance.getCurrentHash("text.txt"));
            Map<String, String> oldCache = instance.getHashCache();
            instance.saveOnFile();
            reset();
            Map<String, String> hashCache = FileHashCache.getInstance(tempDirectory.toString()).getHashCache();
            Assertions.assertEquals(oldCache, hashCache);
        } finally {
            removeFolder(tempDirectory);
        }
    }

    @Test
    void should_save_empty_store() throws IOException {
        Path tempDirectory = Files.createTempDirectory(randomName());
        try {
            Path dirJaorm = tempDirectory.resolve(".jaorm");
            Files.createDirectory(dirJaorm);
            FileHashCache instance = FileHashCache.getInstance(tempDirectory.toString());
            Map<String, String> oldCache = instance.getHashCache();
            instance.saveOnFile();
            reset();
            Map<String, String> hashCache = FileHashCache.getInstance(tempDirectory.toString()).getHashCache();
            Assertions.assertEquals(oldCache, hashCache);
        } finally {
            removeFolder(tempDirectory);
        }
    }

    private Path createJar() throws IOException {
        Path tempFile = Files.createTempFile("test-jar", ".jar");
        try (JarOutputStream os = new JarOutputStream(Files.newOutputStream(tempFile))) {
            ZipEntry entry = new ZipEntry("test-file.class");
            os.putNextEntry(entry);
        }

        return tempFile;
    }

    private FileHashCache createAndGetCache(Path tempDirectory) throws IOException, URISyntaxException {
        Path dirJaorm = tempDirectory.resolve(".jaorm");
        Files.createDirectory(dirJaorm);
        Files.copy(
                Paths.get(Objects.requireNonNull(FileHashCacheTest.class.getResource("/store.xml")).toURI()),
                dirJaorm.resolve("store.xml")
        );
        return FileHashCache.getInstance(tempDirectory.toString());
    }

    private String randomName() {
        return String.format("Dir-%s-%d", new SimpleDateFormat("ddMMyy-hhmmss.SSS").format(new Date()), new SecureRandom().nextInt());
    }

    private void removeFolder(Path tempDirectory) throws IOException {
        File file = tempDirectory.toFile();
        if (file.exists()) {
            deleteFile(tempDirectory.toFile());
        }
    }

    private void deleteFile(File file) throws IOException {
        if (file.isDirectory()) {
            for (File f : Optional.ofNullable(file.listFiles()).orElse(new File[0])) {
                deleteFile(f);
            }
        }

        Files.deleteIfExists(file.toPath());
    }
}
