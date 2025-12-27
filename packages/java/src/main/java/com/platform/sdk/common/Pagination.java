package com.platform.sdk.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Pagination metadata for list responses.
 */
public class Pagination {
    private int page;
    private int pageSize;
    private int totalItems;
    private int totalPages;
    private Boolean hasNext;
    private Boolean hasPrevious;

    public Pagination() {
    }

    public Pagination(int page, int pageSize, int totalItems, int totalPages) {
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    @JsonProperty("has_next")
    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    @JsonProperty("has_previous")
    public Boolean getHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
