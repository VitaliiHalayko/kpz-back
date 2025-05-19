package org.example.fileservice.kpz;

import org.example.fileservice.kpz.dto.SchemaRequest;
import org.example.fileservice.kpz.services.SchemaCompilerService;
import org.example.fileservice.kpz.services.SchemaMapperService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class KpzApplication {

    public static void main(String[] args) {
        SpringApplication.run(KpzApplication.class, args);
    }
}
