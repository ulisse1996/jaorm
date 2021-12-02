package io.github.ulisse1996.jaorm.tools.model;

import java.util.Objects;

public class FileHash {

    private String file;
    private String hash;

    public FileHash() {
        this(null, null);
    }

    public FileHash(String file, String hash) {
        this.file = file;
        this.hash = hash;
    }

    public String getFile() {
        return file;
    }

    public String getHash() {
        return hash;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileHash fileHash = (FileHash) o;
        return Objects.equals(file, fileHash.file) && Objects.equals(hash, fileHash.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, hash);
    }
}
