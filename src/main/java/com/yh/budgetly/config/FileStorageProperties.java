package com.yh.budgetly.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
@AllArgsConstructor
@NoArgsConstructor
public class FileStorageProperties {

    @Value("${file.upload-dir}")
    private String uploadDir;
}
