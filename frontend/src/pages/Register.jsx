
import { useState } from 'react';
import './Register.css';

function Register() {
  const [form, setForm] = useState({
    name: '',
    email: '',
    username: '',
    password: '',
    confirmPassword: '',
  });

  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm((previous) => ({
      ...previous,
      [e.target.name]: e.target.value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;

    setError('');
    setMessage('');

    if (Object.values(form).some((value) => !value.trim())) {
      setError('All fields are required.');
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      setError('Enter a valid email address.');
      return;
    }

    if (form.username.trim().length < 3) {
      setError('Username must contain at least 3 characters.');
      return;
    }

    if (form.password.length < 8) {
      setError('Password must contain at least 8 characters.');
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);

    try {
      // Obtain the CSRF token and associated session cookie
      const csrfResponse = await fetch(
        'http://localhost:8181/api/auth/csrf',
        {
          credentials: 'include',
        }
      );

      if (!csrfResponse.ok) {
        throw new Error('Unable to retrieve CSRF token');
      }

      const csrf = await csrfResponse.json();

      // Submit registration with CSRF protection
      const response = await fetch(
        'http://localhost:8181/api/auth/register',
        {
          method: 'POST',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
            [csrf.headerName]: csrf.token,
          },
          body: JSON.stringify({
            fullName: form.name.trim(),
            email: form.email.trim(),
            username: form.username.trim(),
            password: form.password,
          }),
        }
      );

      if (!response.ok) {
        if (response.status === 409) {
          setError('Username or email is already registered.');
        } else if (response.status === 400) {
          setError('Invalid registration details. Please check your inputs.');
        } else if (response.status === 403) {
          setError('Security verification failed. Please try again.');
        } else {
          setError('Registration failed. Please try again.');
        }
        return;
      }

      const data = await response.json();

      setMessage(`Account created successfully for ${data.username}!`);

      setForm({
        name: '',
        email: '',
        username: '',
        password: '',
        confirmPassword: '',
      });

    } catch {
      setError('Unable to connect to the server. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="register-page">
      <form className="register-form" onSubmit={handleSubmit}>
        <h1>Create Account</h1>

        <label htmlFor="name">Full Name</label>
        <input
          id="name"
          name="name"
          value={form.name}
          onChange={handleChange}
          autoComplete="name"
        />

        <label htmlFor="email">Email</label>
        <input
          id="email"
          name="email"
          type="email"
          value={form.email}
          onChange={handleChange}
          autoComplete="email"
        />

        <label htmlFor="regUsername">Username</label>
        <input
          id="regUsername"
          name="username"
          value={form.username}
          onChange={handleChange}
          autoComplete="username"
        />

        <label htmlFor="regPassword">Password</label>
        <input
          id="regPassword"
          name="password"
          type="password"
          value={form.password}
          onChange={handleChange}
          autoComplete="new-password"
        />

        <label htmlFor="confirmPassword">Confirm Password</label>
        <input
          id="confirmPassword"
          name="confirmPassword"
          type="password"
          value={form.confirmPassword}
          onChange={handleChange}
          autoComplete="new-password"
        />

        {error && (
          <p className="register-error" role="alert">{error}</p>
        )}

        {message && (
          <p className="register-message" role="status">{message}</p>
        )}

        <button
          type="submit"
          className="register-button"
          disabled={loading}
        >
          {loading ? 'Registering...' : 'Register'}
        </button>
      </form>
    </main>
  );
}

export default Register;
