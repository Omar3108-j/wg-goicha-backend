package com.wggoicha.backend.controller;

import com.wggoicha.backend.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping
    public String upload(@RequestParam("file") MultipartFile file) {
        return cloudinaryService.uploadImage(file);
    }
}
