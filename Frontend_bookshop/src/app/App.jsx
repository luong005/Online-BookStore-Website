import { useEffect, useMemo, useRef, useState } from "react";
import { AboutPage } from "../features/about/AboutPage";
import { AccountPage } from "../features/account/AccountPage";
import { accountService } from "../features/account/accountService";
import { AdminPage, emptyBookForm, emptyUpdateBookForm } from "../features/admin/AdminPage";
import { adminService } from "../features/admin/adminService";
import { BookCatalog } from "../features/books/BookCatalog";
import { bookService } from "../features/books/bookService";
import { CartPage } from "../features/cart/CartPage";
import { cartService } from "../features/cart/cartService";
import { BuyingGuidePage } from "../features/guide/BuyingGuidePage";
import { OrdersPage } from "../features/orders/OrdersPage";
import { orderService } from "../features/orders/orderService";
import { StatusMessages } from "../shared/components/StatusMessages";
import { useAsyncAction } from "../shared/hooks/useAsyncAction";
import { isAdminAccount } from "../shared/utils/auth";
import { getErrorMessage, normalizeList } from "../shared/utils/formatters";

const emptyFilters = { name: "", category: "", minPrice: "", maxPrice: "" };
const validPages = new Set(["shop", "cart", "orders", "guide", "about", "login", "register", "profile", "password", "admin"]);

function getInitialTab() {
  const page = new URLSearchParams(window.location.search).get("page");
  return validPages.has(page) ? page : "shop";
}

function getPageUrl(page) {
  const url = new URL(window.location.href);
  url.searchParams.set("page", page);
  url.searchParams.delete("payment_return");
  return `${url.pathname}${url.search}${url.hash}`;
}

function getProfileName(profile) {
  return profile?.fullname || profile?.fullName || profile?.phone_number || profile?.phoneNumber;
}

function getBirthday(profile) {
  const rawDate = profile?.date_of_birth || profile?.dateOfBirth;
  return rawDate ? String(rawDate).slice(0, 10) : "";
}

function normalizeSearchText(value) {
  return String(value || "").trim().toLowerCase();
}

function getUserRoleValues(user) {
  const role = user?.role || user?.roles || user?.roleDTO;
  const values = [
    user?.role_id,
    user?.roleId,
    user?.role_name,
    user?.roleName,
    user?.nameRole,
    role?.id,
    role?.role_id,
    role?.roleId,
    role?.name,
    role?.roleName,
    role?.description,
  ];

  if (typeof role === "string" || typeof role === "number") {
    values.push(role);
  }

  const normalizedValues = values.filter((value) => value !== undefined && value !== null && value !== "");
  const roleText = normalizedValues.map((value) => normalizeSearchText(value)).join(" ");
  if (roleText.includes("admin")) normalizedValues.push(1);
  if (roleText.includes("user") || roleText.includes("customer")) normalizedValues.push(2);

  return normalizedValues;
}

function filterAdminUsers(userList, filters) {
  const address = normalizeSearchText(filters?.address);
  const roleId = normalizeSearchText(filters?.role_id);

  return userList.filter((user) => {
    const userAddress = normalizeSearchText(user?.address);
    const roleValues = getUserRoleValues(user).map((value) => normalizeSearchText(value));
    const matchAddress = !address || userAddress.includes(address);
    const matchRole = !roleId || !roleValues.length || roleValues.includes(roleId);

    return matchAddress && matchRole;
  });
}

function isShopVisibleBook(book) {
  return Number(book?.status ?? book?.bookStatus) === 1;
}

function getSafeHttpUrl(value) {
  try {
    const url = new URL(value);
    return url.protocol === "http:" || url.protocol === "https:" ? url.href : "";
  } catch {
    return "";
  }
}

export default function App() {
  const [activeTab, setActiveTab] = useState(getInitialTab);
  const [token, setToken] = useState(() => localStorage.getItem("bookshop_token") || "");
  const [books, setBooks] = useState([]);
  const [selectedBook, setSelectedBook] = useState(null);
  const [filters, setFilters] = useState(emptyFilters);
  const [appliedFilters, setAppliedFilters] = useState(emptyFilters);
  const [categories, setCategories] = useState([]);
  const booksRequestId = useRef(0);
  const [searchOpen, setSearchOpen] = useState(false);
  const [loadingBooks, setLoadingBooks] = useState(false);
  const [cart, setCart] = useState([]);
  const [checkout, setCheckout] = useState(null);
  const [checkoutOpen, setCheckoutOpen] = useState(false);
  const [orders, setOrders] = useState([]);
  const [orderItems, setOrderItems] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [profile, setProfile] = useState(null);
  const [hasAdminAccess, setHasAdminAccess] = useState(false);
  const [profileEditing, setProfileEditing] = useState(false);
  const [accountMenuOpen, setAccountMenuOpen] = useState(false);
  const [authForm, setAuthForm] = useState({ phoneNumber: "", password: "", confirmPassword: "" });
  const [profileForm, setProfileForm] = useState({ fullname: "", address: "", date_of_birth: "" });
  const [passwordForm, setPasswordForm] = useState({ oldPassword: "", newPassword: "" });
  const [orderForm, setOrderForm] = useState({ shippingAddress: "", phoneNumber: "" });
  const [adminMonth, setAdminMonth] = useState(new Date().getMonth() + 1);
  const [revenue, setRevenue] = useState(null);
  const [revenueLoading, setRevenueLoading] = useState(false);
  const [revenueError, setRevenueError] = useState("");
  const revenueRequestId = useRef(0);
  const [bestSellers, setBestSellers] = useState([]);
  const [recommendedBestSellers, setRecommendedBestSellers] = useState([]);
  const [loadingRecommendations, setLoadingRecommendations] = useState(true);
  const [recommendationsError, setRecommendationsError] = useState(false);
  const [bestSellerTop, setBestSellerTop] = useState("10");
  const [users, setUsers] = useState([]);
  const [userTotal, setUserTotal] = useState(0);
  const [userSearch, setUserSearch] = useState({ address: "", role_id: "" });
  const [bookForm, setBookForm] = useState(emptyBookForm);
  const [updateBookForm, setUpdateBookForm] = useState(emptyUpdateBookForm);
  const [importFile, setImportFile] = useState(null);
  const [importResult, setImportResult] = useState(null);
  const [importDownloadError, setImportDownloadError] = useState("");
  const { busy, notice, error, setNotice, setError, runAction } = useAsyncAction();

  const isSignedIn = Boolean(token);
  const isAdmin = hasAdminAccess || isAdminAccount(token, profile);
  const visibleShopBooks = useMemo(
    () => books.filter((book) => isShopVisibleBook(book) && (!appliedFilters.category || book.categoryName === appliedFilters.category)),
    [books, appliedFilters.category],
  );
  const cartCount = cart.reduce((sum, item) => sum + Number(item.quantity || 0), 0);

  function navigateTo(page, { replace = false } = {}) {
    setActiveTab(page);
    setAccountMenuOpen(false);

    const nextUrl = getPageUrl(page);
    const currentUrl = `${window.location.pathname}${window.location.search}${window.location.hash}`;
    if (nextUrl === currentUrl) return;

    if (replace) {
      window.history.replaceState({ page }, "", nextUrl);
      return;
    }
    window.history.pushState({ page }, "", nextUrl);
  }

  function saveToken(nextToken) {
    setToken(nextToken);
    if (nextToken) localStorage.setItem("bookshop_token", nextToken);
    else localStorage.removeItem("bookshop_token");
  }

  function clearSession() {
    saveToken("");
    setProfile(null);
    setHasAdminAccess(false);
    setCart([]);
    setCheckout(null);
    setCheckoutOpen(false);
    setOrders([]);
    setOrderItems([]);
    setSelectedOrder(null);
    setUsers([]);
    setUserTotal(0);
    setAccountMenuOpen(false);
    setProfileEditing(false);
  }

  function applyProfile(data) {
    setProfile(data);
    setProfileForm({
      fullname: data?.fullname || data?.fullName || "",
      address: data?.address || "",
      date_of_birth: getBirthday(data),
    });
    setOrderForm((current) => ({
      ...current,
      phoneNumber: current.phoneNumber || data?.phone_number || data?.phoneNumber || "",
      shippingAddress: current.shippingAddress || data?.address || "",
    }));
  }

  async function loadBooks(nextFilters = filters) {
    const requestId = ++booksRequestId.current;
    const searchFilters = { ...nextFilters };
    setAppliedFilters(searchFilters);
    setLoadingBooks(true);
    setError("");
    try {
      const nextBooks = normalizeList(await bookService.searchBooks(searchFilters));
      // Keep the full category list available when search results are narrowed down.
      if (!Object.values(searchFilters).some((value) => String(value).trim())) {
        setCategories(
          [...new Set(nextBooks.filter(isShopVisibleBook).map((book) => book.categoryName).filter(Boolean))]
            .sort((first, second) => first.localeCompare(second, "vi")),
        );
      }
      if (requestId !== booksRequestId.current) return false;
      setBooks(nextBooks);
      return true;
    } catch (err) {
      if (requestId !== booksRequestId.current) return false;
      setBooks([]);
      setError(err?.message || "Không tải được danh sách sách.");
      return false;
    } finally {
      if (requestId === booksRequestId.current) setLoadingBooks(false);
    }
  }

  async function loadProfile(authToken = token) {
    if (!authToken) return null;
    const data = await accountService.getProfile(authToken);
    applyProfile(data);
    return data;
  }

  async function loadCart() {
    if (!token) return;
    const [cartData, checkoutData] = await Promise.all([
      cartService.getCart(token),
      cartService.getCheckout(token),
    ]);
    setCart(normalizeList(cartData));
    setCheckout(checkoutData);
  }

  async function loadOrders() {
    if (!token) return;
    setOrders(normalizeList(await orderService.getOrders(token)));
  }

  async function loadAdminDashboard(authToken = token) {
    if (!authToken) return;
    await runAction(async () => {
      const [, sellerData] = await Promise.all([
        loadRevenue(adminMonth, authToken),
        adminService.getBestSellers(bestSellerTop || 10, authToken),
      ]);
      setBestSellers(normalizeList(sellerData));
    }, "Đang tải dashboard...");
  }

  async function loadRevenue(month = adminMonth, authToken = token) {
    if (!authToken || !month || Number(month) < 1 || Number(month) > 12) return;
    const requestId = ++revenueRequestId.current;
    setRevenueLoading(true);
    setRevenueError("");
    setRevenue(null);
    try {
      const data = await adminService.getRevenue(month, authToken);
      if (requestId === revenueRequestId.current) setRevenue(data);
    } catch (err) {
      if (requestId === revenueRequestId.current) setRevenueError(err.message || "Không tải được doanh thu.");
    } finally {
      if (requestId === revenueRequestId.current) setRevenueLoading(false);
    }
  }

  async function loadBestSellers(authToken = token, top = bestSellerTop) {
    if (!authToken) {
      setError("Bạn cần đăng nhập admin trước khi tìm bestseller.");
      return;
    }

    await runAction(async () => {
      const data = await adminService.getBestSellers(top || 10, authToken);
      const sellerList = normalizeList(data);
      setBestSellers(sellerList);
      setNotice(`Đã tải top ${sellerList.length} sách bán chạy.`);
    }, "Đang tải bestseller...");
  }

  async function loadAdminUsers(authToken = token, filters = userSearch) {
    if (!authToken) {
      setError("Bạn cần đăng nhập admin trước khi tìm kiếm người dùng.");
      return;
    }
    await runAction(async () => {
      const data = await adminService.getUsers(filters, authToken);
      const userList = normalizeList(
        data?.userInfoResponseDTOS || data?.userInfoResponseDTOList || data?.userDTOList || data?.users || data,
      );
      const filteredUsers = filterAdminUsers(userList, filters);
      setUsers(filteredUsers);
      setUserTotal(filteredUsers.length);
      setNotice(`Đã tải ${filteredUsers.length} người dùng.`);
    }, "Đang tải người dùng...");
  }

  async function canAccessAdmin(authToken, profileData) {
    if (isAdminAccount(authToken, profileData)) return true;
    try {
      const revenueData = await adminService.getRevenue(adminMonth, authToken, { skipRefresh: true });
      setRevenue(revenueData);
      return true;
    } catch {
      return false;
    }
  }

  useEffect(() => {
    loadBooks();
  }, []);

  useEffect(() => {
    if (activeTab !== "shop") return undefined;
    let active = true;
    setLoadingRecommendations(true);
    setRecommendationsError(false);
    bookService.getBestSellers(10)
      .then((data) => {
        if (active) setRecommendedBestSellers(normalizeList(data));
      })
      .catch(() => {
        if (active) setRecommendationsError(true);
      })
      .finally(() => {
        if (active) setLoadingRecommendations(false);
      });
    return () => { active = false; };
  }, [activeTab]);

  useEffect(() => {
    if (!notice) return undefined;
    const timerId = window.setTimeout(() => setNotice(""), 2600);
    return () => window.clearTimeout(timerId);
  }, [notice, setNotice]);

  useEffect(() => {
    const handleTokenRefreshed = (event) => setToken(event.detail);
    const handleSessionExpired = () => {
      clearSession();
      window.alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
      navigateTo("login", { replace: true });
    };

    window.addEventListener("bookshop:token-refreshed", handleTokenRefreshed);
    window.addEventListener("bookshop:session-expired", handleSessionExpired);
    return () => {
      window.removeEventListener("bookshop:token-refreshed", handleTokenRefreshed);
      window.removeEventListener("bookshop:session-expired", handleSessionExpired);
    };
  }, []);

  useEffect(() => {
    const handlePopState = () => {
      setActiveTab(getInitialTab());
      setAccountMenuOpen(false);
    };
    window.addEventListener("popstate", handlePopState);
    return () => window.removeEventListener("popstate", handlePopState);
  }, []);

  useEffect(() => {
    if (!token) return;
    runAction(async () => {
      const data = await loadProfile(token);
      const adminAllowed = await canAccessAdmin(token, data);
      setHasAdminAccess(adminAllowed);
      await Promise.all([loadCart(), loadOrders()]);
      if (adminAllowed && activeTab === "admin") {
        await Promise.all([loadAdminDashboard(), loadAdminUsers()]);
      }
    }, "Đang tải tài khoản...");
  }, [token]);

  useEffect(() => {
    if (!token || !isAdmin || activeTab !== "admin") return;
    loadAdminDashboard();
  }, [activeTab, isAdmin]);

  useEffect(() => {
    if (!token || !isAdmin || activeTab !== "admin") return;
    loadRevenue(adminMonth);
  }, [adminMonth]);

  useEffect(() => {
    if (!token) return;
    const params = new URLSearchParams(window.location.search);
    if (!params.get("payment_return")) return;

    navigateTo("cart", { replace: true });
    runAction(async () => {
      const pendingOrderCode = localStorage.getItem("bookshop_pending_order_code");
      const pendingCartIds = JSON.parse(localStorage.getItem("bookshop_pending_cart_items") || "[]");

      if (pendingOrderCode) {
        try {
          const status = await orderService.getPaymentStatus(pendingOrderCode, token);
          if (status?.payment_status === "PAID" || status?.paymentStatus === "PAID") {
            await Promise.allSettled(pendingCartIds.map((id) => cartService.removeItem(id, token)));
          }
        } catch {
          // Backend may already clear the cart after successful payment.
        }
      }

      localStorage.removeItem("bookshop_pending_order_code");
      localStorage.removeItem("bookshop_pending_cart_items");
      await Promise.all([loadCart(), loadOrders()]);
      navigateTo("cart", { replace: true });
    }, "Đang cập nhật sau thanh toán...");
  }, [token]);

  async function handleSearch(event) {
    event.preventDefault();
    if (await loadBooks(filters)) setSearchOpen(false);
  }

  async function handleSelectCategory(category) {
    const nextFilters = { ...filters, category };
    setFilters(nextFilters);
    await loadBooks(nextFilters);
  }

  async function handleResetFilters() {
    setFilters(emptyFilters);
    await loadBooks(emptyFilters);
  }

  async function handleViewDetail(bookId) {
    await runAction(async () => {
      setSelectedBook(await bookService.getBook(bookId));
    }, "Đang tải chi tiết sách...");
  }

  async function handleAddToCart(bookId) {
    if (!token) {
      navigateTo("login");
      setError("Bạn cần đăng nhập trước khi thêm sách vào giỏ.");
      return;
    }

    await runAction(async () => {
      await cartService.addItem({ bookId, quantity: 1 }, token);
      await loadCart();
      setCheckoutOpen(false);
      setNotice("Đã thêm sách vào giỏ hàng.");
      navigateTo("cart");
    }, "Đang thêm vào giỏ...");
  }

  async function handleUpdateCartItem(cartItemId, quantity) {
    await runAction(async () => {
      await cartService.updateItem({ id: cartItemId, quantity: Math.max(1, Number(quantity) || 1) }, token);
      await loadCart();
    }, "Đang cập nhật giỏ...");
  }

  async function handleRemoveCartItem(cartItemId) {
    await runAction(async () => {
      await cartService.removeItem(cartItemId, token);
      await loadCart();
      setCheckoutOpen(false);
      setNotice("Đã xóa sản phẩm khỏi giỏ.");
    }, "Đang xóa sản phẩm...");
  }

  async function handleStartCheckout() {
    await runAction(async () => {
      setCheckout(await cartService.getCheckout(token));
      setCheckoutOpen(true);
    }, "Đang tính tổng tiền...");
  }

  async function handleCreatePayment(event) {
    event.preventDefault();
    const payLater = event.nativeEvent.submitter?.value === "later";
    await runAction(async () => {
      const returnUrl = `${window.location.origin}${window.location.pathname}?page=cart&payment_return=1`;
      const payload = {
        shippingAddress: orderForm.shippingAddress,
        phoneNumber: orderForm.phoneNumber,
        returnUrl,
        cancelUrl: returnUrl,
      };

      if (payLater) {
        await orderService.payLater(payload, token);
        await Promise.allSettled(cart.map((item) => cartService.removeItem(item.cartItemId, token)));
        setCheckoutOpen(false);
        await Promise.all([loadCart(), loadOrders()]);
        navigateTo("orders");
        setNotice("Đã tạo đơn hàng thanh toán sau.");
        return;
      }

      const data = await orderService.createPayment(payload, token);

      const checkoutUrl = data?.checkout_url || data?.checkoutUrl;
      const orderCode = data?.order_code || data?.orderCode;
      if (orderCode) localStorage.setItem("bookshop_pending_order_code", String(orderCode));
      localStorage.setItem(
        "bookshop_pending_cart_items",
        JSON.stringify(cart.map((item) => item.cartItemId).filter(Boolean)),
      );

      const safeCheckoutUrl = getSafeHttpUrl(checkoutUrl);
      if (!safeCheckoutUrl) {
        setNotice("Server đã tạo thanh toán nhưng chưa trả checkout_url.");
        return;
      }
      window.location.href = safeCheckoutUrl;
    }, payLater ? "Đang tạo đơn hàng..." : "Đang tạo thanh toán...");
  }

  async function handleLoadOrderDetail(orderId) {
    await runAction(async () => {
      setOrderItems(normalizeList(await orderService.getOrderDetail(orderId, token)));
      setSelectedOrder(orders.find((order) => order.id === orderId) || { id: orderId });
    }, "Đang tải chi tiết đơn...");
  }

  async function handleSubmitAuth(event) {
    event.preventDefault();
    await runAction(async () => {
      const payload = {
        phone_number: authForm.phoneNumber.trim(),
        password: authForm.password,
      };

      if (activeTab === "register") {
        if (!/^[0-9]{10}$/.test(payload.phone_number)) {
          throw new Error("Số điện thoại phải gồm đúng 10 chữ số.");
        }
        if (authForm.password !== authForm.confirmPassword) {
          throw new Error("Mật khẩu nhập lại không khớp.");
        }
        await accountService.register(payload);
        setNotice("Đăng ký thành công. Bạn có thể đăng nhập ngay.");
        navigateTo("login");
        return;
      }

      const data = await accountService.login(payload);
      saveToken(data.accessToken);
      const profileData = await loadProfile(data.accessToken);
      const adminAllowed = await canAccessAdmin(data.accessToken, profileData);
      setHasAdminAccess(adminAllowed);
      setAuthForm({ phoneNumber: "", password: "", confirmPassword: "" });

      if (adminAllowed) {
        navigateTo("admin");
        await Promise.all([loadAdminDashboard(data.accessToken), loadAdminUsers(data.accessToken)]);
        return;
      }

      setNotice("Đăng nhập thành công.");
      navigateTo("shop");
    }, activeTab === "login" ? "Đang đăng nhập..." : "Đang đăng ký...");
  }

  async function handleLogout() {
    await runAction(async () => {
      if (token) await accountService.logout(token);
      clearSession();
      setNotice("Đã đăng xuất.");
      navigateTo("shop");
    }, "Đang đăng xuất...");
  }

  async function handleSubmitProfile(event) {
    event.preventDefault();
    await runAction(async () => {
      await accountService.updateInfo(profileForm, token);
      await loadProfile(token);
      setProfileEditing(false);
      setNotice("Đã cập nhật thông tin cá nhân.");
    }, "Đang cập nhật thông tin...");
  }

  async function handleSubmitPassword(event) {
    event.preventDefault();
    await runAction(async () => {
      await accountService.updatePassword(
        {
          phone_number: profile?.phone_number || profile?.phoneNumber || authForm.phoneNumber,
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword,
        },
        token,
      );
      setPasswordForm({ oldPassword: "", newPassword: "" });
      setNotice("Đã đổi mật khẩu.");
    }, "Đang đổi mật khẩu...");
  }

  async function handleCreateBook(event) {
    event.preventDefault();
    await runAction(async () => {
      await bookService.createBook(
        {
          ...bookForm,
          price: Number(bookForm.price),
          stock: Number(bookForm.stock),
          status: Number(bookForm.status),
          description: bookForm.content,
        },
        token,
      );
      setBookForm(emptyBookForm);
      await loadBooks();
      setNotice("Đã thêm sách mới.");
    }, "Đang thêm sách...");
  }

  async function handleImportBooks(event) {
    event.preventDefault();
    const form = event.currentTarget;
    if (!importFile) {
      setError("Vui lòng chọn file Excel.");
      return;
    }

    setImportResult(null);
    setImportDownloadError("");
    await runAction(async () => {
      const result = await bookService.importBooks(importFile, token);
      setImportResult(result);
      setImportFile(null);
      form.reset();
      await loadBooks();
      setNotice(result.failedCount ? `Đã thêm ${result.insertedCount} sách; ${result.failedCount} dòng lỗi.` : `Đã thêm ${result.insertedCount} sách thành công.`);
    }, "Đang import Excel...");
  }

  async function handleDownloadImportErrors() {
    if (!importResult?.errorFilePath) return;
    setImportDownloadError("");
    await runAction(async () => {
      try {
        const blob = await bookService.downloadImportErrors(importResult.errorFilePath, token);
        const signature = new Uint8Array(await blob.slice(0, 4).arrayBuffer());
        if (signature[0] !== 0x50 || signature[1] !== 0x4b || signature[2] !== 0x03 || signature[3] !== 0x04) {
          throw new Error("Server không trả về file Excel hợp lệ. Vui lòng thử lại hoặc kiểm tra backend.");
        }
        const objectUrl = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = objectUrl;
        link.download = importResult.errorFilePath.split(/[\\/]/).pop().split(/[?#]/)[0];
        document.body.append(link);
        link.click();
        link.remove();
        window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1000);
      } catch (error) {
        setImportDownloadError(getErrorMessage(error));
        throw error;
      }
    }, "Đang tải file lỗi...");
  }

  async function handleUpdateBook(event) {
    event.preventDefault();
    return runAction(async () => {
      await bookService.updateBook(
        updateBookForm.id,
        {
          content: updateBookForm.content,
          price: Number(updateBookForm.price),
          stock: Number(updateBookForm.stock),
          imageURL: updateBookForm.imageURL,
        },
        token,
      );
      setUpdateBookForm(emptyUpdateBookForm);
      await loadBooks();
      setNotice("Đã cập nhật sách.");
      return true;
    }, "Đang cập nhật sách...");
  }

  async function handleDeleteBook(bookId) {
    await runAction(async () => {
      await bookService.deleteBook(bookId, token);
      await loadBooks();
      setNotice("Đã xóa sách.");
    }, "Đang xóa sách...");
  }

  async function handleSearchUsers(event) {
    event?.preventDefault?.();
    const authToken = token || localStorage.getItem("bookshop_token") || "";
    await loadAdminUsers(authToken, userSearch);
  }

  async function handleSearchBestSellers(event) {
    event?.preventDefault?.();
    const authToken = token || localStorage.getItem("bookshop_token") || "";
    await loadBestSellers(authToken, bestSellerTop);
  }

  const showAccountPage = ["login", "register", "profile", "password"].includes(activeTab);

  return (
    <div className={isAdmin ? "app-shell admin-shell" : "app-shell"}>
      <header className="topbar">
        <button className="brand" type="button" onClick={() => navigateTo(isAdmin ? "admin" : "shop")}>
          <span className="brand-mark" aria-hidden="true">
            <svg viewBox="0 0 48 48" fill="none">
              <path d="M24 12c-5-4-11-5-18-3v25c7-2 13-1 18 3 5-4 11-5 18-3V9c-7-2-13-1-18 3Z" fill="white" stroke="white" strokeWidth="2" strokeLinejoin="round" />
              <path d="M24 12v25M10 14c5-1 9 0 12 2m4 0c3-2 7-3 12-2M10 20c5-1 9 0 12 2m4 0c3-2 7-3 12-2" stroke="#2563eb" strokeWidth="2" strokeLinecap="round" />
            </svg>
          </span>
          <span className="brand-name">BookShop<small>Mỗi trang sách, một hành trình</small></span>
        </button>

        <nav className="nav-tabs" aria-label="Điều hướng chính">
          <button className={activeTab === "shop" ? "active" : ""} type="button" onClick={() => navigateTo("shop")}>
            Cửa hàng
          </button>
          <button className={activeTab === "cart" ? "active" : ""} type="button" onClick={() => navigateTo("cart")}>
            Giỏ hàng <span>{cartCount}</span>
          </button>
          <button className={activeTab === "orders" ? "active" : ""} type="button" onClick={() => navigateTo("orders")}>
            Đơn hàng
          </button>
          <button
            className={activeTab === "guide" ? "active" : ""}
            type="button"
            aria-current={activeTab === "guide" ? "page" : undefined}
            onClick={() => navigateTo("guide")}
          >
            Hướng dẫn mua sách
          </button>
          <button
            className={activeTab === "about" ? "active" : ""}
            type="button"
            aria-current={activeTab === "about" ? "page" : undefined}
            onClick={() => navigateTo("about")}
          >
            Giới thiệu
          </button>
        </nav>

        {!isSignedIn ? (
          <div className="auth-actions">
            <button className="ghost-btn" type="button" onClick={() => navigateTo("login")}>
              Đăng nhập
            </button>
            <button className="primary-btn" type="button" onClick={() => navigateTo("register")}>
              Đăng ký
            </button>
          </div>
        ) : (
          <div className="account-menu">
            <button
              className="account-icon"
              type="button"
              aria-label="Tài khoản"
              onClick={() => setAccountMenuOpen((open) => !open)}
            >
              <span className="person-icon" aria-hidden="true" />
            </button>
            {accountMenuOpen ? (
              <div className="dropdown-menu">
                <strong>{getProfileName(profile) || "Tài khoản"}</strong>
                {isAdmin ? <button type="button" onClick={() => navigateTo("admin")}>Quản trị</button> : null}
                <button type="button" onClick={() => { setProfileEditing(false); navigateTo("profile"); }}>
                  Thông tin cá nhân
                </button>
                <button type="button" onClick={() => navigateTo("password")}>
                  Đổi mật khẩu
                </button>
                <button type="button" onClick={handleLogout}>
                  Đăng xuất
                </button>
              </div>
            ) : null}
          </div>
        )}
        <div className="header-banner" aria-label="Khám phá sách cùng BookShop">
          <span className="header-banner-art" aria-hidden="true"><i /><i /><i /></span>
          <span><strong>Khám phá câu chuyện tiếp theo</strong> · Chọn một cuốn sách, mở thêm một thế giới.</span>
        </div>
      </header>

      <main>
        <StatusMessages busy={busy} notice={notice} error={error} />

        {isAdmin && activeTab === "admin" ? (
          <AdminPage
            isSignedIn={isSignedIn}
            month={adminMonth}
            revenue={revenue}
            revenueLoading={revenueLoading}
            revenueError={revenueError}
            bestSellers={bestSellers}
            bestSellerTop={bestSellerTop}
            users={users}
            userTotal={userTotal}
            userSearch={userSearch}
            books={books}
            bookForm={bookForm}
            updateBookForm={updateBookForm}
            importFile={importFile}
            importResult={importResult}
            importDownloadError={importDownloadError}
            onMonthChange={setAdminMonth}
            onLoadRevenue={() => loadRevenue()}
            onBestSellerTopChange={setBestSellerTop}
            onSearchBestSellers={handleSearchBestSellers}
            onUserSearchChange={setUserSearch}
            onSearchUsers={handleSearchUsers}
            onBookFormChange={setBookForm}
            onUpdateBookFormChange={setUpdateBookForm}
            onImportFileChange={(file) => { setImportFile(file); setImportResult(null); }}
            onCreateBook={handleCreateBook}
            onImportBooks={handleImportBooks}
            onDownloadImportErrors={handleDownloadImportErrors}
            onUpdateBook={handleUpdateBook}
            onDeleteBook={handleDeleteBook}
          />
        ) : null}

        {activeTab === "shop" ? (
          <BookCatalog
            books={visibleShopBooks}
            recommendedBestSellers={recommendedBestSellers}
            loadingRecommendations={loadingRecommendations}
            recommendationsError={recommendationsError}
            filters={filters}
            appliedFilters={appliedFilters}
            categories={categories}
            loading={loadingBooks}
            searchOpen={searchOpen}
            selectedBook={selectedBook}
            onToggleSearch={() => setSearchOpen((open) => !open)}
            onFilterChange={setFilters}
            onSelectCategory={handleSelectCategory}
            onSearch={handleSearch}
            onResetFilters={handleResetFilters}
            onViewDetail={handleViewDetail}
            onCloseDetail={() => setSelectedBook(null)}
            onAddToCart={handleAddToCart}
          />
        ) : null}

        {activeTab === "guide" ? (
          <BuyingGuidePage isSignedIn={isSignedIn} onNavigate={navigateTo} />
        ) : null}

        {activeTab === "about" ? <AboutPage onNavigate={navigateTo} /> : null}

        {activeTab === "cart" ? (
          <CartPage
            isSignedIn={isSignedIn}
            cart={cart}
            books={books}
            checkout={checkout}
            checkoutOpen={checkoutOpen}
            orderForm={orderForm}
            onOrderFormChange={setOrderForm}
            onReloadCart={() => runAction(async () => { await loadCart(); setCheckoutOpen(false); }, "Đang tải giỏ...")}
            onStartCheckout={handleStartCheckout}
            onCloseCheckout={() => setCheckoutOpen(false)}
            onUpdateItem={handleUpdateCartItem}
            onRemoveItem={handleRemoveCartItem}
            onCreatePayment={handleCreatePayment}
            isProcessing={Boolean(busy)}
          />
        ) : null}

        {activeTab === "orders" ? (
          <OrdersPage
            isSignedIn={isSignedIn}
            orders={orders}
            orderItems={orderItems}
            books={books}
            selectedOrder={selectedOrder}
            onReloadOrders={() => runAction(loadOrders, "Đang tải đơn...")}
            onLoadOrderDetail={handleLoadOrderDetail}
            onCloseOrderDetail={() => { setOrderItems([]); setSelectedOrder(null); }}
          />
        ) : null}

        {showAccountPage ? (
          <AccountPage
            mode={activeTab}
            authForm={authForm}
            profile={profile}
            profileForm={profileForm}
            profileEditing={profileEditing}
            passwordForm={passwordForm}
            onEditProfile={() => setProfileEditing(true)}
            onCancelProfile={() => setProfileEditing(false)}
            onAuthFormChange={setAuthForm}
            onProfileFormChange={setProfileForm}
            onPasswordFormChange={setPasswordForm}
            onSubmitAuth={handleSubmitAuth}
            onSubmitProfile={handleSubmitProfile}
            onSubmitPassword={handleSubmitPassword}
          />
        ) : null}
      </main>
    </div>
  );
}
