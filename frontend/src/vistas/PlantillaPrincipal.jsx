import React, { useContext } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { AppContext } from '../App';
import { Home, Gavel, User, Wallet, LogOut, Sun, Moon, Bell } from 'lucide-react';

const PlantillaPrincipal = () => {
  const { theme, toggleTheme, controladorAutenticacion, balance } = useContext(AppContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    // Aquí el controlador en un futuro podría llamar a un servicio de logout real
    // Por ahora simplemente seteamos el user a null mediante el estado compartido
    controladorAutenticacion.setContextState('user', null);
    navigate('/login');
  };

  const navItems = [
    { to: "/", icon: <Home size={20} />, label: "Muro" },
    { to: "/auctions", icon: <Gavel size={20} />, label: "Subastas" },
    { to: "/profile", icon: <User size={20} />, label: "Perfil" },
    { to: "/wallet", icon: <Wallet size={20} />, label: `Billetera (${balance})` },
    { to: "/notifications", icon: <Bell size={20} />, label: "Notificaciones" }
  ];

  return (
    <div className="app-container">
      {/* Sidebar PC */}
      <aside className="sidebar">
        <div style={{ padding: '0 1rem', marginBottom: '2rem' }}>
          <h2 style={{ color: 'var(--accent-primary)', fontSize: '1.5rem', fontWeight: 'bold' }}>entreNos</h2>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)' }}>Plaza Alameda</p>
        </div>
        
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', flex: 1 }}>
          {navItems.map(item => (
            <NavLink 
              key={item.to} 
              to={item.to} 
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
            >
              {item.icon}
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <button onClick={toggleTheme} className="nav-link" style={{ background: 'transparent', textAlign: 'left', width: '100%' }}>
            {theme === 'light' ? <Moon size={20} /> : <Sun size={20} />}
            <span>Tema {theme === 'light' ? 'Oscuro' : 'Claro'}</span>
          </button>
          
          <button onClick={handleLogout} className="nav-link" style={{ background: 'transparent', color: 'var(--accent-warning)', textAlign: 'left', width: '100%' }}>
            <LogOut size={20} />
            <span>Cerrar Sesión</span>
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="main-content">
        <Outlet />
      </main>

      {/* Mobile Floating Menu */}
      <nav className="mobile-nav">
        {navItems.map(item => (
          <NavLink 
            key={item.to} 
            to={item.to} 
            className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
            style={{ padding: '0.5rem' }}
          >
            {item.icon}
            <span style={{ fontSize: '0.7rem' }}>{item.label.split(' ')[0]}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
};

export default PlantillaPrincipal;
