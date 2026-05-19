import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { verify, resendCode } from '../api/auth';
import './css/AuthPage.css';

const VerificationPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const email: string = (location.state as any)?.email || '';

  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [resent, setResent] = useState(false);

  const handleCheck = async () => {
    if (!code.trim()) {
      setError('Please enter the verification code.');
      return;
    }
    setError('');
    setLoading(true);
    try {
      await verify({ email, code });
      navigate('/login');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid code. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setResent(false);
    setError('');
    try {
      await resendCode({ email });
      setResent(true);
      setTimeout(() => setResent(false), 4000);
    } catch {
      setError('Could not resend code. Please try again.');
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleCheck();
  };

  return (
    <div className="auth-bg">
      <Link to="/signup" className="auth-back-btn">← Back</Link>
      <div className="auth-card verify-card">
        <h1 className="auth-title">Verification</h1>

        <label className="auth-label">Enter code from email</label>
        <input
          className="auth-input"
          type="text"
          placeholder="Enter code"
          value={code}
          onChange={e => setCode(e.target.value)}
          onKeyDown={handleKeyDown}
          maxLength={10}
          autoComplete="one-time-code"
        />

        {error && <p className="auth-error">{error}</p>}
        {resent && <p className="auth-success">Code sent! Check your inbox.</p>}

        <div className="auth-links-row center">
          <span className="auth-link clickable" onClick={handleResend}>
            Send code again
          </span>
        </div>

        <div className="auth-btn-row">
          <button
            className="auth-btn-outline"
            onClick={handleCheck}
            disabled={loading}
          >
            {loading ? 'Checking...' : 'Check'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default VerificationPage;
