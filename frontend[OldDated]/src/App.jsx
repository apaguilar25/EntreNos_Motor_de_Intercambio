import React, { createContext, useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './views/Layout';
import Wall from './views/Wall';
import Auctions from './views/Auctions';
import Profile from './views/Profile';
import Wallet from './views/Wallet';
import Login from './views/Login';
import Publish from './views/Publish';
import CreateAuction from './views/CreateAuction';
import ChangePassword from './views/ChangePassword';
import Notifications from './views/Notifications';
import PostDetails from './views/PostDetails';
import MakeRequest from './views/MakeRequest';
import CatalogOnboarding from './views/CatalogOnboarding';

// Global Context Mock for Sprint 1
export const AppContext = createContext();

function App() {
  const [theme, setTheme] = useState('light');
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('entreNosUser');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [balance, setBalance] = useState(() => {
    const savedBalance = localStorage.getItem('entreNosBalance');
    return savedBalance ? JSON.parse(savedBalance) : 0;
  });
  const [hasCatalog, setHasCatalog] = useState(() => {
    const savedCatalog = localStorage.getItem('entreNosCatalog');
    return savedCatalog ? JSON.parse(savedCatalog) : false;
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  useEffect(() => {
    if (user) {
      localStorage.setItem('entreNosUser', JSON.stringify(user));
    } else {
      localStorage.removeItem('entreNosUser');
    }
  }, [user]);

  useEffect(() => {
    localStorage.setItem('entreNosBalance', JSON.stringify(balance));
  }, [balance]);

  useEffect(() => {
    localStorage.setItem('entreNosCatalog', JSON.stringify(hasCatalog));
  }, [hasCatalog]);

  const toggleTheme = () => {
    setTheme(prev => prev === 'light' ? 'dark' : 'light');
  };

  return (
    <AppContext.Provider value={{ theme, toggleTheme, user, setUser, balance, setBalance, hasCatalog, setHasCatalog }}>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/onboarding" element={user ? <CatalogOnboarding /> : <Navigate to="/login" />} />
          <Route path="/" element={user ? <Layout /> : <Navigate to="/login" />}>
            <Route index element={<Wall />} />
            <Route path="auctions" element={<Auctions />} />
            <Route path="profile" element={<Profile />} />
            <Route path="wallet" element={<Wallet />} />
            <Route path="publish" element={<Publish />} />
            <Route path="create-auction" element={<CreateAuction />} />
            <Route path="settings/password" element={<ChangePassword />} />
            <Route path="notifications" element={<Notifications />} />
            <Route path="post/:id" element={<PostDetails />} />
            <Route path="request/:id" element={<MakeRequest />} />
          </Route>
        </Routes>
      </Router>
    </AppContext.Provider>
  );
}

export default App;
