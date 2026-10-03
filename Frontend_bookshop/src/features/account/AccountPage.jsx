export function AccountPage({
  mode,
  authForm,
  profile,
  profileForm,
  profileEditing,
  passwordForm,
  onEditProfile,
  onCancelProfile,
  onAuthFormChange,
  onProfileFormChange,
  onPasswordFormChange,
  onSubmitAuth,
  onSubmitProfile,
  onSubmitPassword,
}) {
  if (mode === "login" || mode === "register") {
    return (
      <section className="auth-page">
        <div className="panel auth-panel">
          <h1>{mode === "login" ? "Đăng nhập" : "Đăng ký"}</h1>
          <form className="stack-form" onSubmit={onSubmitAuth}>
            <label>
              Số điện thoại
              <input
                required
                value={authForm.phoneNumber}
                onChange={(event) => onAuthFormChange({ ...authForm, phoneNumber: event.target.value })}
              />
            </label>
            <label>
              Mật khẩu
              <input
                required
                minLength="6"
                type="password"
                value={authForm.password}
                onChange={(event) => onAuthFormChange({ ...authForm, password: event.target.value })}
              />
            </label>
            {mode === "register" ? (
              <label>
                Nhập lại mật khẩu
                <input
                  required
                  minLength="6"
                  type="password"
                  value={authForm.confirmPassword}
                  onChange={(event) =>
                    onAuthFormChange({ ...authForm, confirmPassword: event.target.value })
                  }
                />
              </label>
            ) : null}
            <button className="primary-btn" type="submit">
              {mode === "login" ? "Đăng nhập" : "Tạo tài khoản"}
            </button>
          </form>
        </div>
      </section>
    );
  }

  if (mode === "password") {
    return (
      <section className="auth-page">
        <div className="panel auth-panel">
          <h1>Đổi mật khẩu</h1>
          <form className="stack-form" onSubmit={onSubmitPassword}>
            <label>
              Mật khẩu cũ
              <input
                required
                type="password"
                value={passwordForm.oldPassword}
                onChange={(event) =>
                  onPasswordFormChange({ ...passwordForm, oldPassword: event.target.value })
                }
              />
            </label>
            <label>
              Mật khẩu mới
              <input
                required
                minLength="6"
                type="password"
                value={passwordForm.newPassword}
                onChange={(event) =>
                  onPasswordFormChange({ ...passwordForm, newPassword: event.target.value })
                }
              />
            </label>
            <button className="primary-btn" type="submit">
              Đổi mật khẩu
            </button>
          </form>
        </div>
      </section>
    );
  }

  return (
    <section className="auth-page">
      <div className="panel profile-panel">
        <h1>{profile?.fullname || profile?.fullName || "Thông tin cá nhân"}</h1>
        <div className="profile-readonly">
          <span>Số điện thoại</span>
          <strong>{profile?.phone_number || profile?.phoneNumber || "Chưa có"}</strong>
          <span>Địa chỉ</span>
          <strong>{profile?.address || "Chưa cập nhật"}</strong>
          <span>Ngày sinh</span>
          <strong>{profileForm.date_of_birth || "Chưa cập nhật"}</strong>
        </div>

        {!profileEditing ? (
          <button className="primary-btn" type="button" onClick={onEditProfile}>
            Cập nhật
          </button>
        ) : (
          <form className="stack-form" onSubmit={onSubmitProfile}>
            <label>
              Họ tên
              <input
                value={profileForm.fullname}
                onChange={(event) => onProfileFormChange({ ...profileForm, fullname: event.target.value })}
              />
            </label>
            <label>
              Ngày sinh
              <input
                type="date"
                value={profileForm.date_of_birth}
                onChange={(event) =>
                  onProfileFormChange({ ...profileForm, date_of_birth: event.target.value })
                }
              />
            </label>
            <label>
              Địa chỉ
              <textarea
                rows="3"
                value={profileForm.address}
                onChange={(event) => onProfileFormChange({ ...profileForm, address: event.target.value })}
              />
            </label>
            <div className="form-actions">
              <button className="primary-btn" type="submit">
                Lưu thay đổi
              </button>
              <button className="ghost-btn" type="button" onClick={onCancelProfile}>
                Hủy
              </button>
            </div>
          </form>
        )}
      </div>
    </section>
  );
}
