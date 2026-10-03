const buyingSteps = [
  {
    title: "Đăng nhập hoặc tạo tài khoản",
    content: <>Chọn <strong>Đăng nhập</strong> và nhập số điện thoại, mật khẩu. Nếu chưa có tài khoản, chọn <strong>Đăng ký</strong> để tạo tài khoản trước khi thêm sách vào giỏ.</>,
  },
  {
    title: "Tìm cuốn sách yêu thích",
    content: <>Vào <strong>Cửa hàng</strong>, chọn danh mục hoặc nhập tên sách rồi nhấn <strong>Tìm kiếm</strong>. Dùng <strong>Bộ lọc</strong> để chọn khoảng giá; nhấn <strong>Xóa bộ lọc</strong> khi muốn xem lại tất cả sách.</>,
  },
  {
    title: "Xem sách và thêm vào giỏ",
    content: <>Nhấn <strong>Chi tiết</strong> để xem nội dung, tác giả, giá và số lượng còn lại. Chọn <strong>Thêm giỏ</strong> trên thẻ sách hoặc <strong>Thêm vào giỏ hàng</strong> trong phần chi tiết.</>,
  },
  {
    title: "Kiểm tra giỏ hàng",
    content: <>Mở <strong>Giỏ hàng</strong>, điều chỉnh số lượng hoặc nhấn <strong>Xóa</strong> với sách không muốn mua. Khi đã chọn xong, nhấn <strong>Mua</strong> để xem lại các sách và tổng tiền cần thanh toán.</>,
  },
  {
    title: "Nhập thông tin và thanh toán",
    content: <>Điền đầy đủ <strong>Địa chỉ nhận hàng</strong> và <strong>Số điện thoại</strong>. Chọn <strong>Thanh toán ngay</strong> để mở trang thanh toán, hoặc <strong>Thanh toán sau</strong> để tạo đơn và xem trong mục Đơn hàng.</>,
  },
  {
    title: "Xem lại đơn hàng",
    content: <>Mở <strong>Đơn hàng</strong> để kiểm tra đơn đã tạo cùng trạng thái thanh toán. Nhấn <strong>Chi tiết</strong> để xem sách trong đơn hoặc <strong>Tải lại</strong> để cập nhật thông tin.</>,
  },
];

const commonQuestions = [
  {
    question: "Tôi có cần đăng nhập để mua sách không?",
    answer: "Bạn có thể xem và tìm kiếm sách khi chưa đăng nhập. Để thêm sách vào giỏ, thanh toán và xem đơn hàng, bạn cần đăng nhập vào tài khoản.",
  },
  {
    question: "Tôi có thể thay đổi sách trước khi thanh toán không?",
    answer: "Có. Trong Giỏ hàng, bạn có thể đổi số lượng hoặc xóa sách. Nếu đang ở bước nhập thông tin nhận hàng, nhấn Đóng để quay lại chỉnh giỏ rồi nhấn Mua để kiểm tra tổng tiền mới.",
  },
  {
    question: "Thanh toán xong nhưng chưa thấy thông tin đơn hàng thì làm gì?",
    answer: "Mở mục Đơn hàng và nhấn Tải lại. Kiểm tra danh sách đơn và trạng thái thanh toán trước khi thực hiện thanh toán thêm lần nữa.",
  },
];

export function BuyingGuidePage({ isSignedIn, onNavigate }) {
  return (
    <section className="buying-guide-page" aria-labelledby="buying-guide-title">
      <header className="buying-guide-intro">
        <div>
          <span className="buying-guide-eyebrow">Mua sách cùng BookShop</span>
          <h1 id="buying-guide-title">Hướng dẫn mua sách</h1>
          <p>Từ chọn cuốn sách yêu thích đến hoàn tất thanh toán, cùng bắt đầu với 6 bước đơn giản dưới đây.</p>
        </div>
        <div className="form-actions">
          <button className="primary-btn" type="button" onClick={() => onNavigate("shop")}>
            Bắt đầu chọn sách
          </button>
          <button className="ghost-btn" type="button" onClick={() => onNavigate(isSignedIn ? "cart" : "login")}>
            {isSignedIn ? "Xem giỏ hàng" : "Đăng nhập"}
          </button>
        </div>
      </header>

      <ol className="buying-guide-steps" aria-label="Các bước mua sách">
        {buyingSteps.map((step, index) => (
          <li className="buying-guide-step" key={step.title}>
            <span className="buying-guide-number" aria-hidden="true">{String(index + 1).padStart(2, "0")}</span>
            <h2>{step.title}</h2>
            <p>{step.content}</p>
          </li>
        ))}
      </ol>

      <div className="buying-guide-help">
        <section className="buying-guide-faq" aria-labelledby="buying-guide-faq-title">
          <h2 id="buying-guide-faq-title">Câu hỏi thường gặp</h2>
          {commonQuestions.map(({ question, answer }) => (
            <details key={question}>
              <summary>{question}</summary>
              <p>{answer}</p>
            </details>
          ))}
        </section>

        <aside className="buying-guide-note" aria-labelledby="buying-guide-note-title">
          <h2 id="buying-guide-note-title">Trước khi thanh toán</h2>
          <ul>
            <li>Kiểm tra tên sách, số lượng và tổng tiền.</li>
            <li>Ghi rõ số nhà, tên đường, phường/xã và tỉnh/thành phố.</li>
            <li>Dùng số điện thoại có thể liên lạc khi nhận hàng.</li>
          </ul>
          {!isSignedIn ? (
            <button className="text-btn" type="button" onClick={() => onNavigate("register")}>
              Chưa có tài khoản? Đăng ký
            </button>
          ) : null}
        </aside>
      </div>
    </section>
  );
}
