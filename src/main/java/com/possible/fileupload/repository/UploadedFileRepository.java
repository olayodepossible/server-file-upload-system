package com.possible.fileupload.repository;

import com.possible.fileupload.model.UploadedFile;
import org.springframework.data.repository.CrudRepository;

public interface UploadedFileRepository extends CrudRepository<UploadedFile, String> {
}
