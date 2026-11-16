import Navbar from './components/Navbar';
import './App.css';

function App() {
  return (
    <div className="app">
      <Navbar />

      <main>
        <h1>Welcome to TRANSFORMATIVES</h1>
        <p>
          A platform for managing shipments,
          customers and carriers securely.
        </p>
      </main>
    </div>
  );
}

export default App;