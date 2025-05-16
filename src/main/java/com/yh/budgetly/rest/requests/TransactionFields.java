package com.yh.budgetly.rest.requests;

import com.yh.budgetly.entity.Files;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFields {
    private BigDecimal amount;
    private String category;
    private String transactionDate;
    private String description;
    private String paymentMethod;
    private Integer fileIndex;
    private MultipartFile file;
}
