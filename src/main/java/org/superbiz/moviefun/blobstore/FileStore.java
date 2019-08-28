package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        File targetFile = getCoverFile(blob.name);

        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(FileCopyUtils.copyToByteArray(blob.inputStream));
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        Path coverFilePath;
        try {
            coverFilePath = getExistingCoverPath(name);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        String contentType = new Tika().detect(coverFilePath);
        InputStream in = new FileInputStream(coverFilePath.toFile());
        return Optional.of(new Blob(name, in, contentType));
    }

    @Override
    public void deleteAll() {
        File covers = new File("covers");
        if (!covers.exists()) {
            return;
        }
        for (File cover : covers.listFiles()) {
            cover.delete();
        }
        covers.delete();
    }


    private File getCoverFile(String albumId) {
        String coverFileName = format("covers/%s", albumId);
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(String albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }
}