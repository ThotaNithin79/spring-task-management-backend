package com.sribalajiads.task_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    // CONSTRUCTOR INJECTION:
    // This tells Spring: "Go to application.properties, find 'file.upload-dir',
    // and pass it to this constructor."
    @Autowired
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        // Create the path object from the config string
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            // Ensure the directory exists
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            // Normalize file name
            String originalFileName = file.getOriginalFilename();
            // Generate unique filename
            String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

            // Copy file to the target location
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }
}