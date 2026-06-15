import React, { createContext, useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// Importar clases de la arquitectura (Modelos y Controladores)
import { ClienteHttp } from './servicios_api/clienteHttp';
import { ServicioUsuario } from './servicios_api/ServicioUsuario';
import { ServicioPublicacion } from './servicios_api/ServicioPublicacion';
import { ServicioSubasta } from './servicios_api/ServicioSubasta';
import { ControladorAutenticacion } from './controladores/ControladorAutenticacion';
import { ControladorMuro } from './controladores/ControladorMuro';
import { ControladorSubasta } from './controladores/ControladorSubasta';
import { ControladorPerfil } from './controladores/ControladorPerfil';

// Vistas
import IniciarSesion from './vistas/IniciarSesion';
import PlantillaPrincipal from './vistas/PlantillaPrincipal';
import Wall from './vistas/Wall';
import Auctions from './vistas/Auctions';
import Profile from './vistas/Profile';
import Wallet from './vistas/Wallet';
import Publish from './vistas/Publish';
import CreateAuction from './vistas/CreateAuction';
import ChangePassword from './vistas/ChangePassword';
import Notifications from './vistas/Notifications';
import PostDetails from './vistas/PostDetails';
import MakeRequest from './vistas/MakeRequest';
import CatalogOnboarding from './vistas/CatalogOnboarding';

// Contexto Global que actuará como Inyector de Dependencias
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

  // Efectos de Persistencia
  useEffect(() => { document.documentElement.setAttribute('data-theme', theme); }, [theme]);
  useEffect(() => { user ? localStorage.setItem('entreNosUser', JSON.stringify(user)) : localStorage.removeItem('entreNosUser'); }, [user]);
  useEffect(() => { localStorage.setItem('entreNosBalance', JSON.stringify(balance)); }, [balance]);
  useEffect(() => { localStorage.setItem('entreNosCatalog', JSON.stringify(hasCatalog)); }, [hasCatalog]);

  const toggleTheme = () => setTheme(prev => prev === 'light' ? 'dark' : 'light');

  // Callback para permitir a los controladores mutar el estado global de React
  const setContextState = React.useCallback((key, value) => {
    switch(key) {
      case 'user': setUser(value); break;
      case 'balance': setBalance(value); break;
      case 'hasCatalog': setHasCatalog(value); break;
      default: break;
    }
  }, []);

  // --- ENSAMBLAJE DE DEPENDENCIAS (Dependency Injection Container) ---
  const { controladorAutenticacion, controladorMuro, controladorSubasta, controladorPerfil } = React.useMemo(() => {
    const clienteHttp = new ClienteHttp('http://localhost:8080/api');
    const servicioUsuario = new ServicioUsuario(clienteHttp);
    const servicioPublicacion = new ServicioPublicacion(clienteHttp);
    const servicioSubasta = new ServicioSubasta(clienteHttp);

    const controladorAutenticacion = new ControladorAutenticacion(servicioUsuario, setContextState);
    const controladorMuro = new ControladorMuro(servicioPublicacion);
    const controladorSubasta = new ControladorSubasta(servicioSubasta);
    const controladorPerfil = new ControladorPerfil(servicioUsuario, servicioPublicacion);

    return { controladorAutenticacion, controladorMuro, controladorSubasta, controladorPerfil };
  }, [setContextState]);

  const contextValue = {
    theme, toggleTheme, user, balance, hasCatalog, setUser, setBalance, setHasCatalog,
    controladorAutenticacion, controladorMuro, controladorSubasta, controladorPerfil
  };

  return (
    <AppContext.Provider value={contextValue}>
      <Router>
        <Routes>
          <Route path="/login" element={<IniciarSesion />} />
          <Route path="/onboarding" element={user ? <CatalogOnboarding /> : <Navigate to="/login" />} />
          <Route path="/" element={user ? <PlantillaPrincipal /> : <Navigate to="/login" />}>
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
