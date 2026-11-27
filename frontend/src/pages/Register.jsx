
import { useState } from 'react';
import './Register.css';

const API = 'http://localhost:8181';

const emptyForm = {
  name: '',
  email: '',
  username: '',
  password: '',
  confirmPassword: '',
};

const passwordRequirements = [
  {
    label: 'At least 12 characters',
    check: (password) => password.length >= 12,
  },
  {
    label: 'One uppercase letter (A-Z)',
    check: (password) => /[A-Z]/.test(password),
  },
  {
    label: 'One lowercase letter (a-z)',
    check: (password) => /[a-z]/.test(password),
  },
  {
    label: 'One number (0-9)',
    check: (password) => /[0-9]/.test(password),
  },
  {
    label: 'One special character',
    check: (password) => /[^A-Za-z0-9]/.test(password),
  },
];

function Register() {
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] =
    useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));

    setError('');
    setMessage('');
  };

  const passwordBytes = new TextEncoder()
    .encode(form.password).length;

  const rules = passwordRequirements.map((rule) => ({
    label: rule.label,
    passed: rule.check(form.password),
  }));

  const passedRules = rules.filter(
    (rule) => rule.passed
  ).length;

  const validPassword =
    passedRules === passwordRequirements.length &&
    passwordBytes <= 72;

  const passwordsMatch =
    form.confirmPassword.length > 0 &&
    form.password === form.confirmPassword;

  const formComplete =
    Object.values(form).every((value) => value.trim()) &&
    validPassword &&
    passwordsMatch;

  const handleSubmit = async (event) => {
    event.preventDefault();

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

    if (!validPassword) {
      setError(
        'Password does not meet the security requirements.'
      );
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);

    try {
      const csrfResponse = await fetch(
        `${API}/api/auth/csrf`,
        {
          credentials: 'include',
        }
      );

      if (!csrfResponse.ok) {
        throw new Error('Unable to retrieve CSRF token');
      }

      const csrf = await csrfResponse.json();

      const response = await fetch(
        `${API}/api/auth/register`,
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
          setError(
            'Invalid registration details or password. Please check your inputs.'
          );
        } else if (response.status === 403) {
          setError(
            'Security verification failed. Please try again.'
          );
        } else {
          setError('Registration failed. Please try again.');
        }

        return;
      }

      const data = await response.json();

      setMessage(
        `Account created successfully for ${data.username}!`
      );

      setForm(emptyForm);
      setShowPassword(false);
      setShowConfirmPassword(false);
    } catch {
      setError(
        'Unable to connect to the server. Please try again.'
      );
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
          required
        />

        <label htmlFor="email">Email</label>
        <input
          id="email"
          name="email"
          type="email"
          value={form.email}
          onChange={handleChange}
          autoComplete="email"
          required
        />

        <label htmlFor="regUsername">Username</label>
        <input
          id="regUsername"
          name="username"
          value={form.username}
          onChange={handleChange}
          autoComplete="username"
          minLength={3}
          required
        />

        <label htmlFor="regPassword">Password</label>

        <div className="password-input-wrapper">
          <input
            id="regPassword"
            name="password"
            type={showPassword ? 'text' : 'password'}
            value={form.password}
            onChange={handleChange}
            autoComplete="new-password"
            aria-describedby="password-requirements"
            required
          />

          <button
            type="button"
            className="password-toggle"
            onClick={() =>
              setShowPassword((previous) => !previous)
            }
            aria-label={
              showPassword ? 'Hide password' : 'Show password'
            }
            aria-pressed={showPassword}
          >
            {showPassword ? 'Hide' : 'Show'}
          </button>
        </div>

        <div
          className="password-strength"
          id="password-requirements"
        >
          <div className="password-strength-heading">
            <strong>Password strength</strong>

            <span>
              {form.password.length === 0
                ? 'Not entered'
                : validPassword
                  ? 'Requirements met'
                  : `${passedRules}/5 requirements`}
            </span>
          </div>

          <div
            className="password-strength-bars"
            aria-label={`${passedRules} of 5 requirements satisfied`}
          >
            {passwordRequirements.map((rule, index) => (
              <span
                key={rule.label}
                className={
                  index < passedRules
                    ? validPassword
                      ? 'strength-bar strong'
                      : 'strength-bar active'
                    : 'strength-bar'
                }
              />
            ))}
          </div>

          <div className="password-checklist">
            {rules.map((rule) => (
              <div
                key={rule.label}
                className={`password-rule ${
                  rule.passed ? 'rule-passed' : ''
                }`}
              >
                <span aria-hidden="true">
                  {rule.passed ? '✓' : '○'}
                </span>

                <span>{rule.label}</span>
              </div>
            ))}

            {passwordBytes > 72 && (
              <div className="password-rule rule-error">
                <span aria-hidden="true">!</span>
                <span>
                  Password must not exceed 72 UTF-8 bytes
                </span>
              </div>
            )}
          </div>
        </div>

        <label htmlFor="confirmPassword">
          Confirm Password
        </label>

        <div className="password-input-wrapper">
          <input
            id="confirmPassword"
            name="confirmPassword"
            type={
              showConfirmPassword ? 'text' : 'password'
            }
            value={form.confirmPassword}
            onChange={handleChange}
            autoComplete="new-password"
            required
          />

          <button
            type="button"
            className="password-toggle"
            onClick={() =>
              setShowConfirmPassword((previous) => !previous)
            }
            aria-label={
              showConfirmPassword
                ? 'Hide confirmation password'
                : 'Show confirmation password'
            }
            aria-pressed={showConfirmPassword}
          >
            {showConfirmPassword ? 'Hide' : 'Show'}
          </button>
        </div>

        {form.confirmPassword.length > 0 && (
          <p
            className={
              passwordsMatch
                ? 'password-match success'
                : 'password-match mismatch'
            }
          >
            {passwordsMatch
              ? '✓ Passwords match'
              : 'Passwords do not match'}
          </p>
        )}

        {error && (
          <p className="register-error" role="alert">
            {error}
          </p>
        )}

        {message && (
          <p className="register-message" role="status">
            {message}
          </p>
        )}

        <button
          type="submit"
          className="register-button"
          disabled={loading || !formComplete}
        >
          {loading ? 'Registering...' : 'Register'}
        </button>
      </form>
    </main>
  );
}

export default Register;
