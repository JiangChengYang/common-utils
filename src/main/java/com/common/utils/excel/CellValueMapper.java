package com.common.utils.excel;

public interface CellValueMapper {
    Object map(String value) throws InvalidValueException;
}
