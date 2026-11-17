
import { NavLink } from 'react-router-dom';

function Navbar() {
  return (
    <nav className="navbar" aria-label="Main navigation">
      <div className="navbar-brand">TRANSFORMATIVES</div>

      <div className="navbar-links">
        <NavLink to="/">Home</NavLink>
        <NavLink to="/login">Login</NavLink>
        <NavLink to="/register">Register</NavLink>
      </div>
    </nav>
  );
}

export default Navbar;
