package org.example.fileservice.kpz.dto;

import lombok.Data;

import java.util.List;

@Data
public class TestCase {
    private List<String> input;
    private String expected;
}
