import { useEffect, useMemo, useState } from "react";
import { EmptyState } from "../../shared/components/EmptyState";
import { formatDateTime, formatMoney } from "../../shared/utils/formatters";

const ordersPerPage = 10;

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

function getOrderItemPrice(item, books) {
  const responsePrice =
    item.price ??
    item.bookPrice ??
    item.book_price ??
    item.unitPrice ??
    item.unit_price ??
    item.totalPrice ??
    item.total_price ??
    item.book?.price;

  if (responsePrice !== undefined && responsePrice !== null && responsePrice !== "") {
    return responsePrice;
  }

  return books.find((book) => book.id === item.bookId)?.price;
}

export function OrdersPage({
  isSignedIn,
  orders,
  orderItems,
  books = [],
  selectedOrder,
  onReloadOrders,
  onLoadOrderDetail,
  onCloseOrderDetail,
}) {
  const [orderPage, setOrderPage] = useState(1);
  const orderTotalPages = Math.max(1, Math.ceil(orders.length / ordersPerPage));
  const pagedOrders = useMemo(() => {
    const startIndex = (orderPage - 1) * ordersPerPage;
    return orders.slice(startIndex, startIndex + ordersPerPage);
  }, [orders, orderPage]);

  useEffect(() => {
    setOrderPage(1);
  }, [orders]);

  return (
    <section className="orders-page">
      <section className="panel">
        <div className="section-heading clean-heading">
          <h1>Đơn hàng của tôi</h1>
          <button className="ghost-btn" type="button" disabled={!isSignedIn} onClick={onReloadOrders}>
            Tải lại
          </button>
        </div>

        {!isSignedIn ? <EmptyState>Đăng nhập để xem lịch sử đơn hàng.</EmptyState> : null}
        {isSignedIn && orders.length === 0 ? <EmptyState>Bạn chưa có đơn hàng nào.</EmptyState> : null}

        <div className="order-list">
          {pagedOrders.map((order) => (
            <article className="order-row" key={order.id}>
              <div>
                <strong>Đơn hàng {order.id}</strong>
                <span>{order.shipping_address}</span>
                <span>{formatDateTime(order.order_date)}</span>
              </div>
              <div>
                <strong>{formatMoney(order.total_price)}</strong>
                <span>{order.payment_status}</span>
              </div>
              <div className="row-actions">
                <button className="ghost-btn" type="button" onClick={() => onLoadOrderDetail(order.id)}>
                  Chi tiết
                </button>
              </div>
            </article>
          ))}
        </div>
        <PaginationControls page={orderPage} totalPages={orderTotalPages} onPageChange={setOrderPage} />
      </section>

      {selectedOrder ? (
        <div className="modal-backdrop" role="presentation" onMouseDown={onCloseOrderDetail}>
          <section className="modal-card order-detail-modal" onMouseDown={(event) => event.stopPropagation()}>
            <button className="close-btn" type="button" onClick={onCloseOrderDetail}>
              Đóng
            </button>
            <h2>Chi tiết đơn hàng {selectedOrder.id}</h2>
            {orderItems.length === 0 ? <EmptyState>Đơn hàng chưa có sản phẩm.</EmptyState> : null}
            <div className="simple-list">
              {orderItems.map((item) => {
                const price = getOrderItemPrice(item, books);

                return (
                  <div className="simple-row" key={item.orderItemId}>
                    <span>{item.bookName}</span>
                    <span>{price === undefined ? "Chưa có giá" : formatMoney(price)}</span>
                    <strong>x{item.quantity}</strong>
                  </div>
                );
              })}
            </div>
          </section>
        </div>
      ) : null}
    </section>
  );
}
