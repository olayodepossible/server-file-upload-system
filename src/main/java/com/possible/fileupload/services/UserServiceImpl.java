package com.possible.fileupload.services;

import com.possible.fileupload.exceptions.UserNotFoundException;
import com.possible.fileupload.model.UploadedFile;
import com.possible.fileupload.model.Users;
import com.possible.fileupload.model.dto.UserDto;
import com.possible.fileupload.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Users saveUser(Users user) {
        return userRepository.save(user);
    }

    @Override
    public List<UploadedFile> getUploadedFiles(Long id) {
//        Users user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        Users user = userRepository.getById(id);
//        for(UploadedFile file : user.getUploadedFiles()){
//            log.info(file.toString());
//        }

        return user.getUploadedFiles();
    }

    @Override
    public UserDto getUser(Long id) {
     Users user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

     return UserDto.builder()
             .name(user.getName())
             .email(user.getEmail())
             .uploadedFiles(user.getUploadedFiles())
             .build();

    }
}
