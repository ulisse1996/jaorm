package io.github.ulisse1996.jaorm.tools.model;

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
}
