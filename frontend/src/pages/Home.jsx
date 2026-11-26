
import { Link } from 'react-router-dom';
import './Home.css';

function Home() {
  return (
    <main className="home-page">
      <section className="home-hero">
        <div className="home-content">
          <span className="home-eyebrow">
            SECURE LOGISTICS PLATFORM
          </span>

          <h1>
            Shipment management,
            <span> made simple.</span>
          </h1>

          <p>
            TRANSFORMATIVES helps customers book,
            manage and track deliveries through a
            secure and reliable platform.
          </p>

          <div className="home-actions">
            <Link to="/shipments" className="home-primary">
              Manage My Shipments
            </Link>

            <Link to="/register" className="home-secondary">
              Create an Account
            </Link>
          </div>
        </div>

        <div className="home-feature-card">
          <div className="home-card-heading">
            <span className="home-card-icon">↗</span>
            <div>
              <h2>One platform. Full visibility.</h2>
              <p>Your deliveries, securely managed.</p>
            </div>
          </div>

          <div className="home-feature-row">
            <span>01</span>
            <div>
              <strong>Book shipments</strong>
              <p>Create a delivery request in minutes.</p>
            </div>
          </div>

          <div className="home-feature-row">
            <span>02</span>
            <div>
              <strong>Track progress</strong>
              <p>Find shipments using a tracking number.</p>
            </div>
          </div>

          <div className="home-feature-row">
            <span>03</span>
            <div>
              <strong>Manage bookings</strong>
              <p>Review history and cancel pending shipments.</p>
            </div>
          </div>
        </div>
      </section>

      <section className="home-highlights">
        <div>
          <h2>Secure by design</h2>
          <p>
            Authenticated access and account-specific
            shipment information.
          </p>
        </div>

        <div>
          <h2>Simple tracking</h2>
          <p>
            Track your deliveries and check their
            latest recorded status.
          </p>
        </div>

        <div>
          <h2>Easy management</h2>
          <p>
            Manage shipment requests and review
            your booking history.
          </p>
        </div>
      </section>
    </main>
  );
}

export default Home;
