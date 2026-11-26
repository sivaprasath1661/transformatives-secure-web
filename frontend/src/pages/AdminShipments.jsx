
import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './AdminShipments.css';

const API = 'http://localhost:8181';

const labels = {
  PENDING: 'Pending',
  IN_TRANSIT: 'In Transit',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
};

function AdminShipments() {
  const navigate = useNavigate();

  const [shipments, setShipments] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [updating, setUpdating] = useState('');
  const [selectedTracking, setSelectedTracking] = useState('');
  const [history, setHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [denied, setDenied] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const loadShipments = useCallback(async (signal) => {
    const response = await fetch(`${API}/api/admin/shipments`, {
      credentials: 'include',
      signal,
    });

    if (response.status === 401) {
      navigate('/login', { replace: true });
      return null;
    }

    if (response.status === 403) {
      setDenied(true);
      return null;
    }

    if (!response.ok) {
      throw new Error('Unable to load admin shipments');
    }

    return response.json();
  }, [navigate]);

  useEffect(() => {
    const controller = new AbortController();

    async function load() {
      try {
        const data = await loadShipments(controller.signal);

        if (!controller.signal.aborted && data !== null) {
          setShipments(data);
        }
      } catch (err) {
        if (err.name !== 'AbortError' && !controller.signal.aborted) {
          setError('Unable to load shipment records.');
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    load();

    return () => controller.abort();
  }, [loadShipments]);

  const refreshShipments = async () => {
    setRefreshing(true);
    setError('');

    try {
      const data = await loadShipments();

      if (data !== null) {
        setShipments(data);
      }

      return data;
    } catch {
      setError('Unable to refresh shipments.');
      return null;
    } finally {
      setRefreshing(false);
    }
  };

  const getCsrfToken = async () => {
    const response = await fetch(`${API}/api/auth/csrf`, {
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error('Unable to obtain CSRF token');
    }

    return response.json();
  };

  const loadHistory = async (trackingNumber) => {
    setSelectedTracking(trackingNumber);
    setHistoryLoading(true);
    setHistory([]);
    setError('');

    try {
      const response = await fetch(
        `${API}/api/admin/shipments/${encodeURIComponent(
          trackingNumber
        )}/history`,
        { credentials: 'include' }
      );

      if (response.status === 401) {
        navigate('/login', { replace: true });
        return;
      }

      if (response.status === 403) {
        setDenied(true);
        return;
      }

      if (!response.ok) {
        throw new Error('History request failed');
      }

      setHistory(await response.json());
    } catch {
      setError('Unable to load status history.');
    } finally {
      setHistoryLoading(false);
    }
  };

  const updateShipment = async (shipment, status) => {
    if (updating) return;

    const confirmed = window.confirm(
      `Change ${shipment.trackingNumber} to ${labels[status]}?`
    );

    if (!confirmed) return;

    setUpdating(shipment.trackingNumber);
    setError('');
    setMessage('');

    try {
      const csrf = await getCsrfToken();

      const response = await fetch(
        `${API}/api/admin/shipments/${encodeURIComponent(
          shipment.trackingNumber
        )}/status`,
        {
          method: 'PATCH',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
            [csrf.headerName]: csrf.token,
          },
          body: JSON.stringify({ status }),
        }
      );

      if (response.status === 401) {
        navigate('/login', { replace: true });
        return;
      }

      if (response.status === 403) {
        setError('Administrator access is required.');
        return;
      }

      if (response.status === 409) {
        setError('Shipment status has changed. Please refresh.');
        await refreshShipments();
        return;
      }

      if (!response.ok) {
        throw new Error('Status update failed');
      }

      const updated = await response.json();

      setShipments((previous) =>
        previous.map((item) =>
          item.trackingNumber === updated.trackingNumber
            ? updated
            : item
        )
      );

      await refreshShipments();

      if (selectedTracking === shipment.trackingNumber) {
        await loadHistory(shipment.trackingNumber);
      }

      setMessage(
        `Shipment ${shipment.trackingNumber} marked ${labels[status]}.`
      );
    } catch {
      setError('Unable to update shipment status.');
    } finally {
      setUpdating('');
    }
  };

  const filteredShipments = shipments.filter((shipment) => {
    const query = search.trim().toLowerCase();

    return [
      shipment.trackingNumber,
      shipment.recipientName,
      shipment.destinationCity,
      shipment.status,
    ].some((value) =>
      String(value ?? '').toLowerCase().includes(query)
    );
  });

  const count = (status) =>
    shipments.filter((item) => item.status === status).length;

  const stats = [
    { label: 'Total', count: shipments.length },
    { label: 'Pending', count: count('PENDING') },
    { label: 'In Transit', count: count('IN_TRANSIT') },
    { label: 'Delivered', count: count('DELIVERED') },
    { label: 'Cancelled', count: count('CANCELLED') },
  ];

  if (denied) {
    return (
      <main className="admin-page">
        <h1>Access Denied</h1>
        <p>This page is restricted to administrators.</p>
      </main>
    );
  }

  return (
    <main className="admin-page">
      <header className="admin-heading">
        <div>
          <span className="admin-eyebrow">OPERATIONS</span>
          <h1>Admin Logistics Dashboard</h1>
          <p>Manage shipments and monitor delivery progress.</p>
        </div>

        <button
          className="admin-refresh"
          type="button"
          onClick={() => {
            if (!refreshing) {
              refreshShipments();
            }
          }}
          disabled={refreshing}
          aria-busy={refreshing}
        >
          {refreshing ? '↻ Refreshing...' : '↻ Refresh'}
        </button>
      </header>

      {error && (
        <p className="admin-error" role="alert">
          {error}
        </p>
      )}

      {message && (
        <p className="admin-success" role="status">
          {message}
        </p>
      )}

      <section
        className={`admin-stats ${refreshing ? 'admin-refreshing' : ''}`}
        aria-label="Shipment statistics"
        aria-busy={refreshing}
      >
        {stats.map((stat) => (
          <div className="admin-stat" key={stat.label}>
            <span>{stat.label}</span>
            <strong>{loading ? '—' : stat.count}</strong>
          </div>
        ))}
      </section>

      <section
        className={`admin-panel ${refreshing ? 'admin-refreshing' : ''}`}
      >
        <div className="admin-table-heading">
          <h2>All Customer Shipments</h2>

          <input
            aria-label="Search shipments"
            placeholder="Search tracking, recipient, city or status"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        {loading ? (
          <p>Loading shipments...</p>
        ) : filteredShipments.length === 0 ? (
          <p>No matching shipments found.</p>
        ) : (
          <div className="admin-table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Tracking Number</th>
                  <th>Recipient</th>
                  <th>Destination</th>
                  <th>Package</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {filteredShipments.map((shipment) => {
                  const nextStatus =
                    shipment.status === 'PENDING'
                      ? 'IN_TRANSIT'
                      : shipment.status === 'IN_TRANSIT'
                        ? 'DELIVERED'
                        : null;

                  return (
                    <tr key={shipment.trackingNumber}>
                      <td>{shipment.trackingNumber}</td>
                      <td>{shipment.recipientName}</td>
                      <td>{shipment.destinationCity}</td>
                      <td>
                        {shipment.packageType?.replaceAll('_', ' ') ||
                          'Not recorded'}
                      </td>
                      <td>
                        <span
                          className={`admin-status status-${shipment.status.toLowerCase()}`}
                        >
                          {labels[shipment.status] || shipment.status}
                        </span>
                      </td>
                      <td>
                        <div className="admin-actions">
                          {nextStatus && (
                            <button
                              type="button"
                              disabled={Boolean(updating) || refreshing}
                              onClick={() =>
                                updateShipment(shipment, nextStatus)
                              }
                            >
                              {updating === shipment.trackingNumber
                                ? 'Updating...'
                                : `Mark ${labels[nextStatus]}`}
                            </button>
                          )}

                          <button
                            type="button"
                            className="admin-history-button"
                            disabled={refreshing}
                            onClick={() =>
                              loadHistory(shipment.trackingNumber)
                            }
                          >
                            History
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {selectedTracking && (
        <section className="admin-panel admin-history">
          <div className="admin-table-heading">
            <div>
              <h2>Status History</h2>
              <p>{selectedTracking}</p>
            </div>

            <button
              type="button"
              className="admin-close"
              onClick={() => setSelectedTracking('')}
            >
              Close
            </button>
          </div>

          {historyLoading ? (
            <p>Loading history...</p>
          ) : history.length === 0 ? (
            <p>No administrator status changes recorded yet.</p>
          ) : (
            <div className="admin-table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Previous</th>
                    <th>New Status</th>
                    <th>Changed By</th>
                    <th>Time (UTC)</th>
                  </tr>
                </thead>

                <tbody>
                  {history.map((event) => (
                    <tr key={event.id}>
                      <td>{labels[event.oldStatus]}</td>
                      <td>{labels[event.newStatus]}</td>
                      <td>{event.changedBy}</td>
                      <td>
                        {event.changedAt.replace('T', ' ')}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}
    </main>
  );
}

export default AdminShipments;
