import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import http from "node:http";

function adminGetBodyProxy() {
  function registerMiddleware(server) {
    server.middlewares.use("/__bookshop/admin-users", (req, res) => {
      if (req.method !== "POST") {
        res.statusCode = 405;
        res.setHeader("Content-Type", "application/json");
        res.end(JSON.stringify({ message: "Method not allowed" }));
        return;
      }

      let rawBody = "";
      req.on("data", (chunk) => {
        rawBody += chunk;
      });

      req.on("end", () => {
        let payload = {};
        try {
          payload = rawBody ? JSON.parse(rawBody) : {};
        } catch {
          payload = {};
        }

        const rawRoleId = payload.role_id ?? payload.roleId;
        const hasRoleId = rawRoleId !== undefined && rawRoleId !== null && String(rawRoleId).trim() !== "";
        const roleId = hasRoleId ? Number(rawRoleId) : Number.NaN;
        const query = new URLSearchParams();
        if (payload.address) {
          query.set("address", payload.address);
        }
        if (hasRoleId && Number.isFinite(roleId)) {
          query.set("role_id", String(roleId));
        }
        const path = `/api/admin/dashboard/user${query.toString() ? `?${query.toString()}` : ""}`;

        const backendRequest = http.request(
          {
            hostname: "localhost",
            port: 8082,
            path,
            method: "GET",
            headers: {
              "Content-Type": "application/json",
              ...(req.headers.authorization ? { Authorization: req.headers.authorization } : {}),
            },
          },
          (backendResponse) => {
            res.statusCode = backendResponse.statusCode || 500;
            Object.entries(backendResponse.headers).forEach(([key, value]) => {
              if (value !== undefined) {
                res.setHeader(key, value);
              }
            });
            backendResponse.pipe(res);
          }
        );

        backendRequest.on("error", () => {
          res.statusCode = 502;
          res.setHeader("Content-Type", "application/json");
          res.end(JSON.stringify({ message: "Không kết nối được backend ở localhost:8082" }));
        });

        backendRequest.end();
      });
    });
  }

  return {
    name: "bookshop-admin-get-body-proxy",
    configureServer(server) {
      registerMiddleware(server);
    },
    configurePreviewServer(server) {
      registerMiddleware(server);
    },
  };
}

export default defineConfig({
  plugins: [react(), adminGetBodyProxy()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8082",
        changeOrigin: true,
      },
      "/payment": {
        target: "http://localhost:8082",
        changeOrigin: true,
      },
      "/payouts": {
        target: "http://localhost:8082",
        changeOrigin: true,
      },
    },
  },
});
