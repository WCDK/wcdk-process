package com.wcdk.proces.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private Long total;

    private Long pageNum;

    private Long pageSize;

    private List<T> records;
}
