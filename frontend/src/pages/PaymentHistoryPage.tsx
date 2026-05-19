import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import {
  getMyPayments,
  getMySubscriptions,
  Payment,
  Subscription,
} from '../api';
import { isAuthenticated } from '../api/auth';
import './css/PaymentHistoryPage.css';

const formatDate = (iso?: string) =>
  iso ? new Date(iso).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' }) : '—';

const PaymentHistoryPage: React.FC = () => {
  const navigate = useNavigate();
  const [payments, setPayments] = useState<Payment[]>([]);
  const [subs, setSubs] = useState<Subscription[]>([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState<'payments' | 'subscriptions'>('payments');

  useEffect(() => {
    if (!isAuthenticated()) navigate('/login', { replace: true });
  }, [navigate]);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      const [pRes, sRes] = await Promise.allSettled([
        getMyPayments(),
        getMySubscriptions(),
      ]);
      setPayments(pRes.status === 'fulfilled' ? pRes.value.data : []);
      setSubs(sRes.status === 'fulfilled' ? sRes.value.data : []);
      setLoading(false);
    };
    load();
  }, []);

  return (
    <div className="payments-page">
      <Navbar />
      <div className="ph-container">
        <header className="ph-header">
          <h1>Billing & payments</h1>
          <p className="ph-sub">All your purchases and subscriptions in one place.</p>
        </header>

        <div className="ph-tabs">
          <button
            className={`ph-tab ${tab === 'payments' ? 'active' : ''}`}
            onClick={() => setTab('payments')}
          >
            Payments ({payments.length})
          </button>
          <button
            className={`ph-tab ${tab === 'subscriptions' ? 'active' : ''}`}
            onClick={() => setTab('subscriptions')}
          >
            Subscriptions ({subs.length})
          </button>
        </div>

        {loading ? (
          <p className="ph-empty">Loading...</p>
        ) : tab === 'payments' ? (
          payments.length === 0 ? (
            <p className="ph-empty">No payments yet.</p>
          ) : (
            <div className="ph-table">
              <div className="ph-table-head">
                <span>Date</span>
                <span>Order ID</span>
                <span>Course</span>
                <span>Amount</span>
                <span>Status</span>
              </div>
              {payments.map((p) => (
                <div key={p.id} className="ph-table-row">
                  <span>{formatDate(p.createdAt)}</span>
                  <span className="ph-mono">{p.paypalOrderId || p.id}</span>
                  <span>
                    {p.courseId ? (
                      <a href={`/courses/${p.courseId}`}>View course</a>
                    ) : (
                      '—'
                    )}
                  </span>
                  <span className="ph-amount">
                    {p.currency} {p.amount.toFixed(2)}
                  </span>
                  <span>
                    <span className={`ph-status ph-status-${p.status.toLowerCase()}`}>
                      {p.status}
                    </span>
                  </span>
                </div>
              ))}
            </div>
          )
        ) : subs.length === 0 ? (
          <p className="ph-empty">No subscriptions.</p>
        ) : (
          <div className="ph-sub-list">
            {subs.map((s) => (
              <div key={s.id} className="ph-sub-card">
                <div className="ph-sub-header">
                  <h3>Platform subscription</h3>
                  <span className={`ph-status ph-status-${s.status.toLowerCase()}`}>
                    {s.status}
                  </span>
                </div>
                <div className="ph-sub-grid">
                  <div>
                    <p className="ph-label">Plan ID</p>
                    <p className="ph-mono">{s.planId}</p>
                  </div>
                  <div>
                    <p className="ph-label">Started</p>
                    <p>{formatDate(s.startDate)}</p>
                  </div>
                  <div>
                    <p className="ph-label">Next billing</p>
                    <p>{formatDate(s.nextBillingDate)}</p>
                  </div>
                  {s.paypalSubscriptionId && (
                    <div>
                      <p className="ph-label">PayPal ID</p>
                      <p className="ph-mono">{s.paypalSubscriptionId}</p>
                    </div>
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

export default PaymentHistoryPage;
