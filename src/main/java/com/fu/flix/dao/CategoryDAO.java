package com.fu.flix.dao;

import com.fu.flix.dto.ICategoryDTO;
import com.fu.flix.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryDAO extends JpaRepository<Category, Long> {

    @Query(value = "SELECT c.id as id, c.name as categoryName, icon.url as icon, image.url as image, " +
            "CASE WHEN c.is_active THEN 'ACTIVE' ELSE 'INACTIVE' END as status, " +
            "c.description " +
            "FROM categories c " +
            "JOIN images icon " +
            "ON c.icon_id = icon.id " +
            "JOIN images image " +
            "ON c.image_id = image.id " +
            "WHERE c.name LIKE %:keyword% " +
            "ORDER BY c.id DESC", nativeQuery = true)
    List<ICategoryDTO> searchCategories(String keyword);

    Page<Category> findAllByOrderByIdDesc(Pageable pageable);
}
