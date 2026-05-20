import React, { createContext, useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './views/Layout';
import Wall from './views/Wall';
import Auctions from './views/Auctions';
import Profile from './views/Profile';
import Wallet from './views/Wallet';
import Login from './views/Login';

// Global Context Mock for Sprint 1
export const AppContext = createContext();

function App() {
  const [theme, setTheme] = useState('light');
  const [user, setUser] = useState(null); // null if not logged in
  const [balance, setBalance] = useState(100); // 100 créditos iniciales

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme(prev => prev === 'light' ? 'dark' : 'light');
  };

  return (
    <AppContext.Provider value={{ theme, toggleTheme, user, setUser, balance, setBalance }}>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={user ? <Layout /> : <Navigate to="/login" />}>
            <Route index element={<Wall />} />
            <Route path="auctions" element={<Auctions />} />
            <Route path="profile" element={<Profile />} />
            <Route path="wallet" element={<Wallet />} />
          </Route>
        </Routes>
      </Router>
    </AppContext.Provider>
  );
}

export default App;
