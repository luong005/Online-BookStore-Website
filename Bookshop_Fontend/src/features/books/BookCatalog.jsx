import { useEffect, useMemo, useState } from "react";
import { EmptyState } from "../../shared/components/EmptyState";
import { BookCard } from "./BookCard";
import { BookDetailDrawer } from "./BookDetailDrawer";
import { BookFilters } from "./BookFilters";

const booksPerPage = 10;

export function BookCatalog({
  books,
  filters,
  categories,
  loading,
  searchOpen,
  selectedBook,
  onToggleSearch,
  onFilterChange,
  onSearch,
  onResetFilters,
  onViewDetail,
  onCloseDetail,
  onAddToCart,
}) {
  const [currentPage, setCurrentPage] = useState(1);
  const totalPages = Math.max(1, Math.ceil(books.length / booksPerPage));
  const pagedBooks = useMemo(() => {
    const startIndex = (currentPage - 1) * booksPerPage;
    return books.slice(startIndex, startIndex + booksPerPage);
  }, [books, currentPage]);

  useEffect(() => {
    setCurrentPage(1);
  }, [books]);

  function goToPage(page) {
    setCurrentPage(Math.min(Math.max(1, page), totalPages));
  }

  return (
    <section className="shop-page">
      <div className="section-heading clean-heading">
        <h1>Danh sách sách</h1>
        <button className="icon-btn" type="button" aria-label="Tìm kiếm" onClick={onToggleSearch}>
          <span className="search-icon" aria-hidden="true" />
        </button>
      </div>

      {searchOpen ? (
        <div className="modal-backdrop" role="presentation" onMouseDown={onToggleSearch}>
          <div className="modal-card search-modal" onMouseDown={(event) => event.stopPropagation()}>
            <button className="close-btn" type="button" onClick={onToggleSearch}>
              Đóng
            </button>
            <BookFilters
              filters={filters}
              categories={categories}
              onChange={onFilterChange}
              onSubmit={onSearch}
              onReset={onResetFilters}
            />
          </div>
        </div>
      ) : null}

      <div className="book-grid">
        {pagedBooks.map((book) => (
          <BookCard
            key={book.id || book.name}
            book={book}
            onAddToCart={onAddToCart}
            onViewDetail={onViewDetail}
          />
        ))}
      </div>

      {books.length > booksPerPage ? (
        <div className="pagination-bar" aria-label="Phân trang sách">
          <button className="ghost-btn" type="button" disabled={currentPage === 1} onClick={() => goToPage(currentPage - 1)}>
            Trước
          </button>
          <span>
            Trang {currentPage} / {totalPages}
          </span>
          <button className="ghost-btn" type="button" disabled={currentPage === totalPages} onClick={() => goToPage(currentPage + 1)}>
            Sau
          </button>
        </div>
      ) : null}

      {!loading && books.length === 0 ? <EmptyState>Chưa có sách phù hợp.</EmptyState> : null}

      <BookDetailDrawer book={selectedBook} onClose={onCloseDetail} onAddToCart={onAddToCart} />
    </section>
  );
}
