package com.wggoicha.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String folder;

    @Value("${app.upload.dir:${java.io.tmpdir}/wg-goicha/productos}")
    private String uploadDir;

    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se recibió ninguna imagen");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo debe ser una imagen válida");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image"
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {
            System.err.println("ERROR CLOUDINARY: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return saveImageLocally(file);
        }
    }

    /* Upload fallback local storage V1 */
    private String saveImageLocally(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String extension = resolveExtension(file);
            String filename = UUID.randomUUID() + extension;
            Path target = uploadPath.resolve(filename).normalize();

            if (!target.startsWith(uploadPath)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre de archivo inválido");
            }

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/productos/" + filename;
        } catch (IOException | RuntimeException localError) {
            System.err.println("ERROR LOCAL UPLOAD: " + localError.getClass().getSimpleName() + " - " + localError.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo subir ni guardar localmente la imagen.",
                    localError
            );
        }
    }

    private String resolveExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename != null) {
            int lastDot = originalFilename.lastIndexOf(".");
            if (lastDot >= 0 && lastDot < originalFilename.length() - 1) {
                String extension = originalFilename.substring(lastDot).toLowerCase();
                if (extension.matches("\\.(jpg|jpeg|png|webp|gif)")) {
                    return extension;
                }
            }
        }

        String contentType = file.getContentType();
        if ("image/png".equals(contentType)) return ".png";
        if ("image/webp".equals(contentType)) return ".webp";
        if ("image/gif".equals(contentType)) return ".gif";
        return ".jpg";
    }
}
