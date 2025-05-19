package org.example.fileservice.kpz.services;

import jakarta.annotation.PostConstruct;
import org.example.fileservice.kpz.dto.SchemaRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Service
public class SchemaMapperService {

    private static final String BASE_DIR = "files";
    private final List<String> languages = List.of("java", "c++", "python", "c#");

    @PostConstruct
    public void init() {
        File dir = new File(BASE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public File toFile(SchemaRequest schemaRequest) {
        String fileClass = FileNameGenerator.generateRandomFileName();
        String extension = switch (schemaRequest.getLang().toLowerCase()) {
            case "java" -> "java";
            case "c++" -> "cpp";
            case "python" -> "py";
            case "c#" -> "cs";
            default -> throw new IllegalArgumentException("Unsupported language: " + schemaRequest.getLang());
        };
        String fileName = fileClass + "." + extension;
        File file = new File(BASE_DIR + File.separator + fileName);

        String code = toProgrammingLanguage(schemaRequest, fileClass);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(code);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file", e);
        }

        return file;
    }

    private String toProgrammingLanguage(SchemaRequest request, String className) {
        if (request.getLang() == null || request.getLang().isEmpty()) {
            throw new IllegalArgumentException("Language is not specified");
        }
        String lang = request.getLang().toLowerCase();
        if (!languages.contains(lang)) {
            throw new IllegalArgumentException("Unsupported language: " + lang);
        }

        List<String> lines = List.of(request.getSchema().split("\n"));
        StringBuilder result = new StringBuilder();
        List<String> inputVars = new ArrayList<>();
        Set<String> doubleVars = new HashSet<>();
        int indentLevel = lang.equals("python") ? 1 : 2;

        // Headers
        switch (lang) {
            case "java" -> {
                result.append("public class ").append(className).append(" {\n")
                        .append("    public static void main(String[] args) {\n");
            }
            case "c++" -> {
                result.append("#include <iostream>\n#include <cmath>\nusing namespace std;\n\n")
                        .append("int main(int argc, char* argv[]) {\n");
            }
            case "python" -> result.append("import sys\nimport math\n\ndef main():\n");
            case "c#" -> {
                result.append("using System;\n\n")
                        .append("class ").append(className).append(" {\n")
                        .append("    static void Main(string[] args) {\n");
            }
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("start") || line.startsWith("lang")) continue;
            if (line.startsWith("end") && !line.equals("endif")) {
                break;
            }

            if (line.contains("sqrt")) {
                switch (lang) {
                    case "java" -> line = line.replace("sqrt", "Math.sqrt");
                    case "c#" -> line = line.replace("sqrt", "Math.Sqrt");
                    case "c++" -> line = line.replace("sqrt", "sqrt");
                    case "python" -> line = line.replace("sqrt", "math.sqrt");
                }
            }

            if (line.contains("^")) {
                switch (lang) {
                    case "python" -> line = line.replace("^", "**");
                    case "c++" -> {
                        while (line.contains("^")) {
                            var before = line.charAt(line.indexOf("^") - 2);
                            var after = line.charAt(line.indexOf("^") + 2);
                            line = line.replace(before + " ^ " + after, "pow(" + before + ", " + after + ")");
                        }
                    }
                    case "java" -> {
                        while (line.contains("^")) {
                            var before = line.charAt(line.indexOf("^") - 2);
                            var after = line.charAt(line.indexOf("^") + 2);
                            line = line.replace(before + " ^ " + after, "Math.pow(" + before + ", " + after + ")");
                        }
                    }
                    case "c#" -> {
                        while (line.contains("^")) {
                            var before = line.charAt(line.indexOf("^") - 2);
                            var after = line.charAt(line.indexOf("^") + 2);
                            line = line.replace(before + " ^ " + after, "Math.Pow(" + before + ", " + after + ")");
                        }
                    }
                }
            }

            String indent = "    ".repeat(indentLevel);

            if (line.startsWith("input")) {
                String[] vars = line.substring(6).split(",\\s*");
                for (int j = 0; j < vars.length; j++) {
                    String var = vars[j].trim();
                    inputVars.add(var);
                    switch (lang) {
                        case "java" -> result.append(indent).append("double ").append(var)
                                .append(" = Double.parseDouble(args[").append(j).append("]);\n");
                        case "c++" -> result.append(indent).append("double ").append(var)
                                .append(" = atof(argv[").append(j + 1).append("]);\n");
                        case "python" -> result.append(indent).append(var)
                                .append(" = float(sys.argv[").append(j + 1).append("])\n");
                        case "c#" -> result.append(indent).append("double ").append(var)
                                .append(" = double.Parse(args[").append(j).append("]);\n");
                    }
                    doubleVars.add(var);
                }
            } else if (line.startsWith("v ")) {
                String declaration = line.substring(2).trim();
                String varName = declaration.split("=")[0].trim();
                switch (lang) {
                    case "java", "c#" -> result.append(indent).append("double ").append(declaration).append(";\n");
                    case "c++" -> result.append(indent).append("double ").append(declaration).append(";\n");
                    case "python" -> result.append(indent).append(declaration).append("\n");
                }
                doubleVars.add(varName);
            } else if (line.startsWith("c ")) {
                String declaration = line.substring(2).trim();
                String varName = declaration.split("=")[0].trim();
                doubleVars.add(varName);
                if (lang.equals("python")) {
                    result.append(indent).append(varName).append(" = ").append(declaration.split("=")[1].trim()).append("\n");
                } else {
                    String constDecl = switch (lang) {
                        case "java" -> "final ";
                        case "c++", "c#" -> "const ";
                        default -> "";
                    };
                    result.append(indent).append(constDecl).append("double ").append(declaration).append(";\n");
                }
            } else if (line.contains("=") && !line.startsWith("if")) {
                String expr = line.replace("^", "**");
                String varName = line.split("=")[0].trim();
                if (expr.contains("sqrt") || expr.contains("**") || expr.contains("/")) {
                    doubleVars.add(varName);
                }
                if (!lang.equals("python")) {
                    expr = expr.replace("**", "Math.pow(").replace(")", ", 2)");
                    expr = expr.replace("sqrt", "Math.sqrt");
                }
                result.append(indent).append(expr).append("\n");
            } else if (line.startsWith("if")) {
                String condition = lines.get(i).trim();

                condition = condition.replace("if ", "").replace("and", lang.equals("python") ? "and" : "&&");
                switch (lang) {
                    case "java", "c#" -> result.append(indent).append("if (").append(condition).append(") {\n");
                    case "c++" -> result.append(indent).append("if (").append(condition).append(") {\n");
                    case "python" -> result.append(indent).append("if ").append(condition).append(":\n");
                }
                indentLevel++;
            } else if (line.equals("else")) {
                indentLevel--;
                indent = "    ".repeat(indentLevel);
                switch (lang) {
                    case "java", "c#" -> result.append(indent).append("} else {\n");
                    case "c++" -> result.append(indent).append("} else {\n");
                    case "python" -> result.append(indent).append("else:\n");
                }
                indentLevel++;
            } else if (line.equals("endif")) {
                indentLevel--;
                indent = "    ".repeat(indentLevel);
                if (!lang.equals("python")) {
                    result.append(indent).append("}\n");
                }
            } else if (line.startsWith("print")) {
                String toPrint = line.substring(6).trim();
                // Check if toPrint is a variable or a string literal
                if (!doubleVars.contains(toPrint) && !toPrint.matches("\\d+(\\.\\d+)?")) {
                    toPrint = "\"" + toPrint + "\"";
                }
                switch (lang) {
                    case "java" -> result.append(indent).append("System.out.println(").append(toPrint).append(");\n");
                    case "c#" -> result.append(indent).append("Console.WriteLine(").append(toPrint).append(");\n");
                    case "c++" -> result.append(indent).append("cout << ").append(toPrint).append(" << endl;\n");
                    case "python" -> result.append(indent).append("print(").append(toPrint).append(")\n");
                }
            }
        }

        // End block
        indentLevel--;
        String indent = "    ".repeat(indentLevel);
        switch (lang) {
            case "java", "c#" -> result.append(indent).append("}\n}\n");
            case "c++" -> result.append(indent).append("return 0;\n}\n");
            case "python" -> result.append("\nif __name__ == '__main__':\n    main()\n");
        }

        return result.toString();
    }

    // Mock FileNameGenerator
    static class FileNameGenerator {
        private static final Random random = new Random();
        public static String generateRandomFileName() {
            return "Class" + random.nextInt(10000);
        }
    }
}