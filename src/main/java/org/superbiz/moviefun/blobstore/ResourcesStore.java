package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

public class ResourcesStore implements BlobStore {

    private final Tika tika = new Tika();

    @Override
    public void put(Blob blob) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        File file = ResourceUtils.getFile("classpath:" + name);

        if (!file.exists()) {
            return Optional.empty();
        }

        return Optional.of(new Blob(
                name,
                new FileInputStream(file),
                tika.detect(file)
        ));
    }

    @Override
    public void deleteAll() {
        throw new RuntimeException("not implemented");
    }
}