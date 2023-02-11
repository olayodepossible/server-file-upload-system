package com.possible.fileupload.controller;

import com.possible.fileupload.dto.ResponseFile;
import com.possible.fileupload.model.DalimParam;
import com.possible.fileupload.model.Users;
import com.possible.fileupload.model.dto.UserDto;
import com.possible.fileupload.services.FileUploadService;
import com.possible.fileupload.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/files")
@Slf4j
public class FileUploadController {
    private final FileUploadService fileUploadService;
    private final UserService userService;
    @Value("${jdfInputFile}")
    private String jdfFile;

    @Value("${jdfInputFile1}")
    private String jdfFile1;

    @Value("${jdfInputFile2}")
    private String jdfFile2;

    @Autowired
    ServletContext context;

    public FileUploadController(FileUploadService fileUploadService, UserService userService) {
        this.fileUploadService = fileUploadService;
        this.userService = userService;
    }

    @PostMapping("/upload/local/{userId}")
    public ResponseEntity<ResponseFile> uploadToLocal(@RequestParam("file") MultipartFile multipartFile, @PathVariable Long userId) {
        ResponseFile message = fileUploadService.uploadFileToStorage(multipartFile, userId);
//       ResponseMessage responseMessage = ResponseMessage.builder().message(message).build();
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @PostMapping("/upload/db")
    public ResponseEntity<ResponseFile> uploadToDb(@RequestParam("file") MultipartFile multipartFile) {
        ResponseFile response = fileUploadService.uploadFileDb(multipartFile);
        if (response != null) return new ResponseEntity<>(response, HttpStatus.OK);
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

//    @GetMapping("/download/{id}")
//    public ResponseEntity<byte[]> downloadFile(@PathVariable String id){
//       UploadedFile uploadedFile = fileUploadService.downloadSavedFile(id);
//
//       return ResponseEntity.ok()
////               .contentType(MediaType.parseMediaType("application/octet-stream"))
//               .contentType(MediaType.parseMediaType("application/"+uploadedFile.getFileType()))
//               .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+uploadedFile.getFileName())
//               .body(uploadedFile.getFileData());
//    }

    @GetMapping("/download/{userId}/")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long userId, @RequestParam("fileName") String fileName) {
        Resource resource = fileUploadService.loadFileAsResource(userId, fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename())
                .body(resource);
    }

    @GetMapping()
    public ResponseEntity<List<ResponseFile>> getFiles() {
        return ResponseEntity.ok(fileUploadService.getSavedFiles());
    }

    @PostMapping("/user")
    public ResponseEntity<Users> saveUser(@RequestBody Users user) {
        return ResponseEntity.ok(userService.saveUser(user));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserDto> saveUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping("/jdf")
    public ResponseEntity<List<DalimParam>> getJdfFiles(@RequestBody String file) throws IOException {
        return ResponseEntity.ok(fileUploadService.readXmlFromLocalFile2(jdfFile));
//        return ResponseEntity.ok(fileUploadService.readXmlFromLocalFile(jdfFile2));
//        return ResponseEntity.ok(fileUploadService.jdfParser(jdfFile));
    }

}
