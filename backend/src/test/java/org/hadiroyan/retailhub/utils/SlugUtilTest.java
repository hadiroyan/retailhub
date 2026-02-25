package org.hadiroyan.retailhub.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.hadiroyan.retailhub.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SlugUtilTest {

    @Mock
    StoreRepository storeRepository;

    @InjectMocks
    SlugUtil slugUtil;

    // toSlug()
    @Test
    void should_convert_spaces_with_dash() {
        String result = SlugUtil.toSlug("Active Store");
        assertEquals("active-store", result);
    }

    @Test
    void should_remove_accent() {
        String result = SlugUtil.toSlug("Café");
        assertEquals("cafe", result);
    }

    // generateUniqueSlug()
    @Test
    void should_return_base_slug_if_not_exists() {
        Mockito.when(storeRepository.existsBySlug("store-a")).thenReturn(false);

        String result = slugUtil.generateUniqueSlug("Store A");
        assertEquals("store-a", result);
    }

    @Test
    void should_add_number_if_exists() {
        Mockito.when(storeRepository.existsBySlug("store-b")).thenReturn(true);
        Mockito.when(storeRepository.existsBySlug("store-b-2")).thenReturn(false);

        String result = slugUtil.generateUniqueSlug("Store B");
        assertEquals("store-b-2", result);
    }

    // generateUniqueSlugForUpdate()
    @Test
    void should_ignore_same_store() {
        UUID id = UUID.randomUUID();

        Mockito.when(storeRepository.existsBySlugAndIdNot("store-c", id)).thenReturn(false);

        String result = slugUtil.generateUniqueSlugForUpdate("Store C", id);
        assertEquals("store-c", result);
    }
}
