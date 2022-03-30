package com.possible.fileupload.services;

import com.possible.fileupload.model.UploadedFile;
import com.possible.fileupload.model.Users;
import com.possible.fileupload.model.dto.UserDto;

import java.util.List;

public interface UserService {
    Users saveUser(Users user);
    List<UploadedFile> getUploadedFiles(Long id);
    UserDto getUser(Long id);
}
