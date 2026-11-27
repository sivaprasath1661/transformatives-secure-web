
import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './CourierShipments.css';

const API = 'http://localhost:8181';

const labels = {
  PENDING: 'Pending',
  IN_TRANSIT: 'In Transit',
  DELIVERED: 'Delivered',
};

const courierNames = {
  jetdeliveries: 'Jet Deliveries',
  jkcouriers: 'JK Couriers',
};

// Fetch courier data without changing React state.
// This can be reused by initial loading and manual refresh.
async function fetchCourierData(signal) {
  const [accessResponse, shipmentsResponse] = await Promise.all([
    fetch(`${API}/api/courier/access`, {
      credentials: 'include',
      signal,
    }),
    fetch(`${API}/api/courier/shipments`, {
      credentials: 'include',
      signal,
    }),
  ]);

  if (
    accessResponse.status === 401 ||
    shipmentsResponse.status === 401
  ) {
    return { type: 'unauthenticated' };
  }

  if (
    accessResponse.status === 403 ||
    shipmentsResponse.status === 403
  ) {
    return { type: 'denied' };
  }

  if (!accessResponse.ok || !shipmentsResponse.ok) {
    throw new Error('Unable to load courier shipments');
  }

  const account = await accessResponse.json();
  const shipments = await shipmentsResponse.json();

  return {
    type: 'success',
    username: account.username,
    shipments,
  };
}

function CourierShipments() {
  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [shipments, setShipments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [busy, setBusy] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [denied, setDenied] = useState(false);

  const handleCourierData = useCallback(
    (result) => {
      if (result.type === 'unauthenticated') {
        navigate('/login', { replace: true });
        return;
      }

      if (result.type === 'denied') {
        setDenied(true);
        return;
      }

      setDenied(false);
      setUsername(result.username);
      setShipments(result.shipments);
    },
    [navigate]
  );

  // Initial data loading.
  useEffect(() => {
    const controller = new AbortController();

    async function loadInitialData() {
      try {
        const result = await fetchCourierData(
          controller.signal
        );

        if (controller.signal.aborted) return;

        if (result.type === 'unauthenticated') {
          navigate('/login', { replace: true });
          return;
        }

        if (result.type === 'denied') {
          setDenied(true);
          return;
        }

        setDenied(false);
        setUsername(result.username);
        setShipments(result.shipments);
      } catch (err) {
        if (
          !controller.signal.aborted &&
          err.name !== 'AbortError'
        ) {
          setError('Unable to load your assigned shipments.');
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    loadInitialData();

    return () => controller.abort();
  }, [navigate]);

  // Manual refresh and refresh after delivery updates.
  const refresh = useCallback(async () => {
    setRefreshing(true);
    setError('');

    try {
      const result = await fetchCourierData();

      handleCourierData(result);

      return result;
    } catch {
      setError('Unable to refresh assigned shipments.');
      return null;
    } finally {
      setRefreshing(false);
    }
  }, [handleCourierData]);

  async function updateShipment(shipment, status) {
    if (busy || refreshing) return;

    const confirmed = window.confirm(
      `Mark ${shipment.trackingNumber} as ${labels[status]}?`
    );

    if (!confirmed) return;

    setBusy(shipment.trackingNumber);
    setError('');
    setMessage('');

    try {
      const csrfResponse = await fetch(
        `${API}/api/auth/csrf`,
        {
          credentials: 'include',
        }
      );

      if (!csrfResponse.ok) {
        throw new Error('Unable to obtain CSRF token');
      }

      const csrf = await csrfResponse.json();

      const response = await fetch(
        `${API}/api/courier/shipments/${encodeURIComponent(
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
        setError(
          'Courier access or CSRF token rejected.'
        );
        return;
      }

      if (response.status === 404) {
        setError(
          'This shipment is not assigned to you.'
        );
        return;
      }

      if (response.status === 409) {
        setError(
          'Shipment status has changed. Please refresh.'
        );
        return;
      }

      if (!response.ok) {
        throw new Error('Delivery update failed');
      }

      const updated = await response.json();

      setShipments((previous) =>
        previous.map((item) =>
          item.trackingNumber === updated.trackingNumber
            ? updated
            : item
        )
      );

      setMessage(
        `${updated.trackingNumber} marked ${labels[status]}.`
      );

      // Reload the authoritative records from the backend.
      await refresh();
    } catch {
      setError('Unable to update delivery status.');
    } finally {
      setBusy('');
    }
  }

  if (denied) {
    return (
      <main className="courier-page">
        <h1>Access Denied</h1>
        <p>Courier account required.</p>
      </main>
    );
  }

  const count = (status) =>
    shipments.filter(
      (item) => item.status === status
    ).length;

  return (
    <main className="courier-page">
      <div className="courier-heading">
        <div>
          <span className="courier-eyebrow">
            DELIVERY OPERATIONS
          </span>

          <h1>
            {courierNames[username] || 'Courier'} Dashboard
          </h1>

          <p>
            Only shipments assigned to your courier account
            appear here.
          </p>
        </div>

        <button
          type="button"
          onClick={refresh}
          disabled={refreshing || Boolean(busy)}
        >
          {refreshing ? 'Refreshing...' : '↻ Refresh'}
        </button>
      </div>

      {error && (
        <p className="courier-error" role="alert">
          {error}
        </p>
      )}

      {message && (
        <p className="courier-success" role="status">
          {message}
        </p>
      )}

      <div className="courier-stats">
        <div>
          <span>Assigned</span>
          <strong>{shipments.length}</strong>
        </div>

        <div>
          <span>Pending</span>
          <strong>{count('PENDING')}</strong>
        </div>

        <div>
          <span>In Transit</span>
          <strong>{count('IN_TRANSIT')}</strong>
        </div>

        <div>
          <span>Delivered</span>
          <strong>{count('DELIVERED')}</strong>
        </div>
      </div>

      <section className="courier-panel">
        <h2>My Assigned Shipments</h2>

        {loading ? (
          <p>Loading assigned shipments...</p>
        ) : shipments.length === 0 ? (
          <p>
            No shipments assigned yet. Ask the administrator
            to assign a pending shipment.
          </p>
        ) : (
          <div className="courier-table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Tracking</th>
                  <th>Recipient</th>
                  <th>Destination</th>
                  <th>Address</th>
                  <th>Phone</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                {shipments.map((shipment) => {
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

                      <td>{shipment.deliveryAddress}</td>

                      <td>
                        {shipment.recipientPhone || '—'}
                      </td>

                      <td>
                        <span
                          className={`courier-status courier-${shipment.status.toLowerCase()}`}
                        >
                          {labels[shipment.status] ||
                            shipment.status}
                        </span>
                      </td>

                      <td>
                        {nextStatus ? (
                          <button
                            type="button"
                            disabled={
                              Boolean(busy) || refreshing
                            }
                            onClick={() =>
                              updateShipment(
                                shipment,
                                nextStatus
                              )
                            }
                          >
                            {busy === shipment.trackingNumber
                              ? 'Updating...'
                              : nextStatus === 'IN_TRANSIT'
                                ? 'Start Delivery'
                                : 'Mark Delivered'}
                          </button>
                        ) : (
                          'No action'
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}

export default CourierShipments;
