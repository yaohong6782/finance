package com.yh.budgetly.service;

import com.yh.budgetly.config.FileStorageProperties;
import com.yh.budgetly.constants.CommonVariables;
import com.yh.budgetly.constants.Utils;
import com.yh.budgetly.entity.Categories;
import com.yh.budgetly.entity.FileEntity;
import com.yh.budgetly.entity.Transaction;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.mapper.TransactionMapper;
import com.yh.budgetly.repository.CategoryRepository;
import com.yh.budgetly.repository.FileRepository;
import com.yh.budgetly.repository.TransactionRepository;
import com.yh.budgetly.repository.UserRepository;
import com.yh.budgetly.rest.dtos.TransactionDTO;
import com.yh.budgetly.rest.requests.TransactionFields;
import com.yh.budgetly.rest.requests.TransactionRequestFields;
import com.yh.budgetly.rest.requests.TransactionSearchFields;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Builder
@Service
@AllArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final TransactionMapper transactionMapper;

    private final FileStorageProperties uploadDir;

    private final SupabaseStorageService supabaseStorageService;

    public List<TransactionDTO> addTransaction(TransactionRequestFields transactionRequestFields,
                                               Map<String, MultipartFile> files) {
        log.info("Processing transactions to be added : {} ", transactionRequestFields);

        String username = transactionRequestFields.getUser();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        List<Transaction> transactionsList = new ArrayList<>();
        List<Categories> categoriesList = new ArrayList<>();


        File uploadFolder = new File(uploadDir.getUploadDir());
        if (!uploadFolder.exists()) {
            boolean created = uploadFolder.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create upload directory: " + uploadDir);
            }
        }

        for (int i = 0; i < transactionRequestFields.getTransactions().size(); i++) {

            TransactionFields transField = transactionRequestFields.getTransactions().get(i);

            Categories categories = categoryRepository.findByType(transField.getCategory().toLowerCase())
                    .orElseGet(() -> {
                        Categories newCategory = new Categories();
                        newCategory.setType(transField.getCategory());
                        categoriesList.add(newCategory);
                        return newCategory;
                    });

            if (!categoriesList.isEmpty()) {
                categoryRepository.saveAll(categoriesList);
                categoriesList.clear();
            }

            Transaction transaction = Transaction.builder()
                    .user(user)
                    .categories(categories)
                    .amount(transField.getAmount())
                    .transactionDate(LocalDate.parse(transField.getTransactionDate()))
                    .paymentMethod(transField.getPaymentMethod())
                    .description(transField.getDescription())
                    .build();

            transactionRepository.save(transaction);
            log.info("Transaction saved: {}", transaction);

            // Get file by "file{index}" key
            // Refer to Frontend for the key namings
            if (files != null) {
                log.info("file is not null : {} ", files);
                MultipartFile multipartFile = files.get("file" + i);
                log.info("multi part file : {}, {}", multipartFile.getOriginalFilename(), multipartFile.getName());
                if (!multipartFile.isEmpty()) {
                    FileEntity fileEntity = new FileEntity();
                    String sanitizedFileName = Utils.sanitizeFileName(Objects.requireNonNull(multipartFile.getOriginalFilename()));
                    String fileName = UUID.randomUUID() + "_" + sanitizedFileName;
//                    Path filePath = Paths.get(uploadDir.getUploadDir());
//                    Path filePathLocation = Paths.get(uploadDir.getUploadDir(), fileName);


                    fileEntity.setTransaction(transaction);
                    fileEntity.setUploadedAt(LocalDate.now());
                    fileEntity.setFileName(multipartFile.getOriginalFilename());
                    fileEntity.setBucketFileName(fileName);
                    fileEntity.setFileType(multipartFile.getContentType());

//                    fileEntity.setFilePath(filePath.toString());

                    try {
//                        Files.copy(multipartFile.getInputStream(), filePathLocation, StandardCopyOption.REPLACE_EXISTING);

                        String fileUrl = supabaseStorageService.uploadFile(multipartFile, fileName);
                        fileEntity.setFilePath(fileUrl);
                        fileRepository.save(fileEntity);

                        transaction.setFile(fileEntity);
                        transactionRepository.save(transaction);

                        log.info("File saved for transaction id {}: {}", transaction.getTransactionId(), fileEntity.getFileName());
                    } catch (IOException e) {
                        log.error("Failed to save file for transaction id {}", transaction.getTransactionId(), e);
                    }
                }
            }

            transactionsList.add(transaction);
        }
        List<TransactionDTO> transactionDTOS = transactionMapper.toTransactionDTOList(transactionsList);

        return transactionDTOS;
    }

    public List<TransactionDTO> transactionSummary(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        String userId = String.valueOf(user.getUserId());

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Transaction> transactionList = transactionRepository.findUserTransactionById(userId);
        log.info("transaction list : {} ", transactionList);

        List<TransactionDTO> transactionDTOList = transactionMapper.toTransactionDTOList(transactionList);

        return transactionDTOList;
    }

    public Page<TransactionDTO> transactionPageSummary(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        String userId = String.valueOf(user.getUserId());

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));

        Page<Transaction> transactionPage = transactionRepository.findUserTransactionByIdPagination(userId, pageRequest);
        log.info("transaction page : {} ", transactionPage);

        return transactionMapper.toTransactionDTOPage(transactionPage);
    }

    public Page<TransactionDTO> searchTransactionPageSummary(TransactionSearchFields transactionSearchFields, int page, int size) {
        log.info("username : {} ", transactionSearchFields.getUsername());

        String category = transactionSearchFields.getSearchFields().getCategory().isBlank() ?
                "" : transactionSearchFields.getSearchFields().getCategory();

        String amount = transactionSearchFields.getSearchFields().getAmount().isBlank() ?
                null : transactionSearchFields.getSearchFields().getAmount();

        User user = userRepository.findByUsername(transactionSearchFields.getUsername())
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        String userId = String.valueOf(user.getUserId());

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Transaction> transactionPage = transactionRepository.findUserTransactionSearchesByIdPagination(
                userId,
                category,
                amount != null ? Double.valueOf(amount) : null,
                pageRequest
        );
        log.info("transaction searched pages : {} ", transactionPage);

        return transactionMapper.toTransactionDTOPage(transactionPage);
    }
}
