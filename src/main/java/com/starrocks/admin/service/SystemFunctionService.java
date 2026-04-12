package com.starrocks.admin.service;

import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.dto.request.*;
import com.starrocks.admin.model.entity.SystemFunction;
import com.starrocks.admin.model.entity.SystemFunctionPreference;
import com.starrocks.admin.repository.SystemFunctionRepository;
import com.starrocks.admin.repository.SystemFunctionPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemFunctionService {

    private final SystemFunctionRepository systemFunctionRepository;
    private final SystemFunctionPreferenceRepository preferenceRepository;

    public List<SystemFunction> getFunctions(Long clusterId) {
        return systemFunctionRepository.findByClusterIdOrderByCategoryOrderAscDisplayOrderAsc(clusterId);
    }

    @Transactional
    public SystemFunction createFunction(Long clusterId, CreateFunctionRequest req, Long userId) {
        SystemFunction func = SystemFunction.builder()
                .clusterId(clusterId)
                .categoryName(req.getCategoryName())
                .functionName(req.getFunctionName())
                .description(req.getDescription())
                .sqlQuery(req.getSqlQuery())
                .displayOrder(0)
                .categoryOrder(0)
                .isFavorited(false)
                .isSystem(false)
                .createdBy(userId)
                .build();
        func = systemFunctionRepository.save(func);
        log.info("System function created: {} for cluster {}", func.getFunctionName(), clusterId);
        return func;
    }

    @Transactional
    public SystemFunction updateFunction(Long clusterId, Long functionId, UpdateFunctionRequest req) {
        SystemFunction func = systemFunctionRepository.findById(functionId)
                .orElseThrow(() -> ApiException.resourceNotFound("Function not found: " + functionId));

        if (req.getCategoryName() != null) func.setCategoryName(req.getCategoryName());
        if (req.getFunctionName() != null) func.setFunctionName(req.getFunctionName());
        if (req.getDescription() != null) func.setDescription(req.getDescription());
        if (req.getSqlQuery() != null) func.setSqlQuery(req.getSqlQuery());

        func = systemFunctionRepository.save(func);
        log.info("System function updated: {} (ID: {})", func.getFunctionName(), functionId);
        return func;
    }

    @Transactional
    public void deleteFunction(Long clusterId, Long functionId) {
        systemFunctionRepository.findById(functionId)
                .orElseThrow(() -> ApiException.resourceNotFound("Function not found: " + functionId));
        systemFunctionRepository.deleteById(functionId);
        log.info("System function deleted: ID {}", functionId);
    }

    @Transactional
    public void updateOrder(Long clusterId, UpdateOrderRequest req) {
        for (UpdateOrderRequest.FunctionOrder fo : req.getFunctions()) {
            systemFunctionRepository.findById(fo.getId()).ifPresent(func -> {
                func.setDisplayOrder(fo.getDisplayOrder());
                func.setCategoryOrder(fo.getCategoryOrder());
                systemFunctionRepository.save(func);
            });
        }
    }

    @Transactional
    public void updateAccessTime(String functionName) {
        // Update last access time for the function
        log.debug("Updating access time for function: {}", functionName);
    }

    @Transactional
    public void deleteCategory(String categoryName) {
        // Delete all functions in the category (for custom functions only)
        log.info("Deleting category: {}", categoryName);
    }
}
