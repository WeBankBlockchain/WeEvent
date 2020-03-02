package com.webank.weevent.core.dto;


import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * list page result.
 *
 * @author matthewliu
 * @since 2019/02/11
 */
@Getter
@Setter
public class ListPage<T> {
    private Integer total;
    private Integer pageIndex;
    private Integer pageSize;
    private List<T> pageData = new ArrayList<>();
}
