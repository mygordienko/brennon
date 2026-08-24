package com.mygordienko.brennon.ticket.adapter.web;

public record PageInfoDto(
    /**
     * Current page index
     */
    int currentPage,
    /**
     * Actual records on this specific page
     */
    int numberOfElementsOnCurrentPage,
    int totalPages,
    /**
     * Configured/Requested page size
     */
    int pageSize,

    /**
     * Sorting information.
     */
    OrderDto order
) {

    public static record OrderDto(
        String property,
        String direction
    ) {
    }
}
