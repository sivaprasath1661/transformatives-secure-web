
import { Link } from 'react-router-dom';
import './Welcome.css';

function Welcome() {
  return (
    <main className="welcome-page">
      <section className="welcome-card">
        <div className="welcome-icon">✓</div>

        <h1>Welcome to TRANSFORMATIVES</h1>

        <p>Your login credentials have been verified successfully.</p>

        <p className="welcome-description">
          Your logistics management journey starts here.
          Shipment booking and tracking features are coming soon.
        </p>

        <Link to="/" className="welcome-home">
          Back to Home
        </Link>
      </section>
    </main>
  );
}

export default Welcome;
