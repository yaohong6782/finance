package com.yh.budgetly.repository;

import com.yh.budgetly.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> getFileByFileId(Long id);

    Optional<FileEntity> getFileByBucketFileName(String fileName);

}
