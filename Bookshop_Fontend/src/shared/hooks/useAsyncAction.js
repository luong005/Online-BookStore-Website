import { useState } from "react";
import { getErrorMessage } from "../utils/formatters";

export function useAsyncAction() {
  const [busy, setBusy] = useState("");
  const [notice, setNotice] = useState("");
  const [error, setError] = useState("");

  async function runAction(action, label = "Đang xử lý...") {
    setBusy(label);
    setNotice("");
    setError("");

    try {
      return await action();
    } catch (err) {
      setError(getErrorMessage(err));
      return null;
    } finally {
      setBusy("");
    }
  }

  return {
    busy,
    notice,
    error,
    setNotice,
    setError,
    runAction,
  };
}
