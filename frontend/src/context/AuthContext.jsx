import { createContext, useCallback, useEffect, useMemo, useState } from 'react';
import { TOKEN_KEY } from '../lib/api';

export const AuthContext = createContext(null);

/** Reads the user from the JWT payload. Returns null for missing, malformed or expired tokens. */
function decodeToken(token) {
  if (!token) return null;
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const bytes = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0));
    const payload = JSON.parse(new TextDecoder().decode(bytes));
    if (!payload.exp || payload.exp * 1000 < Date.now()) return null;
    return {
      id: payload.sub,
      name: payload.name,
      email: payload.email,
      mobile: payload.mobile,
      role: payload.role,
    };
  } catch {
    return null;
  }
}

function readStoredToken() {
  try {
    const token = localStorage.getItem(TOKEN_KEY);
    return decodeToken(token) ? token : null;
  } catch {
    return null;
  }
}

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(readStoredToken);

  useEffect(() => {
    try {
      if (token) {
        localStorage.setItem(TOKEN_KEY, token);
      } else {
        localStorage.removeItem(TOKEN_KEY);
      }
    } catch {
      // storage unavailable (private mode); the session still works until reload
    }
  }, [token]);

  const login = useCallback((jwt) => setToken(jwt), []);
  const logout = useCallback(() => setToken(null), []);

  // Any API call answered with 401 means the token is no longer valid
  useEffect(() => {
    window.addEventListener('auth:expired', logout);
    return () => window.removeEventListener('auth:expired', logout);
  }, [logout]);

  const value = useMemo(() => {
    const user = decodeToken(token);
    return {
      token: user ? token : null,
      user,
      isAdmin: user?.role === 'ADMIN',
      login,
      logout,
    };
  }, [token, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
