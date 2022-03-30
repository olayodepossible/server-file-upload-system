package com.possible.fileupload.services;

import com.possible.fileupload.dto.ResponseFile;
import com.possible.fileupload.model.UploadedFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileUploadService {
    String uploadFileLocal(MultipartFile file);
    ResponseFile uploadFileDb(MultipartFile file);
    ResponseFile uploadFileToStorage(MultipartFile file, Long userId);
    UploadedFile downloadSavedFile(String id);
    List <ResponseFile> getSavedFiles();
    Resource loadFileAsResource(Long userId, String fileName);

}
