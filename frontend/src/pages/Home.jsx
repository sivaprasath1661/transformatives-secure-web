
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
          if (!controller.signal.aborted) {
            setRole('GUEST');
          }
          return;
        }

        if (!sessionResponse.ok) {
          throw new Error('Session verification failed');
        }

        const [adminResponse, courierResponse] = await Promise.all([
          fetch(`${API}/api/admin/access`, {
            credentials: 'include',
            signal: controller.signal,
          }),
          fetch(`${API}/api/courier/access`, {
            credentials: 'include',
            signal: controller.signal,
          }),
        ]);

        if (
          adminResponse.status === 401 ||
          courierResponse.status === 401
        ) {
          if (!controller.signal.aborted) {
            setRole('GUEST');
          }
          return;
        }

        if (
          !(adminResponse.ok || adminResponse.status === 403) ||
          !(courierResponse.ok || courierResponse.status === 403)
        ) {
          throw new Error('Role verification failed');
        }

        if (!controller.signal.aborted) {
          if (adminResponse.ok) {
            setRole('ADMIN');
          } else if (courierResponse.ok) {
            setRole('COURIER');
          } else {
            setRole('CUSTOMER');
          }
        }
      } catch (error) {
        if (error.name !== 'AbortError' && !controller.signal.aborted) {
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
  const isCourier = role === 'COURIER';
  const isCustomer = role === 'CUSTOMER';

  const features = isAdmin
    ? [
        {
          title: 'Manage shipments',
          description:
            'View customer bookings in one dashboard.',
        },
        {
          title: 'Assign delivery partners',
          description:
            'Allocate pending shipments to authorised couriers.',
        },
        {
          title: 'Review audit history',
          description:
            'Track recorded courier assignments and delivery updates.',
        },
      ]
    : isCourier
      ? [
          {
            title: 'Assigned deliveries',
            description:
              'View shipments allocated to your courier account.',
          },
          {
            title: 'Start delivery',
            description:
              'Update pending shipments when delivery begins.',
          },
          {
            title: 'Confirm completion',
            description:
              'Mark delivered orders and maintain accurate tracking records.',
          },
        ]
      : [
          {
            title: 'Book shipments',
            description:
              'Create a delivery request in minutes.',
          },
          {
            title: 'Track progress',
            description:
              'Find shipments using a tracking number.',
          },
          {
            title: 'Manage bookings',
            description:
              'Review history and cancel pending shipments.',
          },
        ];

  return (
    <main className="home-page">
      <section className="home-hero">
        <div className="home-content">
          <span className="home-eyebrow">
            {isAdmin
              ? 'LOGISTICS ADMINISTRATION'
              : isCourier
                ? 'DELIVERY PARTNER PORTAL'
                : 'SECURE LOGISTICS PLATFORM'}
          </span>

          <h1>
            {isAdmin ? (
              <>
                Logistics operations,
                <span> simplified.</span>
              </>
            ) : isCourier ? (
              <>
                Delivery management,
                <span> made simple.</span>
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
              ? 'Monitor customer shipments, assign delivery partners and review operational records securely from one central dashboard.'
              : isCourier
                ? 'Access your assigned shipments, manage active deliveries and record completed orders securely through TRANSFORMATIVES.'
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
            ) : isCourier ? (
              <>
                <Link
                  to="/courier/shipments"
                  className="home-primary"
                >
                  Open Courier Dashboard
                </Link>

                <Link
                  to="/welcome"
                  className="home-secondary"
                >
                  My Dashboard
                </Link>
              </>
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
                  : isCourier
                    ? 'Your deliveries at a glance.'
                    : 'One platform. Full visibility.'}
              </h2>

              <p>
                {isAdmin
                  ? 'Manage logistics securely.'
                  : isCourier
                    ? 'Assigned shipments, securely managed.'
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
              : isCourier
                ? 'Assigned shipment visibility'
                : 'Simple tracking'}
          </h2>

          <p>
            {isAdmin
              ? 'Monitor shipment statuses and manage delivery progress.'
              : isCourier
                ? 'View deliveries assigned specifically to your courier account.'
                : 'Track your deliveries and check their latest recorded status.'}
          </p>
        </div>

        <div>
          <h2>
            {isAdmin
              ? 'Accountability'
              : isCourier
                ? 'Delivery updates'
                : 'Easy management'}
          </h2>

          <p>
            {isAdmin
              ? 'Review recorded shipment assignments and status changes.'
              : isCourier
                ? 'Start deliveries and confirm completed orders with recorded status updates.'
                : 'Manage shipment requests and review your booking history.'}
          </p>
        </div>
      </section>
    </main>
  );
}

export default Home;
