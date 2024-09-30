package cloud.igibgo.igibgobackend.service;

import cloud.igibgo.igibgobackend.entity.FUser.Collection;
import cloud.igibgo.igibgobackend.entity.FUser.FUser;
import cloud.igibgo.igibgobackend.mapper.CollectionMapper;
import cloud.igibgo.igibgobackend.mapper.FUserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CollectionService {
    @Resource
    private CollectionMapper collectionMapper;

    @Resource
    private FUserMapper fUserMapper;

    @Transactional
    public void addCollection(Long userId, String collectionName){
        // Check 1: if the author exists
        Optional<FUser> fuserOptional = fUserMapper.findById(userId);
        if (fuserOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        Collection collection = new Collection();
        collection.collectionName = collectionName;
        collection.fUser = fuserOptional.get();
        collectionMapper.save(collection);
    }


    public List<Collection> getCollectionsByUser(Long userId) {
        // Check 1: if the author exists
        Optional<FUser> userOptional = fUserMapper.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        return collectionMapper.findAllByUser(userOptional.get().userId);
    }
}
