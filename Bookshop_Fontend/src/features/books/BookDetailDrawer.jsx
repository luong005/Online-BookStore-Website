import { useEffect } from "react";
import { createPortal } from "react-dom";
import { ImageWithFallback } from "../../shared/components/ImageWithFallback";
import { formatMoney } from "../../shared/utils/formatters";

function getBookContent(book) {
  return (
    book.content ||
    book.description ||
    book.summary ||
    book.shortDescription ||
    book.bookContent ||
    book.detail ||
    ""
  );
}

export function BookDetailDrawer({ book, onClose, onAddToCart }) {
  useEffect(() => {
    if (!book) return undefined;

    const previousBodyOverflow = document.body.style.overflow;
    const previousHtmlOverflow = document.documentElement.style.overflow;
    document.body.style.overflow = "hidden";
    document.documentElement.style.overflow = "hidden";

    return () => {
      document.body.style.overflow = previousBodyOverflow;
      document.documentElement.style.overflow = previousHtmlOverflow;
    };
  }, [book]);

  if (!book) return null;

  const modal = (
    <div className="modal-backdrop book-detail-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className="modal-card book-detail-modal"
        role="dialog"
        aria-modal="true"
        aria-label="Chi tiết sách"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="close-btn" type="button" onClick={onClose}>
          Đóng
        </button>

        <div className="book-detail-layout">
          <div className="detail-cover">
            <ImageWithFallback src={book.imageUrl || book.imageURL} alt={book.name} />
          </div>

          <div className="detail-content">
            <span className="tag">{book.categoryName || "Sách"}</span>
            <h2>{book.name}</h2>
            <p className="detail-summary">{getBookContent(book) || "Backend chưa trả nội dung sách."}</p>
            <p>Tác giả: {book.authorName || "Chưa cập nhật"}</p>
            <p>Nhà xuất bản: {book.publisher || "Chưa cập nhật"}</p>
            <p>Tồn kho: {book.stock ?? 0}</p>
            <strong>{formatMoney(book.price)}</strong>
            <button className="primary-btn wide" type="button" onClick={() => onAddToCart(book.id)}>
              Thêm vào giỏ hàng
            </button>
          </div>
        </div>
      </section>
    </div>
  );

  return createPortal(modal, document.body);
}
