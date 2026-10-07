import { useContext, useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { GoogleLogin, GoogleOAuthProvider } from '@react-oauth/google';
import { AuthContext } from '../context/AuthContext';
import { ThemeContext } from '../context/ThemeContext';
import { useToast } from '../context/ToastContext';
import { errorMessage, userApi } from '../lib/api';
import { firstName } from '../lib/format';
import { BRAND_NAME } from '../config';
import './Auth.css';

const EMPTY_FORM = { identifier: '', password: '', name: '', email: '', otp: '', newPassword: '' };

const TEXT = {
  signin: { title: 'Welcome back', subtitle: 'Sign in to check out faster and track your orders.' },
  register: { title: 'Create your account', subtitle: `Join ${BRAND_NAME} in under a minute.` },
  reset: { title: 'Reset your password', subtitle: 'We will send a code to your email or mobile number.' },
};

const Auth = () => {
  const { token, login } = useContext(AuthContext);
  const { isDarkMode } = useContext(ThemeContext);
  const { showToast } = useToast();
  const location = useLocation();
  const from = location.state?.from || '/';

  const [options, setOptions] = useState(null);
  const [view, setView] = useState('signin'); // signin | register | reset
  const [method, setMethod] = useState('password'); // password | otp (sign in only)
  const [step, setStep] = useState('form'); // form | code
  const [form, setForm] = useState(EMPTY_FORM);
  const [codeInfo, setCodeInfo] = useState(null);
  const [resendIn, setResendIn] = useState(0);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    userApi
      .get('/api/auth/options')
      .then((response) => setOptions(response.data))
      .catch(() => setOptions({ googleClientId: null, emailOtp: true, smsOtp: true, demoMode: false }));
  }, []);

  useEffect(() => {
    if (resendIn <= 0) return undefined;
    const timer = setTimeout(() => setResendIn((seconds) => seconds - 1), 1000);
    return () => clearTimeout(timer);
  }, [resendIn]);

  if (token) {
    return <Navigate to={from} replace />;
  }

  const setField = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const switchView = (next) => {
    setView(next);
    setStep('form');
    setError('');
    setCodeInfo(null);
    setForm((f) => ({ ...f, otp: '', password: '', newPassword: '' }));
  };

  const finish = (data) => {
    showToast(`Welcome, ${firstName(data.user.name)}!`, 'success');
    login(data.token);
  };

  // Wraps a submit handler with loading and error handling
  const run = (action) => async (e) => {
    e?.preventDefault();
    setError('');
    setBusy(true);
    try {
      await action();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const sendCode = async (identifier) => {
    const { data } = await userApi.post('/api/auth/otp/request', { identifier });
    setCodeInfo(data);
    setResendIn(data.resendAfterSeconds || 30);
    setForm((f) => ({ ...f, otp: '' }));
    return data;
  };

  const passwordLogin = run(async () => {
    const { data } = await userApi.post('/api/auth/login', { identifier: form.identifier, password: form.password });
    finish(data);
  });

  const startOtpLogin = run(async () => {
    await sendCode(form.identifier);
    setStep('code');
  });

  const startRegister = run(async () => {
    const data = await sendCode(form.email);
    if (!data.newUser) {
      setError('An account with this email already exists. Please sign in instead.');
      return;
    }
    setStep('code');
  });

  const startReset = run(async () => {
    const data = await sendCode(form.identifier);
    if (data.newUser) {
      setError("We couldn't find an account with that email or mobile number.");
      return;
    }
    setStep('code');
  });

  const verifyCode = run(async () => {
    let response;
    if (view === 'register') {
      response = await userApi.post('/api/auth/register', {
        name: form.name,
        email: form.email,
        password: form.password,
        otp: form.otp,
      });
    } else if (view === 'reset') {
      response = await userApi.post('/api/auth/password/reset', {
        identifier: form.identifier,
        otp: form.otp,
        newPassword: form.newPassword,
      });
    } else {
      response = await userApi.post('/api/auth/otp/verify', {
        identifier: form.identifier,
        otp: form.otp,
        name: codeInfo?.newUser ? form.name : null,
      });
    }
    finish(response.data);
  });

  const resendCode = run(async () => {
    await sendCode(view === 'register' ? form.email : form.identifier);
    showToast('A new code is on its way', 'success');
  });

  const googleLogin = (credentialResponse) =>
    run(async () => {
      const { data } = await userApi.post('/api/auth/google', { credential: credentialResponse.credential });
      finish(data);
    })();

  const text = step === 'code'
    ? { title: "Verify it's you", subtitle: `Enter the 6-digit code sent to ${codeInfo?.sentTo}.` }
    : TEXT[view];

  const googleClientId = options?.googleClientId;
  const showGoogle = Boolean(googleClientId) && view !== 'reset' && step === 'form';

  return (
    <div className="auth-page">
      <div className="auth-container glass-panel animate-fade-in-up">
        <h2>{text.title}</h2>
        <p className="auth-subtitle">{text.subtitle}</p>

        {view !== 'reset' && step === 'form' && (
          <div className="auth-tabs" role="tablist">
            <button role="tab" aria-selected={view === 'signin'} className={view === 'signin' ? 'active' : ''} onClick={() => switchView('signin')}>
              Sign in
            </button>
            <button role="tab" aria-selected={view === 'register'} className={view === 'register' ? 'active' : ''} onClick={() => switchView('register')}>
              Create account
            </button>
          </div>
        )}

        {error && <div className="error-alert" role="alert">{error}</div>}

        {step === 'code' && codeInfo?.demoCode && (
          <div className="demo-code" role="status">
            Demo mode: your code is <strong>{codeInfo.demoCode}</strong>
          </div>
        )}

        {showGoogle && (
          <>
            <GoogleOAuthProvider clientId={googleClientId}>
              <div className="google-btn">
                <GoogleLogin
                  onSuccess={googleLogin}
                  onError={() => setError('Google sign-in did not complete. Please try again.')}
                  theme={isDarkMode ? 'filled_black' : 'outline'}
                  size="large"
                  shape="pill"
                  text={view === 'register' ? 'signup_with' : 'continue_with'}
                  width="320"
                />
              </div>
            </GoogleOAuthProvider>
            <div className="auth-divider"><span>or</span></div>
          </>
        )}

        {step === 'code' ? (
          <form onSubmit={verifyCode} className="auth-form">
            <div className="input-group">
              <label htmlFor="otp">Verification code</label>
              <input
                id="otp"
                name="otp"
                className="otp-input"
                value={form.otp}
                onChange={(e) => setForm({ ...form, otp: e.target.value.replace(/\D/g, '').slice(0, 6) })}
                inputMode="numeric"
                autoComplete="one-time-code"
                pattern="[0-9]{6}"
                maxLength={6}
                placeholder="------"
                autoFocus
                required
              />
            </div>

            {view === 'signin' && codeInfo?.newUser && (
              <div className="input-group">
                <label htmlFor="name">Your name</label>
                <input id="name" name="name" value={form.name} onChange={setField} maxLength={80} autoComplete="name" placeholder="So we know what to call you" required />
              </div>
            )}

            {view === 'reset' && (
              <div className="input-group">
                <label htmlFor="newPassword">New password</label>
                <input id="newPassword" name="newPassword" type="password" value={form.newPassword} onChange={setField} minLength={8} maxLength={72} autoComplete="new-password" placeholder="At least 8 characters" required />
              </div>
            )}

            <button type="submit" className="btn btn-primary auth-submit" disabled={busy}>
              {busy ? 'Please wait...' : view === 'register' ? 'Create account' : view === 'reset' ? 'Set password and sign in' : 'Verify and sign in'}
            </button>

            <div className="auth-links">
              <button type="button" className="switch-btn" onClick={resendCode} disabled={resendIn > 0 || busy}>
                {resendIn > 0 ? `Resend code in ${resendIn}s` : 'Resend code'}
              </button>
              <button type="button" className="switch-btn" onClick={() => { setStep('form'); setError(''); }}>
                Change {view === 'register' ? 'email' : 'email/number'}
              </button>
            </div>
          </form>
        ) : view === 'signin' ? (
          <>
            <div className="method-toggle" role="radiogroup" aria-label="Sign-in method">
              <button type="button" role="radio" aria-checked={method === 'password'} className={method === 'password' ? 'active' : ''} onClick={() => { setMethod('password'); setError(''); }}>
                Password
              </button>
              <button type="button" role="radio" aria-checked={method === 'otp'} className={method === 'otp' ? 'active' : ''} onClick={() => { setMethod('otp'); setError(''); }}>
                One-time code
              </button>
            </div>

            <form onSubmit={method === 'password' ? passwordLogin : startOtpLogin} className="auth-form">
              <div className="input-group">
                <label htmlFor="identifier">Email or mobile number</label>
                <input id="identifier" name="identifier" value={form.identifier} onChange={setField} autoComplete="username" placeholder="you@example.com or 98765 43210" required />
              </div>

              {method === 'password' ? (
                <div className="input-group">
                  <div className="label-row">
                    <label htmlFor="password">Password</label>
                    <button type="button" className="switch-btn small" onClick={() => switchView('reset')}>Forgot password?</button>
                  </div>
                  <input id="password" name="password" type="password" value={form.password} onChange={setField} autoComplete="current-password" required />
                </div>
              ) : (
                <p className="auth-hint">
                  We will send a 6-digit code to your {options?.smsOtp === false ? 'email' : 'email or phone'}. New here? Your account is created automatically.
                </p>
              )}

              <button type="submit" className="btn btn-primary auth-submit" disabled={busy}>
                {busy ? 'Please wait...' : method === 'password' ? 'Sign in' : 'Send code'}
              </button>
            </form>
          </>
        ) : view === 'register' ? (
          <form onSubmit={startRegister} className="auth-form">
            <div className="input-group">
              <label htmlFor="name">Full name</label>
              <input id="name" name="name" value={form.name} onChange={setField} maxLength={80} autoComplete="name" required />
            </div>
            <div className="input-group">
              <label htmlFor="email">Email</label>
              <input id="email" name="email" type="email" value={form.email} onChange={setField} autoComplete="email" required />
            </div>
            <div className="input-group">
              <label htmlFor="password">Password</label>
              <input id="password" name="password" type="password" value={form.password} onChange={setField} minLength={8} maxLength={72} autoComplete="new-password" placeholder="At least 8 characters" required />
            </div>
            <p className="auth-hint">We will email you a code to confirm the address. Prefer your phone? Use Sign in with a one-time code.</p>
            <button type="submit" className="btn btn-primary auth-submit" disabled={busy}>
              {busy ? 'Please wait...' : 'Continue'}
            </button>
          </form>
        ) : (
          <form onSubmit={startReset} className="auth-form">
            <div className="input-group">
              <label htmlFor="identifier">Email or mobile number</label>
              <input id="identifier" name="identifier" value={form.identifier} onChange={setField} autoComplete="username" required />
            </div>
            <button type="submit" className="btn btn-primary auth-submit" disabled={busy}>
              {busy ? 'Please wait...' : 'Send code'}
            </button>
            <div className="auth-links">
              <button type="button" className="switch-btn" onClick={() => switchView('signin')}>Back to sign in</button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default Auth;
