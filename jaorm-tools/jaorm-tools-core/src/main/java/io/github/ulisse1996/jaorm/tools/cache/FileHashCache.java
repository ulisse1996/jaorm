package io.github.ulisse1996.jaorm.tools.cache;

import com.google.common.annotations.VisibleForTesting;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.tools.model.FileHash;
import io.github.ulisse1996.jaorm.tools.model.FileHashes;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FileHashCache {

    private static final Singleton<FileHashCache> INSTANCE = Singleton.instance();

    private final Map<String, String> hashCache;
    private final String originPath;

    private FileHashCache(String originPath) throws IOException {
        this.hashCache = new ConcurrentHashMap<>();
        this.originPath = originPath;
        this.loadCurrent();
    }

    private void loadCurrent() throws IOException {
        Path path = Paths.get(this.originPath, ".jaorm");
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        this.loadContent(path);
    }

    private void loadContent(Path path) throws IOException {
        Path store = path.resolve("store.xml");
        if (!Files.exists(store)) {
            return;
        }
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(FileHashes.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            FileHashes hashes = (FileHashes) unmarshaller.unmarshal(store.toFile());
            for (FileHash hash : hashes.getHashes()) {
                if (hash.getFile() != null) {
                    this.hashCache.put(hash.getFile(), hash.getHash());
                }
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public static synchronized FileHashCache getInstance(String originPath) throws IOException {
        if (!INSTANCE.isPresent()) {
            INSTANCE.set(new FileHashCache(originPath));
        }

        return INSTANCE.get();
    }

    public String getCurrentHash(String key) {
        return hashCache.getOrDefault(key, "");
    }

    public String calculateHash(String key) throws IOException, NoSuchAlgorithmException {
        byte[] bytes = isJar(key) ? this.readJarFile(key) : this.readJavaFile(key);
        return DatatypeConverter
                .printHexBinary(bytes);
    }

    private byte[] readJavaFile(String key) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        try (BufferedReader reader = Files.newBufferedReader(findPath(key))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                digest.update(currentLine.getBytes(StandardCharsets.UTF_8));
            }
        }
        return digest.digest();
    }

    private byte[] readJarFile(String key) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] bytes = Files.readAllBytes(findPath(key));
        digest.update(bytes);
        return digest.digest();
    }

    private boolean isJar(String key) {
        return key.startsWith("jar");
    }

    private Path findPath(String key) throws IOException {
        if (isJar(key)) {
            // Handle Jar file
            try (FileSystem fileSystem = FileSystems.newFileSystem(new URL(key).toURI(), new HashMap<>())) {
                String filePath = key.substring(key.lastIndexOf(".jar!") + 1);
                Path path = fileSystem.getPath(filePath);
                path = path.subpath(1, path.getNameCount());
                String fileName = path.getFileName().toString();
                Path tmp = createTmp(fileName);
                Files.copy(path, tmp, StandardCopyOption.REPLACE_EXISTING);
                return tmp;
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        } else {
            return Paths.get(key);
        }
    }

    @SuppressWarnings({"java:S899", "java:S5443", "ResultOfMethodCallIgnored"})
    private Path createTmp(String fileName) throws IOException {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("linux")) {
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
            return Files.createTempFile(
                    fileName.substring(0, fileName.lastIndexOf(".")),
                    ".class", attr);
        } else {
            File f = Files.createTempFile(fileName.substring(0, fileName.lastIndexOf(".")), ".class").toFile();
            f.setReadable(true, true);
            f.setWritable(true, true);
            f.setExecutable(true, true);
            return f.toPath();
        }
    }

    public void updateHash(String key, String hash) {
        this.hashCache.put(key, hash);
    }

    public void saveOnFile() throws IOException {
        Path store = Paths.get(this.originPath, ".jaorm").resolve("store.xml");
        if (!Files.exists(store)) {
            Files.createFile(store);
        }
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(FileHashes.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            FileHashes hashes = new FileHashes();
            for (Map.Entry<String, String> entry : this.hashCache.entrySet()) {
                hashes.getHashes().add(new FileHash(entry.getKey(), entry.getValue()));
            }
            marshaller.marshal(hashes, store.toFile());
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @VisibleForTesting
    Map<String, String> getHashCache() {
        return hashCache;
    }
}
