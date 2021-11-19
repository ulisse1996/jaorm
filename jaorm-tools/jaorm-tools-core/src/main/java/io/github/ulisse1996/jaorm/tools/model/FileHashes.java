package io.github.ulisse1996.jaorm.tools.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class FileHashes {

    private List<FileHash> hashes;

    public FileHashes() {
        this(new ArrayList<>());
    }

    public FileHashes(List<FileHash> hashes) {
        this.hashes = hashes;
    }

    public List<FileHash> getHashes() {
        return hashes;
    }

    public void setHashes(List<FileHash> hashes) {
        this.hashes = hashes;
    }
}
