package org.superbiz.moviefun.albums;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.*;
import java.util.Map;

import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;

    private final BlobStore store;

    public AlbumsController(AlbumsBean albumsBean, BlobStore store) {
        this.albumsBean = albumsBean;
        this.store = store;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable String albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        store.put(new Blob(albumId, uploadedFile.getInputStream(), uploadedFile.getContentType()));
        return format("redirect:/albums/%s", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable String albumId) throws IOException {
        Blob b = store.get(albumId).orElseThrow(FileNotFoundException::new);
        byte[] imageBytes = FileCopyUtils.copyToByteArray(b.inputStream);
        HttpHeaders headers = createImageHttpHeaders(b.contentType, imageBytes);
        return new HttpEntity<>(imageBytes, headers);
    }

    private HttpHeaders createImageHttpHeaders(String contentType, byte[] imageBytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }


}
