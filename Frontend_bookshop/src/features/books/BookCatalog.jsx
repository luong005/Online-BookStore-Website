import { useEffect, useMemo, useState } from "react";
import { EmptyState } from "../../shared/components/EmptyState";
import { formatMoney } from "../../shared/utils/formatters";
import { BookCard } from "./BookCard";
import { BookCategories } from "./BookCategories";
import { BookDetailDrawer } from "./BookDetailDrawer";
import { BookFilters } from "./BookFilters";

const booksPerPage = 12;

export function BookCatalog({
  books,
  recommendedBestSellers,
  loadingRecommendations,
  recommendationsError,
  filters,
  appliedFilters,
  categories,
  loading,
  searchOpen,
  selectedBook,
  onToggleSearch,
  onFilterChange,
  onSelectCategory,
  onSearch,
  onResetFilters,
  onViewDetail,
  onCloseDetail,
  onAddToCart,
}) {
  const [currentPage, setCurrentPage] = useState(1);
  const hasFilters = Object.values(appliedFilters).some((value) => String(value).trim());
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
      <div className="section-heading clean-heading catalog-heading">
        <div>
          <h1>Danh sách sách</h1>
          <p className="muted-text">Tìm cuốn sách phù hợp với bạn, bắt đầu từ một chủ đề yêu thích.</p>
        </div>
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
              loading={loading}
              onChange={onFilterChange}
              onSubmit={onSearch}
              onReset={onResetFilters}
            />
          </div>
        </div>
      ) : null}

      <div className="catalog-layout">
        <BookCategories
          categories={categories}
          selectedCategory={appliedFilters.category}
          onSelect={onSelectCategory}
        />

        <div className="catalog-content">
          <form className="catalog-search" role="search" aria-label="Tìm kiếm sách" onSubmit={onSearch}>
            <label className="catalog-search-field">
              <span className="search-icon" aria-hidden="true" />
              <input
                type="search"
                aria-label="Tên sách"
                value={filters.name}
                placeholder={appliedFilters.category ? `Tìm sách trong ${appliedFilters.category}...` : "Bạn muốn tìm cuốn sách nào?"}
                onChange={(event) => onFilterChange({ ...filters, name: event.target.value })}
              />
            </label>
            <button className="primary-btn" type="submit" disabled={loading}>Tìm kiếm</button>
            <button className="ghost-btn" type="button" onClick={onToggleSearch}>Bộ lọc</button>
          </form>

          <section className="catalog-recommendations" aria-labelledby="recommendations-heading" aria-busy={loadingRecommendations}>
            <div className="catalog-recommendations-heading">
              <div>
                <span className="catalog-recommendations-eyebrow">Gợi ý cho bạn</span>
                <h2 id="recommendations-heading">Sách bán chạy nhất</h2>
                <p className="muted-text">Những cuốn sách được mua nhiều nhất tại cửa hàng.</p>
              </div>
            </div>
            {loadingRecommendations ? (
              <p className="muted-text" role="status">Đang tải sách bán chạy...</p>
            ) : recommendationsError ? (
              <p className="muted-text" role="status">Chưa tải được sách bán chạy.</p>
            ) : recommendedBestSellers.length ? (
              <div className="book-grid">
                {recommendedBestSellers.map((book, index) => (
                  <div className="recommended-book" key={book.id}>
                    <span className="recommended-book-rank">#{index + 1} bán chạy</span>
                    <BookCard book={book} onAddToCart={onAddToCart} onViewDetail={onViewDetail} />
                  </div>
                ))}
              </div>
            ) : (
              <p className="muted-text">Chưa có sách bán chạy để đề xuất.</p>
            )}
          </section>

          <div className="catalog-results-heading">
            <div>
              <h2>{appliedFilters.category || "Tất cả sách"}</h2>
              <p className="muted-text" role="status">
                {loading ? "Đang tìm sách..." : `${books.length} cuốn sách${hasFilters ? " phù hợp" : " đang có tại cửa hàng"}`}
              </p>
            </div>
            {hasFilters ? (
              <button className="text-btn" type="button" onClick={onResetFilters}>Xóa bộ lọc</button>
            ) : null}
          </div>

          {appliedFilters.name || appliedFilters.minPrice || appliedFilters.maxPrice ? (
            <div className="catalog-filter-summary" aria-label="Bộ lọc đang áp dụng">
              {appliedFilters.name ? <span>Tên sách: {appliedFilters.name}</span> : null}
              {appliedFilters.minPrice ? <span>Giá từ: {formatMoney(appliedFilters.minPrice)}</span> : null}
              {appliedFilters.maxPrice ? <span>Giá đến: {formatMoney(appliedFilters.maxPrice)}</span> : null}
            </div>
          ) : null}

          <div id="catalog-results" aria-busy={loading}>
            {loading ? (
              <div className="catalog-loading">
                <span className="catalog-spinner" aria-hidden="true" />
                Đang tải danh sách sách...
              </div>
            ) : books.length ? (
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
            ) : (
              <EmptyState>
                <p>Chưa có sách phù hợp. Hãy thử danh mục hoặc từ khóa khác.</p>
                {hasFilters ? <button className="ghost-btn" type="button" onClick={onResetFilters}>Xem tất cả sách</button> : null}
              </EmptyState>
            )}
          </div>

          {!loading && books.length > booksPerPage ? (
            <div className="pagination-bar" aria-label="Phân trang sách">
              <button className="ghost-btn" type="button" disabled={currentPage === 1} onClick={() => goToPage(currentPage - 1)}>
                Trước
              </button>
              <span>Trang {currentPage} / {totalPages}</span>
              <button className="ghost-btn" type="button" disabled={currentPage === totalPages} onClick={() => goToPage(currentPage + 1)}>
                Sau
              </button>
            </div>
          ) : null}
        </div>
      </div>

      <BookDetailDrawer book={selectedBook} onClose={onCloseDetail} onAddToCart={onAddToCart} />
    </section>
  );
}
