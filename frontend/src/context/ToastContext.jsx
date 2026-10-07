import { createContext, useState, useContext, useCallback } from 'react';
import './Toast.css';

const ToastContext = createContext();

export const useToast = () => useContext(ToastContext);

export const ToastProvider = ({ children }) => {
  const [toast, setToast] = useState({ message: '', visible: false, type: 'success' });

  const showToast = useCallback((message, type = 'success') => {
    setToast({ message, visible: true, type });
    setTimeout(() => {
      setToast(prev => ({ ...prev, visible: false }));
    }, 3000);
  }, []);

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div className={`toast-notification ${toast.visible ? 'show' : ''} toast-${toast.type}`}>
        <div className="toast-icon">
           {toast.type === 'success' ? '✓' : '!'}
        </div>
        <div className="toast-message">{toast.message}</div>
      </div>
    </ToastContext.Provider>
  );
};
