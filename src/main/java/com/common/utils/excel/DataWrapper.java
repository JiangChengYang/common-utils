package com.common.utils.excel;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DataWrapper<T> {
    private int rowIndex;
    private T data;
}
