import { ImageWithFallback } from "../../shared/components/ImageWithFallback";
import { formatMoney } from "../../shared/utils/formatters";

export function BookCard({ book, onAddToCart, onViewDetail }) {
  return (
    <article className="book-card">
      <div className="book-cover">
        <ImageWithFallback src={book.imageUrl} alt={book.name} />
      </div>

      <div className="book-body">
        <span className="tag">{book.categoryName || "Chưa phân loại"}</span>
        <h3>{book.name}</h3>
        <p>
          {book.authorName || "Chưa có tác giả"} - {book.publisher || "Chưa có NXB"}
        </p>
        <div className="book-meta">
          <strong>{formatMoney(book.price)}</strong>
          <span>Còn {book.stock ?? 0}</span>
        </div>
      </div>

      <div className="card-actions">
        <button className="ghost-btn" type="button" onClick={() => onViewDetail(book.id)}>
          Chi tiết
        </button>
        <button className="primary-btn" type="button" onClick={() => onAddToCart(book.id)}>
          Thêm giỏ
        </button>
      </div>
    </article>
  );
}
