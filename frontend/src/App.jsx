
import { BrowserRouter, Routes, Route } from 'react-router-dom';

import Navbar from './components/Navbar';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Welcome from './pages/Welcome';
import Shipments from './pages/Shipments';
import AdminShipments from './pages/AdminShipments';
import CourierShipments from './pages/CourierShipments';

import './App.css';

function App() {
  return (
    <BrowserRouter>
      <div className="app">
        <Navbar />

        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/welcome" element={<Welcome />} />
          <Route path="/shipments" element={<Shipments />} />

          <Route
            path="/admin/shipments"
            element={<AdminShipments />}
          />

          <Route
            path="/courier/shipments"
            element={<CourierShipments />}
          />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
