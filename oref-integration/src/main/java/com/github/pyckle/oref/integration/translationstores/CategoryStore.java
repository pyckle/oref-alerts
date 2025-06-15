package com.github.pyckle.oref.integration.translationstores;

import com.github.pyckle.oref.integration.dto.Category;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategoryStore {
    public static int INVALID_ID = Integer.MIN_VALUE;
    private final List<Category> categoryList;
    private final Map<Integer, Category> catIdToCategory;
    private final Map<Integer, Category> matIdToCategory;
    private final Set<Integer> flashAndUpdateCatIds = new HashSet<>();
    private final Set<Integer> flashAndUpdateMatIds = new HashSet<>();

    public CategoryStore(List<Category> categoryList) {
        this.categoryList = categoryList;
        catIdToCategory = new HashMap<>();
        matIdToCategory = new HashMap<>();
        for (Category category : categoryList) {
            catIdToCategory.put(category.id(), category);
            matIdToCategory.put(category.matrix_id(), category);
            if (category.category() != null
                    && (category.category().contains("update") || category.category().contains("flash"))) {
                flashAndUpdateCatIds.add(category.id());
                flashAndUpdateMatIds.add(category.matrix_id());
            }
        }
    }

    public int categoryToMatrixId(int catId) {
        Category c = catIdToCategory.get(catId);
        if (c == null)
            return INVALID_ID;
        return c.matrix_id();
    }

    public boolean isFlashOrUpdate(int catId) {
        return flashAndUpdateCatIds.contains(catId);
    }

    public boolean isFlashOrUpdate(String matrixId) {
        try {
            return flashAndUpdateMatIds.contains(Integer.parseInt(matrixId));
        }
        catch (NumberFormatException ex) {
            return false;
        }
    }
}
