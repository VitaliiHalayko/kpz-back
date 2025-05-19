package org.example.fileservice.kpz.dto;

import lombok.Data;

@Data
public class UserFile {
    private String contentType;
    private String fileName;
    private Long fileSize;
    private byte[] fileData;
}
