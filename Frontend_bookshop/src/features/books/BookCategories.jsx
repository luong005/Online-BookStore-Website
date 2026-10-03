export function BookCategories({ categories, selectedCategory, onSelect }) {
  return (
    <aside className="category-panel" aria-labelledby="category-heading">
      <div className="category-heading">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" aria-hidden="true">
          <rect x="3" y="3" width="7" height="7" rx="1.5" />
          <rect x="14" y="3" width="7" height="7" rx="1.5" />
          <rect x="3" y="14" width="7" height="7" rx="1.5" />
          <rect x="14" y="14" width="7" height="7" rx="1.5" />
        </svg>
        <h2 id="category-heading">Danh mục sách</h2>
      </div>
      <p className="category-hint">Chọn chủ đề bạn yêu thích</p>
      <nav className="category-list" aria-label="Danh mục sách">
        {["", ...categories].map((category) => (
          <button
            key={category}
            className={`category-button${selectedCategory === category ? " active" : ""}`}
            type="button"
            aria-pressed={selectedCategory === category}
            aria-controls="catalog-results"
            onClick={() => onSelect(category)}
          >
            <span>{category || "Tất cả sách"}</span>
            <span className="category-indicator" aria-hidden="true">
              {selectedCategory === category ? "✓" : "›"}
            </span>
          </button>
        ))}
      </nav>
    </aside>
  );
}
