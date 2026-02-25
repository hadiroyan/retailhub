package org.hadiroyan.retailhub.utils;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.UUID;

import org.hadiroyan.retailhub.repository.StoreRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SlugUtil {

    @Inject
    StoreRepository storeRepository;

    public static String toSlug(String input) {
        if (input == null || input.isBlank())
            return "";

        String normalized = Normalizer.normalize(input, Form.NFD);
        return normalized
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "") // remove accents
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // keep alphanumeric, space, dash
                .trim()
                .replaceAll("\\s+", "-") // spaces → dash
                .replaceAll("-{2,}", "-"); // collapse multiple dashes
    }

    // Generate unique slug, append suffix number when slug is already use
    public String generateUniqueSlug(String name) {
        String baseSlug = toSlug(name);
        String candidate = baseSlug;
        int counter = 2;

        while (storeRepository.existsBySlug(candidate)) {
            candidate = baseSlug + "-" + counter++;
        }
        return candidate;
    }

    // Generate unique slug when update (exclude store itself)
    public String generateUniqueSlugForUpdate(String name, UUID excludeId) {
        String baseSlug = toSlug(name);
        String candidate = baseSlug;
        int counter = 2;

        while (storeRepository.existsBySlugAndIdNot(candidate, excludeId)) {
            candidate = baseSlug + "-" + counter++;
        }
        return candidate;
    }
}
