
import { useState } from 'react';
import './Login.css';
import { useNavigate } from 'react-router-dom';


async function getCsrfToken() {
  const response = await fetch('http://localhost:8181/api/auth/csrf', {
    credentials: 'include',
  });

  if (!response.ok) {
    throw new Error('Unable to obtain CSRF token');
  }

  return response.json();
}

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  
const handleSubmit = async (e) => {
  e.preventDefault();
  if (loading) return;

  setError('');

  if (!username.trim() || !password) {
    setError('Username and password are required.');
    return;
  }

  setLoading(true);

  try {
    // Get the CSRF token before submitting credentials
    const csrf = await getCsrfToken();

    const response = await fetch(
      'http://localhost:8181/api/auth/login',
      {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          [csrf.headerName]: csrf.token,
        },
        body: JSON.stringify({
          username: username.trim(),
          password,
        }),
      }
    );

    if (response.status === 401) {
      setError('Invalid username or password.');
      return;
    }

    if (!response.ok) {
      setError('Login failed. Please try again.');
      return;
    }

    setPassword('');
    navigate('/welcome');

  } catch {
    setError('Unable to connect or complete secure login.');
  } finally {
    setLoading(false);
  }
};

  return (
    <main className="login-page">
      <form className="login-form" onSubmit={handleSubmit}>
        <h1>Login</h1>

        <label htmlFor="username">Username</label>
        <input
          id="username"
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoComplete="username"
        />

        <label htmlFor="password">Password</label>
        <div className="password-field">
          <input
            id="password"
            type={showPassword ? 'text' : 'password'}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
          >
            {showPassword ? 'Hide' : 'Show'}
          </button>
        </div>

        {error && (
          <p className="login-error" role="alert">{error}</p>
        )}

        <button
          type="submit"
          className="login-button"
          disabled={loading}
        >
          {loading ? 'Verifying...' : 'Login'}
        </button>
      </form>
    </main>
  );
}

export default Login;
