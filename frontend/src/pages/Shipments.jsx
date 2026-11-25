
import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Shipments.css';

const API = 'http://localhost:8181';

function Shipments() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    recipientName: '',
    destinationCity: '',
    deliveryAddress: '',
  });

  const [shipments, setShipments] = useState([]);
  const [trackingNumber, setTrackingNumber] = useState('');
  const [trackedShipment, setTrackedShipment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const loadShipments = useCallback(async () => {
    const response = await fetch(`${API}/api/customer/shipments`, {
      credentials: 'include',
    });

    if (response.status === 401 || response.status === 403) {
      navigate('/login', { replace: true });
      return null;
    }

    if (!response.ok) {
      throw new Error('Unable to load shipments');
    }

    return response.json();
  }, [navigate]);

  useEffect(() => {
    let active = true;

    async function load() {
      try {
        const data = await loadShipments();

        if (active && data !== null) {
          setShipments(data);
        }
      } catch {
        if (active) {
          setError('Unable to load your shipments.');
        }
      } finally {
        if (active) setLoading(false);
      }
    }

    load();

    return () => {
      active = false;
    };
  }, [loadShipments]);

  const handleChange = (event) => {
    setForm((previous) => ({
      ...previous,
      [event.target.name]: event.target.value,
    }));
  };

  const handleBooking = async (event) => {
    event.preventDefault();
    if (saving) return;

    setError('');
    setMessage('');

    if (Object.values(form).some((value) => !value.trim())) {
      setError('All shipment fields are required.');
      return;
    }

    setSaving(true);

    try {
      const csrfResponse = await fetch(`${API}/api/auth/csrf`, {
        credentials: 'include',
      });

      if (!csrfResponse.ok) {
        throw new Error('Unable to obtain security token');
      }

      const csrf = await csrfResponse.json();

      const response = await fetch(`${API}/api/customer/shipments`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          [csrf.headerName]: csrf.token,
        },
        body: JSON.stringify({
          recipientName: form.recipientName.trim(),
          destinationCity: form.destinationCity.trim(),
          deliveryAddress: form.deliveryAddress.trim(),
        }),
      });

      if (response.status === 401 || response.status === 403) {
        navigate('/login', { replace: true });
        return;
      }

      if (!response.ok) {
        setError(
          response.status === 400
            ? 'Please check your shipment details.'
            : 'Unable to book shipment.'
        );
        return;
      }

      const created = await response.json();

      setMessage(`Shipment booked! Tracking ID: ${created.trackingNumber}`);
      setShipments((previous) => [created, ...previous]);

      setForm({
        recipientName: '',
        destinationCity: '',
        deliveryAddress: '',
      });
    } catch {
      setError('Unable to connect to the shipment service.');
    } finally {
      setSaving(false);
    }
  };

  const handleTracking = async (event) => {
    event.preventDefault();
    setError('');
    setTrackedShipment(null);

    if (!trackingNumber.trim()) {
      setError('Enter a tracking number.');
      return;
    }

    try {
      const response = await fetch(
        `${API}/api/customer/shipments/${encodeURIComponent(
          trackingNumber.trim()
        )}`,
        { credentials: 'include' }
      );

      if (response.status === 401 || response.status === 403) {
        navigate('/login', { replace: true });
        return;
      }

      if (response.status === 404) {
        setError('Shipment not found in your account.');
        return;
      }

      if (!response.ok) {
        throw new Error('Tracking request failed');
      }

      setTrackedShipment(await response.json());
    } catch {
      setError('Unable to retrieve shipment details.');
    }
  };

  return (
    <main className="shipments-page">
      <header className="shipments-header">
        <h1>My Shipments</h1>
        <p>Book, manage and track your deliveries.</p>
      </header>

      {error && <p className="shipment-error" role="alert">{error}</p>}
      {message && <p className="shipment-success" role="status">{message}</p>}

      <div className="shipments-layout">
        <section className="shipment-panel">
          <h2>Book a Shipment</h2>

          <form onSubmit={handleBooking}>
            <label htmlFor="recipientName">Recipient Name</label>
            <input
              id="recipientName"
              name="recipientName"
              value={form.recipientName}
              onChange={handleChange}
              maxLength={100}
              required
            />

            <label htmlFor="destinationCity">Destination City</label>
            <input
              id="destinationCity"
              name="destinationCity"
              value={form.destinationCity}
              onChange={handleChange}
              maxLength={100}
              required
            />

            <label htmlFor="deliveryAddress">Delivery Address</label>
            <textarea
              id="deliveryAddress"
              name="deliveryAddress"
              value={form.deliveryAddress}
              onChange={handleChange}
              maxLength={255}
              rows={4}
              required
            />

            <button type="submit" disabled={saving}>
              {saving ? 'Booking...' : 'Book Shipment'}
            </button>
          </form>
        </section>

        <section className="shipment-panel">
          <h2>Track Shipment</h2>

          <form onSubmit={handleTracking}>
            <label htmlFor="trackingNumber">Tracking Number</label>
            <input
              id="trackingNumber"
              value={trackingNumber}
              onChange={(e) => setTrackingNumber(e.target.value)}
              placeholder="TRF-XXXXXXXXXXXX"
            />

            <button type="submit">Track Shipment</button>
          </form>

          {trackedShipment && (
            <div className="tracking-result">
              <h3>Shipment Details</h3>
              <p><strong>Tracking:</strong> {trackedShipment.trackingNumber}</p>
              <p><strong>Recipient:</strong> {trackedShipment.recipientName}</p>
              <p><strong>Destination:</strong> {trackedShipment.destinationCity}</p>
              <p><strong>Status:</strong> {trackedShipment.status}</p>
            </div>
          )}
        </section>
      </div>

      <section className="shipment-panel shipment-list">
        <h2>My Shipment History</h2>

        {loading ? (
          <p>Loading shipments...</p>
        ) : shipments.length === 0 ? (
          <p>No shipments booked yet.</p>
        ) : (
          <div className="shipment-table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Tracking Number</th>
                  <th>Recipient</th>
                  <th>Destination</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {shipments.map((shipment) => (
                  <tr key={shipment.trackingNumber}>
                    <td>{shipment.trackingNumber}</td>
                    <td>{shipment.recipientName}</td>
                    <td>{shipment.destinationCity}</td>
                    <td>{shipment.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}

export default Shipments;
