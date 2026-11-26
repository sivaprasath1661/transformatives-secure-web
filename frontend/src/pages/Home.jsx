
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import './Home.css';

const API = 'http://localhost:8181';

function Home() {
  const [role, setRole] = useState('GUEST');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const controller = new AbortController();

    async function checkRole() {
      try {
        const sessionResponse = await fetch(
          `${API}/api/auth/me`,
          {
            credentials: 'include',
            signal: controller.signal,
          }
        );

        if (sessionResponse.status === 401) {
          setRole('GUEST');
          return;
        }

        if (!sessionResponse.ok) {
          throw new Error('Session verification failed');
        }

        const adminResponse = await fetch(
          `${API}/api/admin/access`,
          {
            credentials: 'include',
            signal: controller.signal,
          }
        );

        if (adminResponse.ok) {
          setRole('ADMIN');
        } else if (adminResponse.status === 403) {
          setRole('CUSTOMER');
        } else if (adminResponse.status === 401) {
          setRole('GUEST');
        } else {
          throw new Error('Role verification failed');
        }
      } catch (error) {
        if (error.name !== 'AbortError') {
          // Do not assume a role when verification fails.
          setRole('UNKNOWN');
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    checkRole();

    return () => controller.abort();
  }, []);

  const isAdmin = role === 'ADMIN';
  const isCustomer = role === 'CUSTOMER';

  const features = isAdmin
    ? [
        {
          title: 'Manage shipments',
          description: 'View customer bookings in one dashboard.',
        },
        {
          title: 'Update delivery status',
          description: 'Manage shipments through their delivery stages.',
        },
        {
          title: 'Review audit history',
          description: 'Review recorded administrator status changes.',
        },
      ]
    : [
        {
          title: 'Book shipments',
          description: 'Create a delivery request in minutes.',
        },
        {
          title: 'Track progress',
          description: 'Find shipments using a tracking number.',
        },
        {
          title: 'Manage bookings',
          description: 'Review history and cancel pending shipments.',
        },
      ];

  return (
    <main className="home-page">
      <section className="home-hero">
        <div className="home-content">
          <span className="home-eyebrow">
            {isAdmin
              ? 'LOGISTICS ADMINISTRATION'
              : 'SECURE LOGISTICS PLATFORM'}
          </span>

          <h1>
            {isAdmin ? (
              <>
                Logistics operations,
                <span> simplified.</span>
              </>
            ) : (
              <>
                Shipment management,
                <span> made simple.</span>
              </>
            )}
          </h1>

          <p>
            {isAdmin
              ? 'Monitor customer shipments, manage delivery progress and review operational records securely from one central dashboard.'
              : 'TRANSFORMATIVES helps customers book, manage and track deliveries through a secure and reliable platform.'}
          </p>

          <div className="home-actions">
            {loading ? (
              <p>Checking your session...</p>
            ) : isAdmin ? (
              <Link
                to="/admin/shipments"
                className="home-primary"
              >
                Open Admin Dashboard
              </Link>
            ) : isCustomer ? (
              <>
                <Link
                  to="/shipments"
                  className="home-primary"
                >
                  Manage My Shipments
                </Link>

                <Link
                  to="/welcome"
                  className="home-secondary"
                >
                  My Dashboard
                </Link>
              </>
            ) : role === 'GUEST' ? (
              <>
                <Link
                  to="/login"
                  className="home-primary"
                >
                  Login
                </Link>

                <Link
                  to="/register"
                  className="home-secondary"
                >
                  Create an Account
                </Link>
              </>
            ) : (
              <p>
                Unable to verify your session.
                Please refresh the page.
              </p>
            )}
          </div>
        </div>

        <div className="home-feature-card">
          <div className="home-card-heading">
            <span
              className="home-card-icon"
              aria-hidden="true"
            >
              ↗
            </span>

            <div>
              <h2>
                {isAdmin
                  ? 'Operations at a glance.'
                  : 'One platform. Full visibility.'}
              </h2>

              <p>
                {isAdmin
                  ? 'Manage logistics securely.'
                  : 'Your deliveries, securely managed.'}
              </p>
            </div>
          </div>

          {features.map((feature, index) => (
            <div
              className="home-feature-row"
              key={feature.title}
            >
              <span>
                {String(index + 1).padStart(2, '0')}
              </span>

              <div>
                <strong>{feature.title}</strong>
                <p>{feature.description}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="home-highlights">
        <div>
          <h2>Secure by design</h2>
          <p>
            Role-based access and protected shipment
            information.
          </p>
        </div>

        <div>
          <h2>
            {isAdmin
              ? 'Operational visibility'
              : 'Simple tracking'}
          </h2>

          <p>
            {isAdmin
              ? 'Monitor shipment statuses and manage delivery progress.'
              : 'Track your deliveries and check their latest recorded status.'}
          </p>
        </div>

        <div>
          <h2>
            {isAdmin
              ? 'Accountability'
              : 'Easy management'}
          </h2>

          <p>
            {isAdmin
              ? 'Review recorded administrative status changes and their history.'
              : 'Manage shipment requests and review your booking history.'}
          </p>
        </div>
      </section>
    </main>
  );
}

export default Home;
