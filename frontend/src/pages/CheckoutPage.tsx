import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import Navbar from '../components/Navbar';
import {
  createSubscription,
  confirmSubscription,
  enrollFree,
} from '../api';
import { getCourseById, Course } from '../api/courses';
import { isAuthenticated } from '../api/auth';
import './css/CheckoutPage.css';

type Step = 'choose' | 'redirect' | 'capturing' | 'success' | 'error';

const CheckoutPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [params] = useSearchParams();
  const navigate = useNavigate();

  const [course, setCourse] = useState<Course | null>(null);
  const [loading, setLoading] = useState(true);
  const [step, setStep] = useState<Step>('choose');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    if (!isAuthenticated()) navigate('/login', { replace: true });
  }, [navigate]);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    getCourseById(id)
      .then((res) => setCourse(res.data))
      .catch(() => setCourse(null))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    const subscriptionId = params.get('subscription_id');
    if (subscriptionId) {
      setStep('capturing');
      confirmSubscription(subscriptionId)
        .then(() => setStep('success'))
        .catch((err: any) => {
          setErrorMsg(err?.response?.data?.message || 'Could not confirm subscription.');
          setStep('error');
        });
    }
  }, [params]);

  const handleSubscribe = async () => {
    setStep('redirect');
    setErrorMsg('');
    try {
      const res = await createSubscription();
      window.location.href = res.data.approvalUrl;
    } catch (err: any) {
      setErrorMsg(err?.response?.data?.message || 'Could not start subscription.');
      setStep('error');
    }
  };

  const handleFreeEnroll = async () => {
    if (!id) return;
    setStep('capturing');
    try {
      await enrollFree(id);
      setStep('success');
    } catch (err: any) {
      setErrorMsg(err?.response?.data?.message || 'Could not enroll.');
      setStep('error');
    }
  };

  if (loading) {
    return (
      <div className="checkout-page">
        <Navbar />
        <p className="ck-loading">Loading checkout...</p>
      </div>
    );
  }

  if (!course) {
    return (
      <div className="checkout-page">
        <Navbar />
        <p className="ck-loading">Course not found.</p>
      </div>
    );
  }

  const isFree = course.free === true;

  return (
    <div className="checkout-page">
      <Navbar />
      <div className="ck-container">
        <button className="back-btn" onClick={() => navigate(`/courses/${course.id}`)}>
          ← Back to course
        </button>

        <div className="ck-card">
          {step === 'choose' && (
            <>
              <h1 className="ck-title">Checkout</h1>
              <div className="ck-summary">
                <div className="ck-summary-row">
                  <span>Course</span>
                  <b>{course.title}</b>
                </div>
                <div className="ck-summary-row">
                  <span>Educator</span>
                  <span>{course.teacherName || 'Educator'}</span>
                </div>
                <div className="ck-summary-row total">
                  <span>Access</span>
                  <b>{isFree ? 'Free' : 'Subscription required'}</b>
                </div>
              </div>

              {isFree ? (
                <button className="ck-pay-btn" onClick={handleFreeEnroll}>
                  Enroll for free
                </button>
              ) : (
                <div className="ck-sub-card">
                  <div>
                    <h3>Subscribe &amp; access everything</h3>
                    <p>One subscription unlocks every subscription-based course on the platform.</p>
                  </div>
                  <button className="ck-sub-btn" onClick={handleSubscribe}>
                    Start subscription
                  </button>
                </div>
              )}

              <p className="ck-fineprint">
                Subscription payments are processed securely by PayPal. You can cancel at any time.
              </p>
            </>
          )}

          {step === 'redirect' && (
            <div className="ck-state">
              <div className="ck-spinner" />
              <h2>Redirecting to PayPal...</h2>
              <p>If nothing happens in a few seconds, please refresh.</p>
            </div>
          )}

          {step === 'capturing' && (
            <div className="ck-state">
              <div className="ck-spinner" />
              <h2>Confirming your subscription...</h2>
              <p>Hold on a moment.</p>
            </div>
          )}

          {step === 'success' && (
            <div className="ck-state success">
              <div className="ck-success-icon">✓</div>
              <h2>You're enrolled!</h2>
              <p>You now have full access to <b>{course.title}</b>.</p>
              <div className="ck-success-actions">
                <button className="ck-pay-btn" onClick={() => navigate(`/courses/${course.id}/learn`)}>
                  Start learning
                </button>
                <button className="ck-secondary-btn" onClick={() => navigate('/my-enrollments')}>
                  My courses
                </button>
              </div>
            </div>
          )}

          {step === 'error' && (
            <div className="ck-state error">
              <div className="ck-error-icon">!</div>
              <h2>Something went wrong</h2>
              <p>{errorMsg}</p>
              <button className="ck-pay-btn" onClick={() => setStep('choose')}>
                Try again
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CheckoutPage;