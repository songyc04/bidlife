package com.example.final_project.Service;

import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Repository.ItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Value("${file.upload-dir:uploads/items}")
    private String uploadDir;

    private String absoluteUploadDir;

    @PostConstruct
    public void init() {
        Path path = Paths.get(uploadDir).toAbsolutePath();
        absoluteUploadDir = path.toString();
        File dir = new File(absoluteUploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Transactional
    public ItemEntity saveItem(String title, String category, String description,
                               Integer startPrice, Integer buyNowPrice, Integer bidUnit,
                               LocalDateTime startTime, LocalDateTime endTime,
                               Long sellerId, MultipartFile[] images) throws IOException {

        ItemEntity item = new ItemEntity();
        item.setTitle(title);
        item.setCategory(category);
        item.setDescription(description);
        item.setStartPrice(startPrice);
        item.setBuyNowPrice(buyNowPrice);
        item.setBidUnit(bidUnit);
        item.setStartTime(startTime);
        item.setEndTime(endTime);
        item.setSellerId(sellerId);

        if (images != null && images.length > 0) {
            List<String> imagePaths = saveImages(images);
            item.setImagePaths(String.join(",", imagePaths));
        }

        item.updateTimeBasedStatus();

        return itemRepository.save(item);
    }

    private List<String> saveImages(MultipartFile[] images) throws IOException {
        List<String> paths = new ArrayList<>();

        for (MultipartFile image : images) {
            if (image.isEmpty()) continue;

            String originalName = image.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID().toString() + extension;
            File dest = new File(absoluteUploadDir, fileName);
            image.transferTo(dest);

            paths.add("/uploads/items/" + fileName);
        }

        return paths;
    }

    public List<ItemEntity> getAllItems() {
        try {
            List<ItemEntity> items = itemRepository.findAllByOrderByCreatedAtDesc();
            if (items == null) {
                return new ArrayList<>();
            }
            items.forEach(item -> {
                try {
                    item.updateTimeBasedStatus();
                } catch (Exception e) {
                    // Skip status update for problematic items
                }
            });
            return items;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<ItemEntity> getItemsBySeller(Long sellerId) {
        List<ItemEntity> items = itemRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
        items.forEach(ItemEntity::updateTimeBasedStatus);
        return items;
    }

    public ItemEntity getItemById(Long id) {
        ItemEntity item = itemRepository.findById(id).orElse(null);
        if (item != null) {
            item.updateTimeBasedStatus();
        }
        return item;
    }
}
