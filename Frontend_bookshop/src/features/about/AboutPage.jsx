const features = [
  {
    title: "Khám phá sách theo sở thích",
    description: "Duyệt sách theo danh mục, tìm theo tên và lọc theo khoảng giá. Xem nội dung giới thiệu, tác giả, nhà xuất bản và số lượng còn lại trước khi lựa chọn.",
  },
  {
    title: "Mua sách ngay trên website",
    description: "Thêm sách vào giỏ, điều chỉnh số lượng và kiểm tra tổng tiền. Nhập thông tin nhận hàng rồi tiếp tục đến trang thanh toán để hoàn tất việc mua sách.",
  },
  {
    title: "Quản lý tài khoản và đơn hàng",
    description: "Cập nhật thông tin cá nhân, xem lại các đơn đã mua, kiểm tra chi tiết sách trong từng đơn và theo dõi trạng thái thanh toán trong tài khoản của bạn.",
  },
];

export function AboutPage({ onNavigate }) {
  return (
    <section className="about-page" aria-labelledby="about-title">
      <header className="about-hero">
        <div>
          <span className="about-eyebrow">Không gian dành cho người yêu sách</span>
          <h1 id="about-title">Giới thiệu BookShop</h1>
          <p>
            BookShop là website mua sách trực tuyến, giúp bạn tìm kiếm và lựa chọn
            những cuốn sách phù hợp với sở thích, nhu cầu học tập và khám phá của mình.
          </p>
        </div>
        <div className="about-reading-note">
          <svg viewBox="0 0 64 64" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M32 17C24 11 14 10 5 13v38c9-3 19-2 27 4 8-6 18-7 27-4V13c-9-3-19-2-27 4Z" />
            <path d="M32 17v38M13 23c4-1 8 0 12 2M13 32c4-1 8 0 12 2M39 25c4-2 8-3 12-2M39 34c4-2 8-3 12-2" />
          </svg>
          <p>Mỗi cuốn sách,<br /><strong>một góc nhìn mới.</strong></p>
        </div>
      </header>

      <section className="about-story" aria-labelledby="about-purpose-title">
        <div>
          <h2 id="about-purpose-title">Đưa việc chọn sách đến gần bạn hơn</h2>
          <p>
            Một cuốn sách có thể mở ra kiến thức mới, mang đến cảm hứng hoặc đơn giản
            là một khoảng thời gian thư giãn. BookShop hướng đến việc giúp bạn tìm
            được cuốn sách ấy bằng một trải nghiệm mua sắm dễ sử dụng.
          </p>
          <p>
            Bạn có thể tự do xem và tìm kiếm sách trước khi đăng nhập. Khi muốn mua,
            tài khoản cá nhân giúp bạn quản lý giỏ hàng và xem lại thông tin đơn hàng
            ngay trên website.
          </p>
        </div>
        <aside className="about-values" aria-labelledby="about-values-title">
          <h2 id="about-values-title">Trải nghiệm dành cho bạn</h2>
          <ul>
            <li>Dễ tìm sách theo chủ đề và khoảng giá mong muốn.</li>
            <li>Xem thông tin sách trước khi quyết định mua.</li>
            <li>Kiểm tra giỏ hàng và tổng tiền trước khi thanh toán.</li>
          </ul>
        </aside>
      </section>

      <section aria-labelledby="about-features-title">
        <h2 id="about-features-title">Bạn có thể làm gì tại BookShop?</h2>
        <div className="about-features">
          {features.map(({ title, description }) => (
            <article className="about-feature" key={title}>
              <h3>{title}</h3>
              <p>{description}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="about-start" aria-labelledby="about-start-title">
        <div>
          <h2 id="about-start-title">Tìm cuốn sách tiếp theo của bạn</h2>
          <p>Ghé cửa hàng để khám phá sách, hoặc xem hướng dẫn nếu đây là lần đầu bạn mua tại BookShop.</p>
        </div>
        <div className="form-actions">
          <button className="primary-btn" type="button" onClick={() => onNavigate("shop")}>
            Khám phá sách
          </button>
          <button className="ghost-btn" type="button" onClick={() => onNavigate("guide")}>
            Hướng dẫn mua sách
          </button>
        </div>
      </section>
    </section>
  );
}
