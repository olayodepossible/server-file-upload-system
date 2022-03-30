package com.possible.fileupload.services;

import com.possible.fileupload.dto.ResponseFile;
import com.possible.fileupload.exceptions.FileNotFoundException;
import com.possible.fileupload.exceptions.UserNotFoundException;
import com.possible.fileupload.model.UploadedFile;
import com.possible.fileupload.model.Users;
import com.possible.fileupload.repository.UploadedFileRepository;
import com.possible.fileupload.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private File uploadFolderPath = new File("C:\\Users\\olayo\\OneDrive\\Documents\\new_upload" );
    private Path uploadFolderPath2 = Paths.get("C:\\Users\\olayo\\OneDrive\\Documents\\new_upload\\");
    private final UploadedFileRepository fileRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public FileUploadServiceImpl(UploadedFileRepository fileRepository, UserRepository userRepository, UserService userService) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public String uploadFileLocal(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String realName = originalName.substring(0, originalName.lastIndexOf("."));
        String fileExt = originalName.substring(originalName.lastIndexOf("."));
        String fileName = realName+"_"+System.currentTimeMillis()+fileExt;
        File fileWrite = new File(uploadFolderPath, fileName);
        try {

            byte[] fileData = file.getBytes();
//            Path path = Paths.get(uploadFolderPath + file.getOriginalFilename());
//            Files.write(path, fileData);
            Files.write(fileWrite.toPath(), fileData);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Locally Uploaded the file: "+file.getOriginalFilename() +" successfully";
    }

    @Override
    public ResponseFile uploadFileToStorage(MultipartFile file,  Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User not found"));
        String originalName = file.getOriginalFilename();
        String realName = Objects.requireNonNull(originalName).substring(0, originalName.lastIndexOf("."));
        String fileExt = originalName.substring(originalName.lastIndexOf("."));
        String fileName = realName+"_"+user.getName()+"-"+System.currentTimeMillis()+fileExt;
        Path path = Paths.get(uploadFolderPath2+"\\"+fileName);
        try {

            byte[] fileData = file.getBytes();
            if (fileData.length > 0){
                log.info(String.valueOf(path));
                Files.write(path, fileData);
                return saveFileProperties(fileName,fileExt,originalName,user);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseFile.builder()
                .message("The uploaded file is empty")
                .build();
    }

    private ResponseFile saveFileProperties(String fileName, String fileFormat, String contentType, Users user) {
        UploadedFile savedFile;

        UploadedFile uploadedFile = UploadedFile.builder()
                .fileName(fileName)
                .fileType(contentType)
                .fileFormat(fileFormat)
                .user(user)
                .build();
        savedFile = fileRepository.save(uploadedFile);
        return getResponseFile(savedFile);
    }

    @Override
    public Resource loadFileAsResource(Long userId, String fileName) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
       for (UploadedFile f : userService.getUser(userId).getUploadedFiles()){
           String storedName = f.getFileName();
           if (storedName.split("_")[0].equalsIgnoreCase(fileName)){
               try {
                   Path filePath = this.uploadFolderPath2.resolve(storedName);
                   Resource resource = new UrlResource(filePath.toUri());
                   if (resource.exists()){
                       return resource;
                   }else {
                       throw new FileNotFoundException(fileName+" file not found");
                   }
               } catch (MalformedURLException e) {
                   throw  new FileNotFoundException("File not found");
               }
           }

       }
        return null;
    }


    @Override
    public ResponseFile uploadFileDb(MultipartFile file) {
        UploadedFile savedFile;
        try {
            UploadedFile uploadedFile = UploadedFile.builder()
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileData(file.getBytes())
                    .build();
            savedFile = fileRepository.save(uploadedFile);
            return getResponseFile(savedFile);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UploadedFile downloadSavedFile(String id) {
      return fileRepository.findById(id).orElseThrow(() -> new FileNotFoundException("File not found"));
//        Optional<UploadedFile> optionalEntity = fileRepository.findById(id);
//        return optionalEntity.get();
//        return optionalEntity.orElseThrow();
    }

    @Override
    public List<ResponseFile> getSavedFiles() {
        List<ResponseFile> files = new ArrayList<>();
        fileRepository.findAll().forEach(file ->{
            ResponseFile responseFile = getResponseFile(file);
            files.add(responseFile);
        });
        return files;
    }

    private ResponseFile getResponseFile(UploadedFile file) {
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/files/download/")
                .path(file.getId())
                .toUriString();

        return new ResponseFile(
                 file.getFileName(),
                 file.getFileType(),
                 file.getFileData().length,
                 fileDownloadUri, "File uploaded Successfully");
    }

   /* public UploadFileResponse uploadFileDb(MultipartFile file, Long userId) {
        FileStorageProperties savedFile;

        try {
            if (file.getBytes().length > 0){
                FileStorageProperties uploadedFile = FileStorageProperties.builder()
                        .fileName(file.getOriginalFilename())
                        .documentType(file.getContentType())
                        .fileData(file.getBytes())
                        .documentFormat(file.getContentType())
                        .userId(userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User not found", userId)))
                        .build();
                savedFile = fileRepository.save(uploadedFile);
                return getResponseFile(savedFile);
            }
            return  UploadFileResponse.builder()
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .size(0)
                    .fileDownloadUri(null)
                    .message("uploaded file is empty")
                    .build();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    */
}
