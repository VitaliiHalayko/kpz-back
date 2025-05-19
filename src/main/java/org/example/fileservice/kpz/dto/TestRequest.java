package org.example.fileservice.kpz.dto;

import lombok.Data;

import java.util.List;

@Data
public class TestRequest {
    private SchemaRequest schemaRequest;
    private List<TestCase> testCases;
}
