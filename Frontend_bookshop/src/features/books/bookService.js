import { apiFetch, toQuery } from "../../shared/api/httpClient";

export const bookService = {
  searchBooks(filters = {}) {
    return apiFetch(`/api/books/search${toQuery(filters)}`);
  },

  getBestSellers(top = 10) {
    return apiFetch(`/api/books/best-selling-${top}`);
  },

  getBook(bookId) {
    return apiFetch(`/api/book-${bookId}`);
  },

  createBook(payload, token) {
    return apiFetch("/api/book", {
      method: "POST",
      token,
      body: payload,
    });
  },

  importBooks(file, token) {
    const formData = new FormData();
    formData.append("file", file);

    return apiFetch("/api/books", {
      method: "POST",
      token,
      body: formData,
    });
  },

  downloadImportErrors(errorFilePath, token) {
    const fileName = errorFilePath.split(/[\\/]/).pop();
    return apiFetch(`/api/books/import-errors/${encodeURIComponent(fileName)}`, {
      token,
      responseType: "blob",
    });
  },

  updateBook(bookId, payload, token) {
    return apiFetch(`/api/book-${bookId}`, {
      method: "PATCH",
      token,
      body: payload,
    });
  },

  deleteBook(bookId, token) {
    return apiFetch(`/api/book-${bookId}`, {
      method: "DELETE",
      token,
    });
  },
};
