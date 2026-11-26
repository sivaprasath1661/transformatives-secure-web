
import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Welcome.css';

const API = 'http://localhost:8181';

function Welcome() {
  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [isAdmin, setIsAdmin] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loggingOut, setLoggingOut] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();

    async function checkSession() {
      try {
        const response = await fetch(`${API}/api/auth/me`, {
          credentials: 'include',
          signal: controller.signal,
        });

        if (response.status === 401 || response.status === 403) {
          navigate('/login', { replace: true });
          return;
        }

        if (!response.ok) {
          throw new Error('Session verification failed');
        }

        const data = await response.json();

        // This endpoint is accessible only to ADMIN users.
        const adminResponse = await fetch(`${API}/api/admin/access`, {
          credentials: 'include',
          signal: controller.signal,
        });

        if (adminResponse.status === 401) {
          navigate('/login', { replace: true });
          return;
        }

        if (
          !adminResponse.ok &&
          adminResponse.status !== 403
        ) {
          throw new Error('Unable to verify user role');
        }

        if (!controller.signal.aborted) {
          setUsername(data.username);
          setIsAdmin(adminResponse.ok);
        }
      } catch (err) {
        if (err.name !== 'AbortError' && !controller.signal.aborted) {
          setError('Unable to verify your session.');
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    checkSession();

    return () => controller.abort();
  }, [navigate]);

  const handleLogout = async () => {
    if (loggingOut) return;

    setLoggingOut(true);
    setError('');

    try {
      const csrfResponse = await fetch(`${API}/api/auth/csrf`, {
        credentials: 'include',
      });

      if (!csrfResponse.ok) {
        throw new Error('Unable to obtain CSRF token');
      }

      const csrf = await csrfResponse.json();

      const response = await fetch(`${API}/api/auth/logout`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          [csrf.headerName]: csrf.token,
        },
      });

      if (!response.ok) {
        throw new Error('Logout failed');
      }

      navigate('/login', { replace: true });
    } catch {
      setError('Logout failed. Please try again.');
    } finally {
      setLoggingOut(false);
    }
  };

  if (loading) {
    return (
      <main className="welcome-page">
        Checking session...
      </main>
    );
  }

  if (!username) {
    return (
      <main className="welcome-page">
        <p>{error || 'Redirecting to login...'}</p>
      </main>
    );
  }

  return (
    <main className="welcome-page">
      <section
        className={`welcome-card ${
          isAdmin ? 'welcome-admin-card' : ''
        }`}
      >
        <div className="welcome-icon">
          {isAdmin ? '◆' : '✓'}
        </div>

        {isAdmin ? (
          <>
            <span className="welcome-role-label">
              ADMINISTRATOR ACCESS
            </span>

            <h1>Welcome, Logistics Administrator</h1>

            <p>
              Signed in as <strong>{username}</strong>
            </p>

            <p className="welcome-description">
              Manage customer shipments, monitor delivery
              progress and review operational audit history
              from your secure administration dashboard.
            </p>

            <div className="welcome-admin-features">
              <div>
                <span>01</span>
                <strong>Manage Shipments</strong>
              </div>

              <div>
                <span>02</span>
                <strong>Update Delivery Status</strong>
              </div>

              <div>
                <span>03</span>
                <strong>Review Audit History</strong>
              </div>
            </div>

            <Link
              to="/admin/shipments"
              className="welcome-home welcome-admin-primary"
            >
              Open Admin Dashboard
            </Link>
          </>
        ) : (
          <>
            <h1>Welcome to TRANSFORMATIVES</h1>

            <p>
              Successfully logged in as {username}.
            </p>

            <p className="welcome-description">
              Book new shipments, track your deliveries and
              manage your logistics activity in one place.
            </p>

            <Link to="/shipments" className="welcome-home">
              Manage My Shipments
            </Link>
          </>
        )}

        {error && (
          <p className="login-error" role="alert">
            {error}
          </p>
        )}

        <div className="welcome-footer">
          <button
            type="button"
            className="welcome-home welcome-logout"
            onClick={handleLogout}
            disabled={loggingOut}
          >
            {loggingOut ? 'Logging out...' : 'Logout'}
          </button>

          <Link to="/" className="welcome-back-link">
            Back to Home
          </Link>
        </div>
      </section>
    </main>
  );
}

export default Welcome;
