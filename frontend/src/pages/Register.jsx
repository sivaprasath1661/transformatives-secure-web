
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

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
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

    if (form.password.length < 8) {
      setError('Password must contain at least 8 characters.');
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setMessage('To be impl...');
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

        {error && <p className="register-error" role="alert">{error}</p>}
        {message && <p className="register-message" role="status">{message}</p>}

        <button type="submit" className="register-button">
          Register
        </button>
      </form>
    </main>
  );
}

export default Register;
