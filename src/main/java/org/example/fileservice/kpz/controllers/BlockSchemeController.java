package org.example.fileservice.kpz.controllers;

import lombok.RequiredArgsConstructor;
import org.example.fileservice.kpz.dto.SchemaRequest;
import org.example.fileservice.kpz.dto.UserFile;
import org.example.fileservice.kpz.services.SchemaMapperService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/kpz/schema")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
public class BlockSchemeController {

    private final SchemaMapperService schemaMapperService;

    @PostMapping
    public ResponseEntity<byte[]> schemaToFile(@RequestBody SchemaRequest schemaRequest) {
        try {
            File file = schemaMapperService.toFile(schemaRequest);

            UserFile userFile = new UserFile();
            userFile.setFileName(file.getName());
            userFile.setContentType("application/octet-stream");
            userFile.setFileSize(file.length());
            userFile.setFileData(java.nio.file.Files.readAllBytes(file.toPath()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(userFile.getContentType()));
            headers.setContentLength(userFile.getFileSize());
            headers.setContentDispositionFormData("attachment", userFile.getFileName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(userFile.getFileData());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(("Error: " + ex.getMessage()).getBytes());
        }
    }
}
