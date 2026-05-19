import React, { useState, useRef, DragEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { submitTeacherApplication } from '../api/profile';
import './css/TeacherApplicationPage.css';
const CATEGORIES = ['Programming', 'Mathematics', 'Physics', 'Sciences'];

type Step = 'form' | 'submitting' | 'success' | 'error';

const Field: React.FC<{
  label: string;
  error?: string;
  children: React.ReactNode;
  hint?: string;
}> = ({ label, error, children, hint }) => (
  <div className="tap-field">
    <label className="tap-label">{label}</label>
    {hint && <span className="tap-hint">{hint}</span>}
    {children}
    {error && <span className="tap-field-error">{error}</span>}
  </div>
);

const TeacherApplicationPage: React.FC = () => {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [step, setStep] = useState<Step>('form');
  const [errorMsg, setErrorMsg] = useState('');
  const [dragOver, setDragOver] = useState(false);

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [specialization, setSpecialization] = useState('');
  const [yearsOfExperience, setYearsOfExperience] = useState('');
  const [resumeFile, setResumeFile] = useState<File | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const validate = () => {
    const errors: Record<string, string> = {};
    if (!fullName.trim() || fullName.trim().length < 2) errors.fullName = 'Full name must be at least 2 characters.';
    if (fullName.trim().length > 20) errors.fullName = 'Full name must be 20 characters or fewer.';
    if (!email.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.email = 'Enter a valid email address.';
    if (!specialization) errors.specialization = 'Please select a direction.';
    const exp = parseInt(yearsOfExperience, 10);
    if (isNaN(exp) || exp < 0) errors.yearsOfExperience = 'Experience must be 0 or more years.';
    if (!resumeFile) errors.resumeFile = 'Please upload your resume as a PDF.';
    else if (!resumeFile.name.toLowerCase().endsWith('.pdf')) errors.resumeFile = 'Only PDF files are accepted.';
    return errors;
  };


  const handleFileSelect = (file: File) => {
    setFieldErrors((e) => ({ ...e, resumeFile: '' }));
    if (!file.name.toLowerCase().endsWith('.pdf')) {
      setFieldErrors((e) => ({ ...e, resumeFile: 'Only PDF files are accepted.' }));
      return;
    }
    setResumeFile(file);
  };

  const handleDrop = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setDragOver(false);
    const file = e.dataTransfer.files?.[0];
    if (file) handleFileSelect(file);
  };

  const handleSubmit = async () => {
    const errors = validate();
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setStep('submitting');
    try {
      await submitTeacherApplication({
        fullName: fullName.trim(),
        email: email.trim(),
        specialization: specialization.trim(),
        yearsOfExperience: parseInt(yearsOfExperience, 10),
        resumeFile: resumeFile!,
      });
      setStep('success');
    } catch (err: any) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Failed to submit application. Please try again.';
      setErrorMsg(msg);
      setStep('error');
    }
  };

  if (step === 'submitting') {
    return (
      <div className="tap-page">
        <Navbar />
        <div className="tap-center-wrap">
          <div className="tap-status-card">
            <div className="tap-spinner" />
            <p className="tap-status-title">Submitting your application…</p>
            <p className="tap-status-sub">Your resume is being uploaded and analysed by AI. This may take a few seconds.</p>
          </div>
        </div>
      </div>
    );
  }

  if (step === 'success') {
    return (
      <div className="tap-page">
        <Navbar />
        <div className="tap-center-wrap">
          <div className="tap-status-card">
            <div className="tap-success-icon">✓</div>
            <p className="tap-status-title">Application Submitted!</p>
            <p className="tap-status-sub">
              Your resume has been received and is now under review. Our team and AI screening
              system will evaluate your application. You'll hear back via email.
            </p>
            <div className="tap-success-actions">
              <button className="tap-btn-primary" onClick={() => navigate('/profile')}>
                Back to Profile
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (step === 'error') {
    return (
      <div className="tap-page">
        <Navbar />
        <div className="tap-center-wrap">
          <div className="tap-status-card">
            <div className="tap-error-icon">✕</div>
            <p className="tap-status-title">Submission Failed</p>
            <p className="tap-status-sub">{errorMsg}</p>
            <div className="tap-success-actions">
              <button className="tap-btn-outline" onClick={() => setStep('form')}>
                Try Again
              </button>
              <button className="tap-btn-primary" onClick={() => navigate('/profile')}>
                Back to Profile
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="tap-page">
      <Navbar />

      <div className="tap-container">
        {/* Back link */}
        <Link to="/profile" className="back-btn">← Back to Profile</Link>

        <div className="tap-card">
          {/* Header */}
          <div>
            <h1 className="tap-title">Become a Qualified Teacher</h1>
            <p className="tap-subtitle">
              Submit your resume and details for review. Our AI system will screen your application and an admin will make the final decision.
            </p>
          </div>

          <div className="tap-divider" />

          {/* Form */}
          <div className="tap-form">
            <div className="tap-form-row">
              <Field label="Full Name" error={fieldErrors.fullName}>
                <input
                  className={`tap-input ${fieldErrors.fullName ? 'tap-input-error' : ''}`}
                  type="text"
                  placeholder="Jane Smith"
                  value={fullName}
                  onChange={(e) => { setFullName(e.target.value); setFieldErrors(f => ({ ...f, fullName: '' })); }}
                  maxLength={20}
                />
              </Field>

              <Field label="Email Address" error={fieldErrors.email}>
                <input
                  className={`tap-input ${fieldErrors.email ? 'tap-input-error' : ''}`}
                  type="email"
                  placeholder="jane@example.com"
                  value={email}
                  onChange={(e) => { setEmail(e.target.value); setFieldErrors(f => ({ ...f, email: '' })); }}
                />
              </Field>
            </div>

            <div className="tap-form-row">
              <Field label="Direction" error={fieldErrors.specialization}>
                <select
                  className={`tap-input ${fieldErrors.specialization ? 'tap-input-error' : ''}`}
                  value={specialization}
                  onChange={(e) => { setSpecialization(e.target.value); setFieldErrors(f => ({ ...f, specialization: '' })); }}
                >
                  <option value="">Choose a direction…</option>
                  {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                </select>
              </Field>

              <Field label="Years of Experience" error={fieldErrors.yearsOfExperience}>
                <input
                  className={`tap-input ${fieldErrors.yearsOfExperience ? 'tap-input-error' : ''}`}
                  type="number"
                  placeholder="e.g. 3"
                  min={0}
                  value={yearsOfExperience}
                  onChange={(e) => { setYearsOfExperience(e.target.value); setFieldErrors(f => ({ ...f, yearsOfExperience: '' })); }}
                />
              </Field>
            </div>

            {/* Resume upload */}
            <Field label="Resume / CV" error={fieldErrors.resumeFile} hint="PDF only — will be analysed by AI">
              <div
                className={`tap-dropzone ${dragOver ? 'tap-dropzone-over' : ''} ${resumeFile ? 'tap-dropzone-filled' : ''} ${fieldErrors.resumeFile ? 'tap-dropzone-error' : ''}`}
                onClick={() => fileInputRef.current?.click()}
                onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
                onDragLeave={() => setDragOver(false)}
                onDrop={handleDrop}
              >
                <input
                  ref={fileInputRef}
                  type="file"
                  accept=".pdf"
                  style={{ display: 'none' }}
                  onChange={(e) => { const f = e.target.files?.[0]; if (f) handleFileSelect(f); }}
                />
                {resumeFile ? (
                  <div className="tap-file-info">
                    <div>
                      <p className="tap-file-name">{resumeFile.name}</p>
                      <p className="tap-file-size">{(resumeFile.size / 1024).toFixed(1)} KB</p>
                    </div>
                    <button
                      className="tap-file-remove"
                      onClick={(e) => { e.stopPropagation(); setResumeFile(null); }}
                    >
                      ✕
                    </button>
                  </div>
                ) : (
                  <div className="tap-dropzone-idle">
                    <p className="tap-dropzone-label">
                      <strong>Click to browse</strong> or drag & drop your PDF here
                    </p>
                    <p className="tap-dropzone-sub">PDF only Р’В· Max 10 MB</p>
                  </div>
                )}
              </div>
            </Field>

            {/* AI note */}
            <div className="tap-ai-note">
              <p className="tap-ai-text">
                Your resume will be automatically scanned by our AI screening system, which
                analyses your experience, strengths, and fit for the platform — before an
                admin makes the final call.
              </p>
            </div>

            {/* Actions */}
            <div className="tap-actions">
              <Link to="/profile" className="tap-btn-outline">Cancel</Link>
              <button className="tap-btn-primary" onClick={handleSubmit}>
                Submit Application
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TeacherApplicationPage;
