package com.possible.fileupload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class ResponseFile {
    private String name;
    private String type;
    private long size;
    private String url;
    private String message;
}
