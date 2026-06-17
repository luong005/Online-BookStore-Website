import { useEffect, useMemo, useState } from "react";
import { EmptyState } from "../../shared/components/EmptyState";
import { formatMoney } from "../../shared/utils/formatters";

const cartItemsPerPage = 10;

function PaginationControls({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;

  return (
    <div className="pagination-bar">
      <button className="ghost-btn" type="button" disabled={page === 1} onClick={() => onPageChange(page - 1)}>
        Trước
      </button>
      <span>
        Trang {page} / {totalPages}
      </span>
      <button className="ghost-btn" type="button" disabled={page === totalPages} onClick={() => onPageChange(page + 1)}>
        Sau
      </button>
    </div>
  );
}

function CartItemRow({ item, unitPrice, onUpdateItem, onRemoveItem, readonly = false }) {
  return (
    <article className="cart-row">
      <div>
        <strong>{item.bookName}</strong>
        <span>{unitPrice !== undefined ? `Giá một cuốn: ${formatMoney(unitPrice)}` : "Giá đang cập nhật"}</span>
      </div>
      <input
        aria-label={`Số lượng ${item.bookName}`}
        type="number"
        min="1"
        value={item.quantity}
        readOnly={readonly}
        onChange={(event) => onUpdateItem?.(item.cartItemId, event.target.value)}
      />
      {readonly ? (
        <span className="muted-text">Đã chọn</span>
      ) : (
        <button className="danger-btn" type="button" onClick={() => onRemoveItem(item.cartItemId)}>
          Xóa
        </button>
      )}
    </article>
  );
}

export function CartPage({
  isSignedIn,
  cart,
  books,
  checkout,
  checkoutOpen,
  orderForm,
  onOrderFormChange,
  onReloadCart,
  onStartCheckout,
  onCloseCheckout,
  onUpdateItem,
  onRemoveItem,
  onCreatePayment,
}) {
  const [cartPage, setCartPage] = useState(1);
  const cartCount = cart.reduce((sum, item) => sum + Number(item.quantity || 0), 0);
  const totalPrice = checkout?.totalPrice || 0;
  const reviewItems = checkout?.cartItemDTOS?.length ? checkout.cartItemDTOS : cart;
  const getUnitPrice = (bookId) => books.find((book) => book.id === bookId)?.price;
  const cartTotalPages = Math.max(1, Math.ceil(cart.length / cartItemsPerPage));
  const pagedCart = useMemo(() => {
    const startIndex = (cartPage - 1) * cartItemsPerPage;
    return cart.slice(startIndex, startIndex + cartItemsPerPage);
  }, [cart, cartPage]);

  useEffect(() => {
    setCartPage(1);
  }, [cart]);

  return (
    <section className="cart-page">
      <div className="section-heading clean-heading">
        <h1>{cartCount} sản phẩm</h1>
        {isSignedIn ? (
          <button className="ghost-btn" type="button" onClick={onReloadCart}>
            Tải lại
          </button>
        ) : null}
      </div>

      {!isSignedIn ? <EmptyState>Đăng nhập để xem và quản lý giỏ hàng.</EmptyState> : null}
      {isSignedIn && cart.length === 0 ? <EmptyState>Giỏ hàng đang trống.</EmptyState> : null}

      <div className="cart-list">
        {pagedCart.map((item) => (
          <CartItemRow
            key={item.cartItemId}
            item={item}
            unitPrice={getUnitPrice(item.bookId)}
            onUpdateItem={onUpdateItem}
            onRemoveItem={onRemoveItem}
          />
        ))}
      </div>
      <PaginationControls page={cartPage} totalPages={cartTotalPages} onPageChange={setCartPage} />

      {isSignedIn && cart.length > 0 ? (
        <div className="cart-footer">
          <button className="primary-btn" type="button" onClick={onStartCheckout}>
            Mua
          </button>
        </div>
      ) : null}

      {checkoutOpen ? (
        <div className="modal-backdrop" role="presentation" onMouseDown={onCloseCheckout}>
          <section className="modal-card checkout-review checkout-modal" onMouseDown={(event) => event.stopPropagation()}>
            <button className="close-btn" type="button" onClick={onCloseCheckout}>
              Đóng
            </button>
            <div className="section-heading clean-heading">
              <h2>Sản phẩm sẽ thanh toán</h2>
              <strong>{formatMoney(totalPrice)}</strong>
            </div>

            <div className="cart-list checkout-item-list">
              {reviewItems.map((item) => (
                <CartItemRow key={item.cartItemId} item={item} unitPrice={getUnitPrice(item.bookId)} readonly />
              ))}
            </div>

            <form className="checkout-form" onSubmit={onCreatePayment}>
              <label>
                Địa chỉ nhận hàng
                <textarea
                  required
                  rows="3"
                  value={orderForm.shippingAddress}
                  onChange={(event) => onOrderFormChange({ ...orderForm, shippingAddress: event.target.value })}
                  placeholder="Số nhà, đường, phường/xã..."
                />
              </label>
              <label>
                Số điện thoại
                <input
                  required
                  value={orderForm.phoneNumber}
                  onChange={(event) => onOrderFormChange({ ...orderForm, phoneNumber: event.target.value })}
                  placeholder="Số điện thoại nhận hàng"
                />
              </label>
              <button className="primary-btn wide" type="submit">
                Thanh toán
              </button>
            </form>
          </section>
        </div>
      ) : null}
    </section>
  );
}
