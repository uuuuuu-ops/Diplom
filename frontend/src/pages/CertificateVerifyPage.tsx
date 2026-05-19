import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { verifyCertificate, Certificate } from '../api';
import './css/CertificatePage.css';

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });

const CertificateVerifyPage: React.FC = () => {
  const { code: codeParam } = useParams<{ code: string }>();
  const navigate = useNavigate();

  const [code, setCode] = useState(codeParam || '');
  const [cert, setCert] = useState<Certificate | null>(null);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState('');

  const runLookup = async (c: string) => {
    if (!c.trim()) return;
    setLoading(true);
    setError('');
    setSearched(true);
    try {
      const res = await verifyCertificate(c.trim());
      setCert(res.data);
    } catch (err: any) {
      if (err?.response?.status === 404) {
        setError('No certificate found for that code.');
      } else {
        setError('Could not verify certificate. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (codeParam) runLookup(codeParam);
  }, [codeParam]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setCert(null);
    if (code.trim()) {
      navigate(`/certificates/verify/${encodeURIComponent(code.trim())}`);
      runLookup(code.trim());
    }
  };

  return (
    <div className="cert-page">
      <Navbar />
      <div className="cert-container">
        <header className="cert-header">
          <h1>Verify a certificate</h1>
          <p className="cert-sub">
            Enter a verification code printed on a CouTeach certificate to confirm it is genuine.
          </p>
        </header>

        <form className="cert-form" onSubmit={handleSubmit}>
          <input
            className="cert-input"
            type="text"
            placeholder="e.g. 9d2f4c1e-..."
            value={code}
            onChange={(e) => setCode(e.target.value)}
          />
          <button className="cert-btn" type="submit" disabled={loading || !code.trim()}>
            {loading ? 'Checking...' : 'Verify'}
          </button>
        </form>

        {searched && error && <p className="cert-error">{error}</p>}

        {cert && (
          <div className="cert-card valid">
            <div className="cert-valid-banner">
              <span className="cert-check">✓</span> Genuine certificate
            </div>

            <div className="cert-body">
              <p className="cert-label">This is to certify that</p>
              <h2 className="cert-name">{cert.studentName || 'Student'}</h2>

              <p className="cert-label">has successfully completed the course</p>
              <h3 className="cert-course">{cert.courseTitle || 'Course'}</h3>

              <div className="cert-meta">
                <div>
                  <p className="cert-label">Issued on</p>
                  <p>{formatDate(cert.issuedAt)}</p>
                </div>
                <div>
                  <p className="cert-label">Verification code</p>
                  <p className="cert-mono">{cert.verificationCode}</p>
                </div>
              </div>

              {cert.pdfUrl && (
                <a
                  className="cert-pdf-btn"
                  href={cert.pdfUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Download PDF
                </a>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CertificateVerifyPage;
