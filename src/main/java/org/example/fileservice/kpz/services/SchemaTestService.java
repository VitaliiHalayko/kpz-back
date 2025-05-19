package org.example.fileservice.kpz.services;

import lombok.RequiredArgsConstructor;
import org.example.fileservice.kpz.dto.TestCase;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchemaTestService {

    private final SchemaCompilerService schemaCompilerService;

    public String tests(File file, List<TestCase> testCases) {
        StringBuilder testsResult = new StringBuilder();
        for (var testCase : testCases) {
            String result = "";
            try {
                result = schemaCompilerService.runProgram(file.getAbsolutePath(), testCase.getInput());
                var resDouble = Double.parseDouble(result);
                var expectedDouble = Double.parseDouble(testCase.getExpected());
                if (resDouble == expectedDouble) {
                    testsResult.append("Test passed\n");
                } else {
                    testsResult.append("Test failed: expected ").append(testCase.getExpected())
                            .append(", but got ").append(result).append("\n");
                }
            } catch (NumberFormatException e) {
                if (result.equals(testCase.getExpected())) {
                    testsResult.append("Test passed\n");
                } else {
                    testsResult.append("Test failed: expected ").append(testCase.getExpected())
                            .append(", but got ").append(result).append("\n");
                }
            } catch (Exception e) {
                testsResult.append("Error: ").append(e.getMessage()).append("\n");
                continue;
            }
        }
        return testsResult.toString();
    }
}
