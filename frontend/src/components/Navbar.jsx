
import { useEffect, useState } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import './Navbar.css';

const API = 'http://localhost:8181';

function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();

  const [authenticated, setAuthenticated] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [checking, setChecking] = useState(true);
  const [loggingOut, setLoggingOut] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();

    async function checkSession() {
      setChecking(true);

      try {
        const response = await fetch(`${API}/api/auth/me`, {
          credentials: 'include',
          signal: controller.signal,
        });

        if (!response.ok) {
          setAuthenticated(false);
          setIsAdmin(false);
          return;
        }

        setAuthenticated(true);

        const adminResponse = await fetch(`${API}/api/admin/access`, {
          credentials: 'include',
          signal: controller.signal,
        });

        if (!controller.signal.aborted) {
          setIsAdmin(adminResponse.ok);
        }
      } catch (err) {
        if (err.name !== 'AbortError') {
          setAuthenticated(false);
          setIsAdmin(false);
        }
      } finally {
        if (!controller.signal.aborted) {
          setChecking(false);
        }
      }
    }

    checkSession();

    return () => controller.abort();
  }, [location.pathname]);

  const handleLogout = async () => {
    if (loggingOut) return;

    setLoggingOut(true);
    setError('');

    try {
      const csrfResponse = await fetch(`${API}/api/auth/csrf`, {
        credentials: 'include',
      });

      if (!csrfResponse.ok) {
        throw new Error('CSRF request failed');
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

      setAuthenticated(false);
      setIsAdmin(false);
      navigate('/login', { replace: true });
    } catch {
      setError('Logout failed. Please try again.');
    } finally {
      setLoggingOut(false);
    }
  };

  const navClass = ({ isActive }) =>
    isActive ? 'nav-link active' : 'nav-link';

  return (
    <>
      <nav className="navbar" aria-label="Main navigation">
        <NavLink to="/" className="navbar-brand">
          <span className="brand-icon" aria-hidden="true">↗</span>
          TRANSFORMATIVES
        </NavLink>

        <div className="navbar-links">
          <NavLink to="/" end className={navClass}>
            Home
          </NavLink>

          {!checking && (
            authenticated ? (
              <>
                {isAdmin ? (
                  <NavLink
                    to="/admin/shipments"
                    className={navClass}
                  >
                    Admin Dashboard
                  </NavLink>
                ) : (
                  <>
                    <NavLink to="/welcome" className={navClass}>
                      Dashboard
                    </NavLink>

                    <NavLink to="/shipments" className={navClass}>
                      My Shipments
                    </NavLink>
                  </>
                )}

                <button
                  type="button"
                  className="nav-logout"
                  onClick={handleLogout}
                  disabled={loggingOut}
                >
                  {loggingOut ? 'Logging out...' : 'Logout'}
                </button>
              </>
            ) : (
              <>
                <NavLink to="/login" className={navClass}>
                  Login
                </NavLink>

                <NavLink to="/register" className="nav-register">
                  Get Started
                </NavLink>
              </>
            )
          )}
        </div>
      </nav>

      {error && (
        <p className="navbar-error" role="alert">
          {error}
        </p>
      )}
    </>
  );
}

export default Navbar;
