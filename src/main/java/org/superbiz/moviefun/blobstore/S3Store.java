package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import java.io.IOException;
import java.util.Optional;

public class S3Store implements BlobStore {
    private final AmazonS3Client s3Client;
    private final String bucketName;

    public S3Store(AmazonS3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public void put(Blob blob) throws IOException {
        try {
            if (!s3Client.doesBucketExist(bucketName)) {
                s3Client.createBucket(bucketName);
            }

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(blob.contentType);
            s3Client.putObject(bucketName, blob.name, blob.inputStream, metadata);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        try {
            S3Object o = s3Client.getObject(bucketName, name);
            return Optional.of(new Blob(name, o.getObjectContent(), o.getObjectMetadata().getContentType()));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void deleteAll() {
        s3Client.deleteBucket(bucketName);
    }
}
