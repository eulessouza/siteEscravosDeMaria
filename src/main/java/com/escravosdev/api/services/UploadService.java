package com.escravosdev.api.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class UploadService {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @Value("${upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private static final List<String> ALLOWED_FORUM_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif",
            "video/mp4", "video/x-matroska", "video/quicktime",
            "video/x-msvideo", "video/webm"
    );

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;  // 10MB
    private static final long MAX_VIDEO_SIZE = 100L * 1024 * 1024; // 100MB

    public String uploadImage(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE, "Apenas JPG, PNG e WEBP são permitidos no blog");
        return save(file);
    }

    public String uploadForumFile(MultipartFile file) {
        validateFile(file, ALLOWED_FORUM_TYPES, MAX_VIDEO_SIZE, "Tipo de arquivo não permitido no fórum");
        // imagens têm limite menor
        if (isImage(file) && file.getSize() > MAX_IMAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Imagens devem ter no máximo 10MB");
        }
        return save(file);
    }

    private void validateFile(MultipartFile file, List<String> allowed, long maxSize, String errorMsg) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo vazio");
        }
        if (!allowed.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }
        if (file.getSize() > maxSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Arquivo excede o tamanho máximo permitido");
        }
    }

    private String save(MultipartFile file) {
        try {
            var uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            var ext      = getExtension(file.getOriginalFilename());
            var filename = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            var dest     = uploadPath.resolve(filename);

            file.transferTo(dest);

            return baseUrl + "/" + filename;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar arquivo");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private boolean isImage(MultipartFile file) {
        var ct = file.getContentType();
        return ct != null && ct.startsWith("image/");
    }
}