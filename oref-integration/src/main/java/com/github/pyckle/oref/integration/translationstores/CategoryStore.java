package com.github.pyckle.oref.integration.translationstores;

import com.github.pyckle.oref.integration.dto.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryStore {
    public static int INVALID_ID = Integer.MIN_VALUE;
    private final List<Category> categoryList;
    private final Map<Integer, Category> catIdToCategory;
    private final Map<Integer, Category> matIdToCategory;

    public CategoryStore(List<Category> categoryList) {
        this.categoryList = categoryList;
        catIdToCategory = new HashMap<>();
        matIdToCategory = new HashMap<>();
        for (Category category : categoryList) {
            catIdToCategory.put(category.id(), category);
            matIdToCategory.put(category.matrix_id(), category);
        }
    }

    public int categoryToMatrixId(int catId) {
        Category c = catIdToCategory.get(catId);
        if (c == null)
            return INVALID_ID;
        return c.matrix_id();
    }
}
