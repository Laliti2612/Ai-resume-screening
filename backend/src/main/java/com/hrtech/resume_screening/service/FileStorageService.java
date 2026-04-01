package com.hrtech.resume_screening.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    public String storeFile(MultipartFile file){

        return file.getOriginalFilename();

    }

}
