package org.example.fileservice.kpz.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SchemaCompilerService {

    public String runProgram(String filePath, List<String> args) throws IOException, InterruptedException {
        String extension = getFileExtension(filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            log.error("File not found: " + filePath);
            return null;
        }

        String fileName = file.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String directory = file.getParent() != null ? file.getParent() : ".";

        ProcessBuilder pb;
        switch (extension) {
            case "py":
                List<String> pyCommand = new ArrayList<>(List.of("python", filePath));
                pyCommand.addAll(args);
                pb = new ProcessBuilder(pyCommand);
                break;
            case "cpp":
                String exeFile = directory + File.separator + baseName + ".exe";
                pb = new ProcessBuilder("g++", filePath, "-o", exeFile);
                if (!executeCompilation(pb, "C++")) {
                    return null;
                }
                List<String> cppCommand = new ArrayList<>(List.of(exeFile));
                cppCommand.addAll(args);
                pb = new ProcessBuilder(cppCommand);
                break;
            case "cs":
                String exeFileCs = directory + File.separator + baseName + ".exe";
                pb = new ProcessBuilder("C:\\Windows\\Microsoft.NET\\Framework\\v4.0.30319\\csc.exe", "/out:" + exeFileCs, filePath);
                if (!executeCompilation(pb, "C#")) {
                    return null;
                }
                List<String> csCommand = new ArrayList<>(List.of(exeFileCs));
                csCommand.addAll(args);
                pb = new ProcessBuilder(csCommand);
                break;
            case "java":
                pb = new ProcessBuilder("javac", filePath);
                if (!executeCompilation(pb, "Java")) {
                    return null;
                }
                List<String> javaCommand = new ArrayList<>(List.of("java", "-cp", directory, baseName));
                javaCommand.addAll(args);
                pb = new ProcessBuilder(javaCommand);
                break;
            default:
                log.error("Unsupported file type: " + extension);
                return null;
        }

        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return "Execution failed with exit code: " + exitCode + "\n" + output;
            }
            return output.toString();
        } catch (IOException e) {
            log.error("Error executing process: " + e.getMessage());
            if (e.getMessage().contains("error=225")) {
               log.error("Execution failed: " + e.getMessage());
            }
            return null;
        }
    }

    private boolean executeCompilation(ProcessBuilder pb, String language) throws IOException, InterruptedException {
        pb.redirectErrorStream(true);
        Process compileProcess = pb.start();
        StringBuilder errorOutput = new StringBuilder();
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
        }
        int compileExitCode = compileProcess.waitFor();
        if (compileExitCode != 0) {
            throw new IOException("Compilation failed with exit code: " + compileExitCode + "\n" + errorOutput);
        }
        return true;
    }

    private static String getFileExtension(String filePath) {
        String fileName = new File(filePath).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }
}