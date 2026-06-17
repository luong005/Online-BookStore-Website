package com.BookShop_Backend.services.Impl;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.BookShop_Backend.DTO.Production.BookDTO;
import com.BookShop_Backend.DTO.Production.BookImportData;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class BookImportListener implements ReadListener<BookDTO> {
    private final List<BookImportData> rows = new ArrayList<>();
    private final Map<Integer, String> rowErrors = new LinkedHashMap<>();

    @Override
    public void invoke(BookDTO data, AnalysisContext context) {
        int rowNumber = context.readRowHolder().getRowIndex() + 1;
        rows.add(new BookImportData(rowNumber, data));
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        int rowNumber = context.readRowHolder() == null ? -1 : context.readRowHolder().getRowIndex() + 1;
        if (rowNumber > 0 && !rowErrors.containsKey(rowNumber)) {
            String message = exception == null ? null : exception.getMessage();
            rowErrors.put(rowNumber, normalizeImportErrorMessage(message));
        }
    }

    public List<BookImportData> getRows() {
        return rows;
    }

    public Map<Integer, String> getRowErrors() {
        return rowErrors;
    }

    public static String normalizeImportErrorMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "sai du lieu";
        }
        String normalized = message.trim().toLowerCase();
        if (normalized.contains("convert data")
                || normalized.contains("error")
                || normalized.contains("numberformat")
                || normalized.contains("parse")
                || normalized.contains("class java.lang.double")
                || normalized.contains("class java.lang.integer")) {
            return "sai du lieu";
        }
        return message;
    }
}
