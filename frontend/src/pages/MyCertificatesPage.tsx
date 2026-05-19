import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { getMyCertificates, Certificate } from '../api';
import { isAuthenticated } from '../api/auth';
import './css/CertificatePage.css';

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });

const MyCertificatesPage: React.FC = () => {
  const navigate = useNavigate();
  const [certs, setCerts] = useState<Certificate[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated()) navigate('/login', { replace: true });
  }, [navigate]);

  useEffect(() => {
    getMyCertificates()
      .then((r) => setCerts(r.data || []))
      .catch(() => setCerts([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="cert-page">
      <Navbar />
      <div className="cert-container">
        <header className="cert-header">
          <h1>My certificates</h1>
          <p className="cert-sub">
            Earn a certificate by completing every lesson and required quiz in a course.
          </p>
        </header>

        {loading ? (
          <p className="cert-empty">Loading certificates...</p>
        ) : certs.length === 0 ? (
          <div className="cert-empty-card">
            <p className="cert-empty">You haven't earned any certificates yet.</p>
            <Link to="/my-enrollments" className="cert-browse-btn">
              View my courses
            </Link>
          </div>
        ) : (
          <div className="cert-grid">
            {certs.map((cert) => (
              <div key={cert.id} className="cert-mini">
                <h3>{cert.courseTitle || 'Course'}</h3>
                <p className="cert-mini-date">Issued {formatDate(cert.issuedAt)}</p>
                <div className="cert-mini-actions">
                  <Link
                    to={`/certificates/verify/${cert.verificationCode}`}
                    className="cert-mini-btn"
                  >
                    View
                  </Link>
                  {cert.pdfUrl && (
                    <a
                      className="cert-mini-btn"
                      href={cert.pdfUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      Download PDF
                    </a>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyCertificatesPage;