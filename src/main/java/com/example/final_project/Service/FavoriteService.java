package com.example.final_project.Service;

import com.example.final_project.Entity.FavoriteEntity;
import com.example.final_project.Repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    @Transactional
    public boolean toggleFavorite(Long userId, Long itemId) {
        return favoriteRepository.findByUserIdAndItemId(userId, itemId)
                .map(favorite -> {
                    favoriteRepository.delete(favorite);
                    return false;
                })
                .orElseGet(() -> {
                    FavoriteEntity favorite = new FavoriteEntity();
                    favorite.setUserId(userId);
                    favorite.setItemId(itemId);
                    favoriteRepository.save(favorite);
                    return true;
                });
    }

    public boolean isFavorite(Long userId, Long itemId) {
        if (userId == null) {
            return false;
        }
        return favoriteRepository.existsByUserIdAndItemId(userId, itemId);
    }

    public List<FavoriteEntity> getFavoritesByUserId(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void removeFavorite(Long userId, Long itemId) {
        favoriteRepository.deleteByUserIdAndItemId(userId, itemId);
    }
}
