package com.webank.weevent.broker.fisco.dto;


import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * list page result.
 *
 * @author matthewliu
 * @since 2019/02/11
 */
@Data
public class ListPage<T> {
    Integer total;
    Integer pageIndex;
    Integer pageSize;
    List<T> pageData = new ArrayList<>();
}
