package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class PageResponse<T> {

    private List<T> records;
    private Long total;
    private Integer page;
    private Integer size;

    public static <T> PageResponse<T> of(List<T> records, Long total, Integer page, Integer size) {
        return PageResponse.<T>builder()
                .records(records)
                .total(total)
                .page(page)
                .size(size)
                .build();
    }
}