export function BookFilters({ filters, categories, loading, onChange, onSubmit, onReset }) {
  return (
    <aside className="search-panel">
      <h2>Tìm kiếm sách</h2>
      <form className="stack-form" onSubmit={onSubmit}>
        <label>
          Tên sách
          <input
            value={filters.name}
            onChange={(event) => onChange({ ...filters, name: event.target.value })}
            placeholder="Nhập tên sách"
          />
        </label>

        <label>
          Danh mục
          <select
            value={filters.category}
            onChange={(event) => onChange({ ...filters, category: event.target.value })}
          >
            <option value="">Tất cả danh mục</option>
            {categories.map((category) => (
              <option key={category} value={category}>
                {category}
              </option>
            ))}
          </select>
        </label>

        <div className="two-columns">
          <label>
            Giá từ
            <input
              type="number"
              min="0"
              value={filters.minPrice}
              onChange={(event) => onChange({ ...filters, minPrice: event.target.value })}
            />
          </label>

          <label>
            Giá đến
            <input
              type="number"
              min="0"
              value={filters.maxPrice}
              onChange={(event) => onChange({ ...filters, maxPrice: event.target.value })}
            />
          </label>
        </div>

        <div className="form-actions">
          <button className="primary-btn" type="submit" disabled={loading}>
            {loading ? "Đang tìm..." : "Tìm kiếm"}
          </button>
          <button className="ghost-btn" type="button" onClick={onReset}>
            Làm mới
          </button>
        </div>
      </form>
    </aside>
  );
}
