package io.github.ulisse1996.jaorm.validation.cache;

import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.validation.model.FileHash;
import io.github.ulisse1996.jaorm.validation.model.FileHashes;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
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
        MessageDigest digest = MessageDigest.getInstance("md5");
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(key))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                digest.update(currentLine.getBytes(StandardCharsets.UTF_8));
            }
        }
        byte[] bytes = digest.digest();
        return DatatypeConverter
                .printHexBinary(bytes);
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
}
