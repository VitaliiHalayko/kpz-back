package org.example.fileservice.kpz.controllers;

import lombok.RequiredArgsConstructor;
import org.example.fileservice.kpz.dto.TestRequest;
import org.example.fileservice.kpz.services.SchemaMapperService;
import org.example.fileservice.kpz.services.SchemaTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kpz/tests")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
public class TestsController {

    private final SchemaMapperService schemaMapperService;
    private final SchemaTestService schemaTestController;

    @PostMapping
    public ResponseEntity<?> tests(@RequestBody TestRequest testRequest) {
        try {
            var file = schemaMapperService.toFile(testRequest.getSchemaRequest());

            var result = schemaTestController.tests(file, testRequest.getTestCases());
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Error: " + ex.getMessage());
        }
    }
}
