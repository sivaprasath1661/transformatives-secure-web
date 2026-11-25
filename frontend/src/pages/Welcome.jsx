
import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Welcome.css';

function Welcome() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();

    async function checkSession() {
      try {
        const response = await fetch(
          'http://localhost:8181/api/auth/me',
          {
            credentials: 'include',
            signal: controller.signal,
          }
        );

        if (response.status === 401 || response.status === 403) {
          navigate('/login', { replace: true });
          return;
        }

        if (!response.ok) {
          throw new Error('Session verification failed');
        }

        const data = await response.json();
        setUsername(data.username);
      } catch (err) {
        if (err.name !== 'AbortError') {
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
    setError('');

    try {
      const csrfResponse = await fetch(
        'http://localhost:8181/api/auth/csrf',
        { credentials: 'include' }
      );

      if (!csrfResponse.ok) {
        throw new Error('Unable to obtain CSRF token');
      }

      const csrf = await csrfResponse.json();

      const response = await fetch(
        'http://localhost:8181/api/auth/logout',
        {
          method: 'POST',
          credentials: 'include',
          headers: {
            [csrf.headerName]: csrf.token,
          },
        }
      );

      if (!response.ok) {
        throw new Error('Logout failed');
      }

      navigate('/login', { replace: true });
    } catch {
      setError('Logout failed. Please try again.');
    }
  };

  if (loading) {
    return <main className="welcome-page">Checking session...</main>;
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
      <section className="welcome-card">
        <div className="welcome-icon">✓</div>

        <h1>Welcome to TRANSFORMATIVES</h1>

        <p>Successfully logged in as {username}.</p>

        <p className="welcome-description">
          Book new shipments, track your deliveries and
          manage your logistics activity in one place.
        </p>

        <Link to="/shipments" className="welcome-home">
          Manage My Shipments
        </Link>

        {error && (
          <p className="login-error" role="alert">{error}</p>
        )}

        <div style={{ marginTop: '20px' }}>
          <button
            type="button"
            className="welcome-home"
            onClick={handleLogout}
          >
            Logout
          </button>
        </div>

        <p>
          <Link to="/">Back to Home</Link>
        </p>
      </section>
    </main>
  );
}

export default Welcome;
