package com.harshalkhade.signvault.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j

public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadPdf(byte[] fileBytes) throws Exception{
        Map uploadResult = cloudinary.uploader().upload(
                fileBytes, ObjectUtils.asMap(
                        "resource_type", "raw",
                        "folder", "signvault/contracts"
                )
        );
        return uploadResult.get("secure_url").toString();
    }

}
