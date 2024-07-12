package cloud.igibgo.igibgobackend.controller;

import cloud.igibgo.igibgobackend.entity.APIResponse;
import cloud.igibgo.igibgobackend.entity.Collection;
import cloud.igibgo.igibgobackend.entity.ResponseCodes;
import cloud.igibgo.igibgobackend.service.CollectionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/collection")
public class CollectionController {
    @Resource
    private CollectionService collectionService;

    @PostMapping("/add")
    APIResponse<Void> addCollection(Long userId, String collectionName){
        try {
            collectionService.addCollection(userId, collectionName);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }
    @GetMapping("/get/userId")
    APIResponse<List<Collection>> getCollectionsByUser(Long userId){
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, collectionService.getCollectionsByUser(userId));
        } catch (DataAccessException e) {
            log.error("Database query error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

}
