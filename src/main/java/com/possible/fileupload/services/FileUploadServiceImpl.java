package com.possible.fileupload.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.possible.fileupload.dto.ResponseFile;
import com.possible.fileupload.exceptions.FileNotFoundException;
import com.possible.fileupload.exceptions.UserNotFoundException;
import com.possible.fileupload.model.DalimParam;
import com.possible.fileupload.model.UploadedFile;
import com.possible.fileupload.model.Users;
import com.possible.fileupload.repository.UploadedFileRepository;
import com.possible.fileupload.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private ResourceLoader resourceLoader;

    private final String UPLOAD_PATH = "C:\\Users\\olayo\\OneDrive\\Documents\\new_upload";

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
        File fileWrite = new File(UPLOAD_PATH, fileName);
        try {

            byte[] fileData = file.getBytes();
//            Path path = Paths.get(UPLOAD_PATH + file.getOriginalFilename());
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
        Path path = Paths.get(UPLOAD_PATH+"\\"+fileName);
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
       for (UploadedFile f : userService.getUser(userId).getUploadedFiles()){
           String storedName = f.getFileName();
           if (storedName.split("_")[0].equalsIgnoreCase(fileName)){
               try {
                   Path filePath = Paths.get(UPLOAD_PATH).resolve(storedName);
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

    @Override //587ms
    public List<DalimParam> jdfParser(String file) {
        List<DalimParam> dalimParamList = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ClassPathResource(file).getInputStream());
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("Notification");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element nodeElement = (Element) node;

                    if (nodeElement.getAttribute("Class").equalsIgnoreCase("Information")){
                       String  nodeContent = nodeElement.getTextContent().split("!!!")[1];
                        StringBuilder stringBuilder = new StringBuilder(nodeContent);
                        stringBuilder.deleteCharAt(0);
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

                        ObjectMapper mapper = new ObjectMapper();
                        DalimParam dalimParam = mapper.readValue(stringBuilder.toString(), DalimParam.class);
                        dalimParamList.add(dalimParam);

                    }
                }

            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Error: \n{}", e.getMessage());
            e.printStackTrace();
        }

        return dalimParamList;
    }


    @Override //436ms
    public List<DalimParam> readXmlFromLocalFile(String filePath) throws IOException {
        List<DalimParam> dalimParamList = new ArrayList<>();
        Resource resource = resourceLoader.getResource(filePath);
        File file = resource.getFile();
        try ( BufferedReader reader = new BufferedReader(new FileReader(file))){

            String line;
            while ((line = reader.readLine()) !=null){
                if (line.contains("!!!")) {
                    StringBuilder sb = new StringBuilder();
                    String lineSplit = line.split("!!!")[1];
                    sb.append(lineSplit);
                    sb.deleteCharAt(0);
                    sb.deleteCharAt(sb.length() - 1);
                    ObjectMapper mapper = new ObjectMapper();
                    DalimParam dalimParam = mapper.readValue(sb.toString(), DalimParam.class);
                    dalimParamList.add(dalimParam);
                    break;
                }

            }
        }

        return dalimParamList;
    }

    @Override // 818ms
    public List<DalimParam> readXmlFromLocalFile2(String filePath) {
        List<DalimParam> dalimParamList = new ArrayList<>();

        try {
            byte[] xmlFile = Files.readAllBytes(Paths.get(filePath));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlFile));
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("Notification");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element nodeElement = (Element) node;

                    if (nodeElement.getAttribute("Class").equalsIgnoreCase("Information")){
                       String  nodeContent = nodeElement.getTextContent().split("!!!")[1];
                        StringBuilder stringBuilder = new StringBuilder(nodeContent);
                        stringBuilder.deleteCharAt(0);
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

                        ObjectMapper mapper = new ObjectMapper();
                        DalimParam dalimParam = mapper.readValue(stringBuilder.toString(), DalimParam.class);
                        dalimParamList.add(dalimParam);

                    }
                }

            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Error: \n{}", e.getMessage());
            e.printStackTrace();
        }

        return dalimParamList;
    }

}
