package com.possible.fileupload.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DalimParam {
    @JsonProperty("return")
    private JdfFile response;

    @Data
    public static  class JdfFile{
        private String source;
        private String status;
        private String destination;

    }
}
