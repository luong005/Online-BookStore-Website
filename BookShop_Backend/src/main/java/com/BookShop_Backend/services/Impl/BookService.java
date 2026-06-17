package com.BookShop_Backend.services.Impl;

import com.BookShop_Backend.DTO.Production.*;
import com.alibaba.excel.EasyExcel;
import com.BookShop_Backend.converter.MapStruct;
import com.BookShop_Backend.customexceptions.BusinessException;
import com.BookShop_Backend.models.AuthorEntity;
import com.BookShop_Backend.models.BookEntity;
import com.BookShop_Backend.models.CategoryEntity;
import com.BookShop_Backend.models.PublisherEntity;
import com.BookShop_Backend.repositories.AuthorRepository;
import com.BookShop_Backend.repositories.BookRepository;
import com.BookShop_Backend.repositories.CartItemRepository;
import com.BookShop_Backend.repositories.CategoryRepository;
import com.BookShop_Backend.repositories.PublisherRepository;
import com.BookShop_Backend.services.IBookService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class BookService implements IBookService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final CartItemRepository cartItemRepository;
    private final MapStruct mapStruct;
    private final Validator validator;

    @Override
    public List<BookDTO> searchBooks(String name, String category, Double minPrice, Double maxPrice) {
        List<BookEntity> books = bookRepository.searchBooks(name, category, minPrice, maxPrice);
        return books.stream()
                .map(mapStruct::toBook)
                .toList();
    }

    @Override
    public BookDTO searchBook(Long id) {
        Optional<BookEntity> bookEntity = bookRepository.findActiveById(id);
        if (bookEntity.isEmpty()) throw new BusinessException("BOOK_NOT_FOUND", "Khong tim thay sach", HttpStatus.NOT_FOUND);
        BookDTO book = mapStruct.toBook(bookEntity.get());
        return book;
    }

    @Override
    public void updateBook(Long id, UpdateBookRequestDTO updateBookRequestDTO) {
        Optional<BookEntity> bookEntity = bookRepository.findActiveById(id);
        if(bookEntity.isEmpty()) throw new BusinessException("BOOK_NOT_FOUND", "Khong tim thay sach", HttpStatus.NOT_FOUND);
        BookEntity book = bookEntity.get();
        book.setContent(updateBookRequestDTO.getContent());
        book.setPrice(updateBookRequestDTO.getPrice());
        book.setStock(updateBookRequestDTO.getStock());
        book.setImageUrl(updateBookRequestDTO.getImageURL());
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Optional<BookEntity> bookEntity = bookRepository.findActiveById(id);
        if(bookEntity.isEmpty()) throw new BusinessException("BOOK_NOT_FOUND", "Sach khong ton tai", HttpStatus.NOT_FOUND);
        cartItemRepository.deleteAllByBook_Id(id);
        BookEntity book = bookEntity.get();
        book.setStatus(0);
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public DeleteBooksResponseDTO deleteBooks(List<Long> ids) {
        List<Long> deletedIds = new ArrayList<>();
        List<Long> notFoundIds = new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            return new DeleteBooksResponseDTO(deletedIds, notFoundIds);
        }

        for (Long id : ids) {
            if (id == null || id <= 0) {
                notFoundIds.add(id);
                continue;
            }
            Optional<BookEntity> bookEntity = bookRepository.findActiveById(id);
            if (bookEntity.isEmpty()) {
                notFoundIds.add(id);
                continue;
            }
            cartItemRepository.deleteAllByBook_Id(id);
            BookEntity book = bookEntity.get();
            book.setStatus(0);
            bookRepository.save(book);
            deletedIds.add(id);
        }
        return new DeleteBooksResponseDTO(deletedIds, notFoundIds);
    }

    @Override
    @Transactional
    public ImportBooksResponseDTO insertBooks(MultipartFile file) {
        // 1) Validate input file upload
        if (file == null || file.isEmpty()) {
            throw new BusinessException("INVALID_FILE", "File excel khong hop le", HttpStatus.BAD_REQUEST);
        }

        // 2) Mo workbook de:
        // - Lay sheet goc (de doc lai du lieu raw khi can ghi file loi)
        // - Lay so cot dong header (de tao dong loi dong bo, khong hard-code cot)
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException("INVALID_FILE", "Khong tim thay sheet du lieu", HttpStatus.BAD_REQUEST);
            }

            // Formatter dung de doc gia tri cell dang chuoi khi tao file loi
            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            int totalColumns = getTotalColumnsFromHeader(headerRow);

            // 3) Dung EasyExcel map tung dong vao BookDTO theo @ExcelProperty
            // Neu dong nao convert/map that bai, listener se ghi lai rowErrors
            BookImportListener listener = new BookImportListener();
            EasyExcel.read(file.getInputStream(), BookDTO.class, listener)
                    .sheet()
                    .headRowNumber(1)
                    .doRead();

            // 4) Bien dem ket qua import
            int inserted = 0;
            int failed = 0;
            List<String[]> errorRows = new ArrayList<>();

            // 5) Gom cac dong loi map/convert tu EasyExcel vao danh sach errorRows
            Map<Integer, String> rowErrors = listener.getRowErrors();
            for (Map.Entry<Integer, String> entry : rowErrors.entrySet()) {
                failed++;
                Row sourceRow = sheet.getRow(entry.getKey() - 1);
                errorRows.add(buildErrorRow(entry.getKey(), sourceRow, formatter, entry.getValue(), totalColumns));
            }

            // 6) Duyet cac dong map thanh cong de validate + insert
            for (BookImportData rowData : listener.getRows()) {
                int rowNumber = rowData.getRowNumber();
                // Neu dong nay da loi o buoc map/convert thi bo qua
                if (rowErrors.containsKey(rowNumber)) {
                    continue;
                }
                try {
                    BookDTO dto = rowData.getBookDTO();
                    // Chuan hoa chuoi truoc khi validate/insert
                    normalizeDto(dto);
                    // Validate annotation trong BookDTO (@NotBlank/@NotNull/@Size)
                    validateBookDto(dto);

                    // Insert 1 dong hop le
                    insertBook(dto);
                    inserted++;
                } catch (Exception ex) {
                    // 7) Neu dong nay loi nghiep vu/validate -> ghi vao file loi va tiep tuc dong sau
                    failed++;
                    Row sourceRow = sheet.getRow(rowNumber - 1);
                    errorRows.add(buildErrorRow(rowNumber, sourceRow, formatter, BookImportListener.normalizeImportErrorMessage(ex.getMessage()), totalColumns));
                }
            }

            // 8) Neu co loi thi phat sinh file excel loi, neu khong thi de null
            String errorFilePath = null;
            if (!errorRows.isEmpty()) {
                errorFilePath = writeErrorFile(errorRows);
            }

            // 9) Tra ket qua tong hop import
            return new ImportBooksResponseDTO(inserted, failed, errorFilePath);
        } catch (IOException e) {
            // Loi doc file stream/workbook
            throw new BusinessException("INVALID_FILE", "Khong the doc file excel", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void insertBook(BookDTO bookDTO) {
        if(bookRepository.existsActiveByName(bookDTO.getName())){
            throw new BusinessException("INVALID_FILE", "Sach da ton tai", HttpStatus.BAD_REQUEST);
        }
        AuthorEntity author = authorRepository.findByNameIgnoreCase(bookDTO.getAuthorName().trim())
                .orElseGet(() -> authorRepository.save(AuthorEntity.builder().name(bookDTO.getAuthorName().trim()).build()));
        CategoryEntity category = categoryRepository.findByNameIgnoreCase(bookDTO.getCategoryName().trim())
                .orElseGet(() -> categoryRepository.save(CategoryEntity.builder().name(bookDTO.getCategoryName().trim()).build()));
        PublisherEntity publisher = publisherRepository.findByNameIgnoreCase(bookDTO.getPublisher().trim())
                .orElseGet(() -> publisherRepository.save(PublisherEntity.builder().name(bookDTO.getPublisher().trim()).build()));

        BookEntity book = mapStruct.toBookEntity(bookDTO);
        book.setAuthor(author);
        book.setCategory(category);
        book.setPublisher(publisher);
        book.setStatus(1);
        bookRepository.save(book);
    }

    private String readCell(Row row, int index, DataFormatter formatter) {
        // Doc 1 o theo index; neu o rong thi tra ve null de xu ly thong nhat
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        // Chuyen moi kieu cell (number/date/string/...) ve String
        String value = formatter.formatCellValue(cell);
        return value == null ? null : value.trim();
    }

    private String safeCell(Row row, int index, DataFormatter formatter) {
        // Dung khi ghi file loi: khong de null, luon tra ve chuoi
        if (row == null) {
            return "";
        }
        String value = readCell(row, index, formatter);
        return value == null ? "" : value;
    }

    private String writeErrorFile(List<String[]> rows) throws IOException {
        // Tao thu muc luu file loi neu chua ton tai
        Path dir = Paths.get("import-errors");
        Files.createDirectories(dir);
        // Dat ten file theo timestamp de tranh trung ten
        String fileName = "books_import_errors_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        Path path = dir.resolve(fileName);

        try (Workbook workbook = new XSSFWorkbook(); OutputStream os = Files.newOutputStream(path)) {
            Sheet sheet = workbook.createSheet("errors");
            String[] header = {"rowNumber", "imageUrl", "price", "stock", "name", "content", "authorName", "categoryName", "publisher", "reason"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < header.length; i++) {
                headerRow.createCell(i).setCellValue(header[i]);
            }

            int rowIdx = 1;
            for (String[] data : rows) {
                Row row = sheet.createRow(rowIdx++);
                for (int i = 0; i < data.length; i++) {
                    // Ghi tung cot cua dong loi; null se duoc doi thanh chuoi rong
                    row.createCell(i).setCellValue(data[i] == null ? "" : data[i]);
                }
            }
            workbook.write(os);
        }
        return path.toAbsolutePath().toString();
    }

    private void validateBookDto(BookDTO dto) {
        // Validate theo annotation trong BookDTO (@NotBlank/@NotNull/@Size...)
        Set<ConstraintViolation<BookDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            // Theo yeu cau hien tai: gom ve 1 thong diep chung cho cot rong/khong hop le
            throw new BusinessException("INVALID_FILE", "Co cot du lieu bi trong", HttpStatus.BAD_REQUEST);
        }
    }

    private void normalizeDto(BookDTO dto) {
        // Chuan hoa chuoi: trim khoang trang dau/cuoi truoc khi validate va save
        dto.setImageUrl(trimOrNull(dto.getImageUrl()));
        dto.setName(trimOrNull(dto.getName()));
        dto.setContent(trimOrNull(dto.getContent()));
        dto.setAuthorName(trimOrNull(dto.getAuthorName()));
        dto.setCategoryName(trimOrNull(dto.getCategoryName()));
        dto.setPublisher(trimOrNull(dto.getPublisher()));
    }

    private String trimOrNull(String value) {
        // Helper chung: giu null, con chuoi thi trim
        return value == null ? null : value.trim();
    }

    private String[] buildErrorRow(int rowNumber, Row row, DataFormatter formatter, String reason, int totalColumns) {
        // Cau truc dong loi: [rowNumber] + [du lieu goc cac cot] + [reason]
        String[] data = new String[totalColumns + 2];
        data[0] = String.valueOf(rowNumber);
        for (int i = 0; i < totalColumns; i++) {
            data[i + 1] = safeCell(row, i, formatter);
        }
        data[data.length - 1] = reason == null ? "Du lieu khong hop le" : reason;
        return data;
    }

    private int getTotalColumnsFromHeader(Row headerRow) {
        // Lay tong so cot thuc te tu header de sinh dong loi dong bo, khong hard-code
        if (headerRow == null || headerRow.getLastCellNum() <= 0) {
            throw new BusinessException("INVALID_FILE", "File trong", HttpStatus.BAD_REQUEST);
        }
        return headerRow.getLastCellNum();
    }
}
