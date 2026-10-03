import { useEffect, useMemo, useState } from "react";
import { EmptyState } from "../../shared/components/EmptyState";
import { StatCard } from "../../shared/components/StatCard";
import { formatMoney } from "../../shared/utils/formatters";

export const emptyBookForm = {
  name: "",
  price: "",
  stock: "",
  imageUrl: "",
  categoryName: "",
  authorName: "",
  publisher: "",
  content: "",
  status: "1",
};

export const emptyUpdateBookForm = {
  id: "",
  price: "",
  stock: "",
  imageURL: "",
};

const sectionLabels = {
  overview: "Tổng quan",
  users: "Người dùng",
  products: "Sản phẩm",
};

function getPagedItems(items, page, perPage) {
  const startIndex = (page - 1) * perPage;
  return items.slice(startIndex, startIndex + perPage);
}

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

function getRoleLabel(user) {
  const role = user.role || user.roles || user.roleDTO;
  if (typeof role === "string" || typeof role === "number") return role;

  return (
    user.role_id ||
    user.roleId ||
    user.role_name ||
    user.roleName ||
    user.nameRole ||
    role?.id ||
    role?.role_id ||
    role?.roleId ||
    role?.name ||
    role?.roleName ||
    role?.description ||
    "Chưa có"
  );
}

function UserTable({ users }) {
  if (!users.length) {
    return <EmptyState>Chưa có người dùng phù hợp.</EmptyState>;
  }

  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            <th>Họ tên</th>
            <th>Số điện thoại</th>
            <th>Địa chỉ</th>
            <th>Ngày sinh</th>
            <th>Role</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user, index) => (
            <tr key={`${user.phone_number || user.phoneNumber}-${index}`}>
              <td>{user.fullname || user.fullName || "Chưa cập nhật"}</td>
              <td>{user.phone_number || user.phoneNumber || "Chưa có"}</td>
              <td>{user.address || "Chưa cập nhật"}</td>
              <td>{user.date_of_birth ? String(user.date_of_birth).slice(0, 10) : "Chưa cập nhật"}</td>
              <td>{getRoleLabel(user)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function ProductTable({ books, onEdit, onDelete }) {
  if (!books.length) {
    return <EmptyState>Chưa tải được danh sách sách từ backend.</EmptyState>;
  }

  return (
    <div className="table-wrap">
      <table className="data-table product-table">
        <thead>
          <tr>
            <th>Sách</th>
            <th>Danh mục</th>
            <th>Tác giả</th>
            <th>NXB</th>
            <th>Giá</th>
            <th>Tồn kho</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          {books.map((book) => (
            <tr key={book.id}>
              <td>
                <strong>{book.name}</strong>
              </td>
              <td>{book.categoryName}</td>
              <td>{book.authorName}</td>
              <td>{book.publisher}</td>
              <td>{formatMoney(book.price)}</td>
              <td>{book.stock}</td>
              <td>{Number(book.status ?? book.bookStatus) === 1 ? "Đang bán" : "Không còn hiệu lực"}</td>
              <td>
                <div className="row-action-inline">
                  <button className="ghost-btn" type="button" onClick={() => onEdit(book)}>
                    Sửa
                  </button>
                  <button className="danger-btn" type="button" onClick={() => onDelete(book.id)}>
                    Xóa
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function getBestSellerBookName(item) {
  return (
    item.bookDTOS?.name ||
    item.bookDTO?.name ||
    item.book?.name ||
    item.bookName ||
    item.book_name ||
    item.name ||
    "Chưa có tên sách"
  );
}

function getBestSellerBookCategory(item) {
  return item.bookDTOS?.categoryName || item.bookDTO?.categoryName || item.book?.categoryName || "Chưa có";
}

function getBestSellerBookPrice(item) {
  return item.bookDTOS?.price ?? item.bookDTO?.price ?? item.book?.price ?? null;
}

function getBestSellerQuantity(item) {
  return item.total_quantity || item.totalQuantity || item.quantity || item.quantitySold || item.soldQuantity || item.totalSold || 0;
}

function getBestSellerRevenue(item) {
  return item.totalRevenue || item.revenue || item.totalPrice || item.totalAmount || item.amount || null;
}

function BestSellerTable({ bestSellers }) {
  if (!bestSellers.length) {
    return <EmptyState>Chưa có dữ liệu sách bán chạy từ server.</EmptyState>;
  }

  return (
    <div className="table-wrap">
      <table className="data-table bestseller-table">
        <thead>
          <tr>
            <th>Top</th>
            <th>Sách</th>
            <th>Danh mục</th>
            <th>Giá</th>
            <th>Số lượng bán</th>
            <th>Doanh thu</th>
          </tr>
        </thead>
        <tbody>
          {bestSellers.map((item, index) => {
            const price = getBestSellerBookPrice(item);
            const quantity = getBestSellerQuantity(item);
            const revenue = getBestSellerRevenue(item) ?? (price === null ? null : Number(price) * Number(quantity || 0));

            return (
              <tr key={`${item.bookDTOS?.id || item.bookDTO?.id || item.book?.id || getBestSellerBookName(item)}-${index}`}>
                <td>
                  <strong>#{index + 1}</strong>
                </td>
                <td>{getBestSellerBookName(item)}</td>
                <td>{getBestSellerBookCategory(item)}</td>
                <td>{price === null ? "Chưa có" : formatMoney(price)}</td>
                <td>{quantity}</td>
                <td>{revenue === null ? "Chưa có" : formatMoney(revenue)}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

export function AdminPage({
  isSignedIn,
  month,
  revenue,
  bestSellers,
  bestSellerTop,
  users,
  userTotal,
  userSearch,
  books,
  bookForm,
  updateBookForm,
  importFile,
  onMonthChange,
  onLoadDashboard,
  onBestSellerTopChange,
  onSearchBestSellers,
  onUserSearchChange,
  onSearchUsers,
  onBookFormChange,
  onUpdateBookFormChange,
  onImportFileChange,
  onCreateBook,
  onImportBooks,
  onUpdateBook,
  onDeleteBook,
}) {
  const [activeSection, setActiveSection] = useState("overview");
  const [productModal, setProductModal] = useState(null);
  const [userPage, setUserPage] = useState(1);
  const [productPage, setProductPage] = useState(1);
  const userTotalPages = Math.max(1, Math.ceil(users.length / 15));
  const productTotalPages = Math.max(1, Math.ceil(books.length / 10));
  const pagedUsers = useMemo(() => getPagedItems(users, userPage, 15), [users, userPage]);
  const pagedBooks = useMemo(() => getPagedItems(books, productPage, 10), [books, productPage]);

  useEffect(() => {
    setUserPage(1);
  }, [users]);

  useEffect(() => {
    setProductPage(1);
  }, [books]);

  function openUpdateModal(book) {
    onUpdateBookFormChange({
      id: book.id,
      price: book.price ?? "",
      stock: book.stock ?? "",
      imageURL: book.imageURL || book.imageUrl || "",
    });
    setProductModal("update");
  }

  function closeProductModal() {
    setProductModal(null);
  }

  return (
    <section className="admin-dashboard">
      <div className="admin-hero">
        <div>
          <h1>Dashboard quản trị</h1>
        </div>
        <div className="admin-tabs">
          {Object.entries(sectionLabels).map(([key, label]) => (
            <button
              key={key}
              className={activeSection === key ? "active" : ""}
              type="button"
              onClick={() => setActiveSection(key)}
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      {activeSection === "overview" ? (
        <section className="admin-section">
          <div className="panel gradient-panel">
            <div className="section-heading clean-heading">
              <h2>Doanh thu theo tháng</h2>
              <button className="primary-btn" type="button" disabled={!isSignedIn} onClick={onLoadDashboard}>
                Tải doanh thu
              </button>
            </div>
            <div className="overview-grid">
              <label className="month-picker">
                Tháng
                <input
                  type="number"
                  min="1"
                  max="12"
                  value={month}
                  onChange={(event) => onMonthChange(event.target.value)}
                />
              </label>
              <StatCard
                label={`Tổng tiền tháng ${month}`}
                value={revenue === null ? "Chưa tải" : formatMoney(revenue)}
                helper="Lấy từ API doanh thu của backend"
              />
            </div>
          </div>

          <div className="panel">
            <div className="section-heading clean-heading">
              <h2>Sách bán chạy</h2>
              <strong>{bestSellers.length} sách</strong>
            </div>

            <form className="admin-search-form bestseller-search-form" onSubmit={onSearchBestSellers}>
              <label>
                Top bestseller
                <input
                  type="number"
                  min="1"
                  value={bestSellerTop}
                  onChange={(event) => onBestSellerTopChange(event.target.value)}
                  placeholder="Nhập số lượng top"
                />
              </label>
              <button className="primary-btn" type="button" disabled={!isSignedIn} onClick={onSearchBestSellers}>
                Tìm kiếm
              </button>
            </form>

            <BestSellerTable bestSellers={bestSellers} />
          </div>
        </section>
      ) : null}

      {activeSection === "users" ? (
        <section className="admin-section">
          <div className="panel">
            <div className="section-heading clean-heading">
              <h2>Người dùng</h2>
              <strong>{userTotal} người dùng</strong>
            </div>

            <form className="admin-search-form" onSubmit={onSearchUsers}>
              <label>
                Tìm theo địa chỉ
                <input
                  value={userSearch.address}
                  onChange={(event) => onUserSearchChange({ ...userSearch, address: event.target.value })}
                  placeholder="Nhập địa chỉ"
                />
              </label>
              <label>
                Role ID
                <input
                  type="number"
                  min="1"
                  value={userSearch.role_id}
                  placeholder="Nhập role_id"
                  onChange={(event) => onUserSearchChange({ ...userSearch, role_id: event.target.value })}
                />
              </label>
              <button className="primary-btn" type="button" onClick={onSearchUsers}>
                Tìm kiếm
              </button>
            </form>

            <UserTable users={pagedUsers} />
            <PaginationControls page={userPage} totalPages={userTotalPages} onPageChange={setUserPage} />
          </div>
        </section>
      ) : null}

      {activeSection === "products" ? (
        <section className="admin-section">
          <div className="panel">
            <div className="section-heading clean-heading">
              <h2>Product</h2>
              <div className="row-action-inline">
                <button className="primary-btn" type="button" onClick={() => setProductModal("insert")}>
                  Thêm sách
                </button>
                <button className="ghost-btn" type="button" onClick={() => setProductModal("import")}>
                  Import Excel
                </button>
              </div>
            </div>

            <ProductTable books={pagedBooks} onEdit={openUpdateModal} onDelete={onDeleteBook} />
            <PaginationControls page={productPage} totalPages={productTotalPages} onPageChange={setProductPage} />
          </div>
        </section>
      ) : null}

      {productModal === "insert" ? (
        <div className="modal-backdrop" role="presentation" onMouseDown={closeProductModal}>
          <section className="modal-card admin-modal" onMouseDown={(event) => event.stopPropagation()}>
            <button className="close-btn" type="button" onClick={closeProductModal}>
              Đóng
            </button>
            <h2>Thêm một sách</h2>
            <form className="stack-form" onSubmit={onCreateBook}>
              <input
                placeholder="Tên sách"
                required
                value={bookForm.name}
                onChange={(event) => onBookFormChange({ ...bookForm, name: event.target.value })}
              />
              <div className="two-columns">
                <input
                  placeholder="Giá"
                  required
                  type="number"
                  min="0"
                  value={bookForm.price}
                  onChange={(event) => onBookFormChange({ ...bookForm, price: event.target.value })}
                />
                <input
                  placeholder="Tồn kho"
                  required
                  type="number"
                  min="0"
                  value={bookForm.stock}
                  onChange={(event) => onBookFormChange({ ...bookForm, stock: event.target.value })}
                />
              </div>
              <label>
                Status
                <input
                  placeholder="1 là hiển thị ở cửa hàng"
                  required
                  type="number"
                  min="0"
                  value={bookForm.status}
                  onChange={(event) => onBookFormChange({ ...bookForm, status: event.target.value })}
                />
              </label>
              <input
                placeholder="Image URL"
                required
                value={bookForm.imageUrl}
                onChange={(event) => onBookFormChange({ ...bookForm, imageUrl: event.target.value })}
              />
              <input
                placeholder="Danh mục"
                required
                value={bookForm.categoryName}
                onChange={(event) => onBookFormChange({ ...bookForm, categoryName: event.target.value })}
              />
              <input
                placeholder="Tác giả"
                required
                value={bookForm.authorName}
                onChange={(event) => onBookFormChange({ ...bookForm, authorName: event.target.value })}
              />
              <input
                placeholder="Nhà xuất bản"
                required
                value={bookForm.publisher}
                onChange={(event) => onBookFormChange({ ...bookForm, publisher: event.target.value })}
              />
              <textarea
                placeholder="Nội dung ngắn của sách"
                rows="3"
                value={bookForm.content}
                onChange={(event) => onBookFormChange({ ...bookForm, content: event.target.value })}
              />
              <button className="primary-btn" type="submit" disabled={!isSignedIn}>
                Lưu sách
              </button>
            </form>
          </section>
        </div>
      ) : null}

      {productModal === "import" ? (
        <div className="modal-backdrop" role="presentation" onMouseDown={closeProductModal}>
          <section className="modal-card admin-modal" onMouseDown={(event) => event.stopPropagation()}>
            <button className="close-btn" type="button" onClick={closeProductModal}>
              Đóng
            </button>
            <h2>Import file Excel</h2>
            <form className="stack-form" onSubmit={onImportBooks}>
              <label>
                File Excel
                <input
                  required
                  type="file"
                  accept=".xlsx,.xls"
                  onChange={(event) => onImportFileChange(event.target.files?.[0] || null)}
                />
              </label>
              {importFile ? <span className="muted-text">Đã chọn: {importFile.name}</span> : null}
              <button className="primary-btn" type="submit" disabled={!isSignedIn || !importFile}>
                Tải file lên
              </button>
            </form>
          </section>
        </div>
      ) : null}

      {productModal === "update" ? (
        <div className="modal-backdrop" role="presentation" onMouseDown={closeProductModal}>
          <section className="modal-card admin-modal" onMouseDown={(event) => event.stopPropagation()}>
            <button className="close-btn" type="button" onClick={closeProductModal}>
              Đóng
            </button>
            <h2>Cập nhật sách</h2>
            <form className="stack-form" onSubmit={onUpdateBook}>
              <label>
                Mã sách
                <input readOnly value={`Sách ID: ${updateBookForm.id}`} />
              </label>
              <label>
                Giá
                <input
                  placeholder="Nhập giá mới"
                  required
                  type="number"
                  min="0"
                  value={updateBookForm.price}
                  onChange={(event) => onUpdateBookFormChange({ ...updateBookForm, price: event.target.value })}
                />
              </label>
              <label>
                Tồn kho
                <input
                  placeholder="Nhập số lượng tồn kho"
                  required
                  type="number"
                  min="0"
                  value={updateBookForm.stock}
                  onChange={(event) => onUpdateBookFormChange({ ...updateBookForm, stock: event.target.value })}
                />
              </label>
              <label>
                Image URL
                <input
                  placeholder="Nhập đường dẫn ảnh mới"
                  required
                  value={updateBookForm.imageURL}
                  onChange={(event) => onUpdateBookFormChange({ ...updateBookForm, imageURL: event.target.value })}
                />
              </label>
              <button className="primary-btn" type="submit" disabled={!isSignedIn}>
                Cập nhật
              </button>
            </form>
          </section>
        </div>
      ) : null}
    </section>
  );
}
