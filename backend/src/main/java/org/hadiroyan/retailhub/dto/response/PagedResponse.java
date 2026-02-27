package org.hadiroyan.retailhub.dto.response;

import java.util.List;

public class PagedResponse<T> {

    public List<T> content;
    public int page;
    public int size;
    public long totalElements;
    public int totalPages;
    public boolean first;
    public boolean last;

    public PagedResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        this.first = page == 0;
        this.last = page >= this.totalPages - 1;
    }
}
