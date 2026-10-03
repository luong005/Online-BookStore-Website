export function StatusMessages({ busy, notice, error }) {
  if (!busy && !notice && !error) return null;

  return (
    <section className="message-stack" aria-live="polite">
      {busy ? <div className="message loading">{busy}</div> : null}
      {notice ? <div className="message success">{notice}</div> : null}
      {error ? <div className="message error">{error}</div> : null}
    </section>
  );
}
