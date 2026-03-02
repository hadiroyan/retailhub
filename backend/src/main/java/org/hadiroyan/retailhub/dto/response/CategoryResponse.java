package org.hadiroyan.retailhub.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {

    public UUID id;
    public String name;
    public String slug;
    public String description;
    public String imageUrl;
    public long productCount;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public ParentInfo parent;

    public List<ChildInfo> children;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ParentInfo {
        public UUID id;
        public String name;
        public String slug;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChildInfo {
        public UUID id;
        public String name;
        public String slug;
        public long productCount;
    }
}