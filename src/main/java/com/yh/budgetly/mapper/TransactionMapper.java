package com.yh.budgetly.mapper;

import com.yh.budgetly.entity.FileEntity;
import com.yh.budgetly.entity.Transaction;
import com.yh.budgetly.rest.dtos.FileDTO;
import com.yh.budgetly.rest.dtos.TransactionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "transactionDate", target = "transactionDate")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "categories", target = "categoriesDTO")
    @Mapping(source = "paymentMethod", target="paymentMethod")
    @Mapping(source = "file", target="fileDTO")
    TransactionDTO toTransactionDTO(Transaction transaction);

    default FileDTO filetoFileDTO(FileEntity file) {
        if (file == null) {
            return null;
        }

        return FileDTO.builder()
                .fileName(file.getFileName())
                .uploadedAt(file.getUploadedAt())
                .filePath(file.getFilePath())
                .fileType(file.getFileType())
                .bucketFileName(file.getBucketFileName())
//                .fileUrl("http://localhost:8085/api/files/getReceipts/" + file.getFileId())
                .build();
    }

    default Page<TransactionDTO> toTransactionDTOPage(Page<Transaction> transactionPage) {
        return transactionPage.map(this::toTransactionDTO);
    }

    List<TransactionDTO> toTransactionDTOList(List<Transaction> transactions);


}
