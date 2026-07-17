import React, { useContext, useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { AppContext } from '../App';
import { ToastContext } from '../contextos/ToastContext';
import { Wallet as WalletIcon, Clock, Star, ArrowUpRight, ArrowDownRight } from 'lucide-react';

const Wallet = () => {
  const { user, setBalance, controladorPerfil } = useContext(AppContext);
  const { addToast } = useContext(ToastContext);

  const [transactions, setTransactions] = useState([]);
  const [availableBalance, setAvailableBalance] = useState(0);
  const [retainedBalance, setRetainedBalance] = useState(0);
  const [pubsMap, setPubsMap] = useState({});
  const [usersMap, setUsersMap] = useState({});
  const [resenasMap, setResenasMap] = useState({});

  useEffect(() => {
    const fetchData = async () => {
      if (!user?.id) return;
      try {
        const monedero = await controladorPerfil.obtenerSaldo(user.id);
        setAvailableBalance(monedero.creditosDisponibles || 0);
        setRetainedBalance(monedero.creditosComprometidos || monedero.creditosRetenidos || 0);
        if (setBalance) {
            setBalance(monedero.creditosDisponibles || 0);
        }
      } catch (err) {
        console.error("Error cargando monedero:", err);
      }
      try {
        const resTx = await fetch('http://localhost:8080/api/transacciones');
        if (resTx.ok) {
          const allTx = await resTx.json();
          const userTx = allTx.filter(t =>
            (t.idDemandante === user.id || t.idOfertante === user.id) &&
            t.estado === 'FINALIZADA'
          );
          setTransactions(userTx);
        }
      } catch (err) {
        console.error("Error cargando transacciones:", err);
      }
      try {
        const resPubs = await fetch('http://localhost:8080/api/publicaciones');
        if (resPubs.ok) {
          const pubsData = await resPubs.json();
          const pMap = {};
          pubsData.forEach(p => pMap[p.idPublicacion] = p);
          setPubsMap(pMap);
          const uMap = {};
          Object.values(pMap).forEach(p => {
            if (p.idUsuario && p.nombreUsuario) uMap[p.idUsuario] = p.nombreUsuario;
          });
          setUsersMap(uMap);
        }
      } catch (err) {
        console.error("Error cargando publicaciones:", err);
      }
      try {
        const resResenas = await fetch('http://localhost:8080/api/resenas');
        if (resResenas.ok) {
          const resenasData = await resResenas.json();
          const rMap = {};
          resenasData.forEach(r => {
            if (!rMap[r.idTransaccion]) rMap[r.idTransaccion] = [];
            rMap[r.idTransaccion].push(r);
          });
          setResenasMap(rMap);
        }
      } catch (err) {
        console.error("Error cargando reseñas:", err);
      }
    };
    fetchData();
  }, [user, controladorPerfil, setBalance]);

  const formatDate = (millis) => {
    if (!millis) return '-';
    const d = new Date(millis);
    return d.toLocaleDateString('es-ES', { year: 'numeric', month: 'short', day: 'numeric' });
  };

  return (
    <div className="animate-in" style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2>Mi Billetera</h2>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem', marginBottom: '2.5rem' }}>
        <div className="card" style={{ backgroundColor: 'var(--accent-primary)', color: 'var(--text-on-accent)', border: 'none' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', opacity: 0.9 }}>
            <WalletIcon size={20} />
            <span style={{ fontSize: '1.125rem', fontWeight: '500' }}>Créditos Disponibles</span>
          </div>
          <div style={{ fontSize: '3rem', fontWeight: 'bold' }}>
            {availableBalance} <span style={{ fontSize: '1.5rem', fontWeight: 'normal', opacity: 0.8 }}>cr</span>
          </div>
        </div>

        <div className="card" style={{ backgroundColor: 'var(--bg-warning-soft)', color: 'var(--text-on-warning-soft)', border: 'none' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', opacity: 0.9 }}>
            <Clock size={20} />
            <span style={{ fontSize: '1.125rem', fontWeight: '500' }}>Créditos Comprometidos</span>
          </div>
          <div style={{ fontSize: '3rem', fontWeight: 'bold' }}>
            {retainedBalance} <span style={{ fontSize: '1.5rem', fontWeight: 'normal', opacity: 0.8 }}>cr</span>
          </div>
        </div>
      </div>

      <h3 style={{ marginBottom: '1rem', fontSize: '1.25rem' }}>Historial de Transacciones</h3>
      {transactions.length === 0 ? (
        <div className="card" style={{ padding: '1.5rem', textAlign: 'center', color: 'var(--text-tertiary)' }}>
          <p style={{ fontStyle: 'italic', marginBottom: '0.5rem' }}>Aún no hay transacciones finalizadas en tu historial.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {transactions.map((tx, i) => {
            const esOfertante = tx.idOfertante === user.id;
            const contraparte = usersMap[esOfertante ? tx.idDemandante : tx.idOfertante] || 'Usuario';
            const nombreServicio = pubsMap[tx.idPublicacion]?.nombreServicio || pubsMap[tx.idPublicacion]?.titulo || tx.idPublicacion;
            const resenas = resenasMap[tx.idTransaccion] || [];
            return (
              <div key={i} className="card" style={{ padding: '1rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: resenas.length > 0 ? '0.75rem' : 0 }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                      {esOfertante ? <ArrowUpRight size={16} color="var(--accent-primary)" /> : <ArrowDownRight size={16} color="var(--color-red-600)" />}
                      <strong style={{ fontSize: '0.95rem' }}>{nombreServicio}</strong>
                    </div>
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-tertiary)', margin: 0 }}>
                      {tx.idTransaccion.split('-')[0].toUpperCase()} &middot; {contraparte} &middot; {formatDate(tx.fechaCreacion)}
                    </p>
                  </div>
                  <div style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                    <span style={{ fontWeight: 'bold', color: esOfertante ? 'var(--accent-primary)' : 'var(--text-primary)' }}>
                      {esOfertante ? '+' : '-'}{(pubsMap[tx.idPublicacion]?.precioCreditos || tx.creditosRetenidos || 0)} cr
                    </span>
                    <br />
                    <span style={{ fontSize: '0.75rem', padding: '0.15rem 0.4rem', borderRadius: '0.25rem', backgroundColor: 'var(--color-green-100)', color: 'var(--color-green-700)' }}>
                      {tx.estado}
                    </span>
                  </div>
                </div>
                {resenas.length > 0 && (
                  <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '0.75rem' }}>
                    {resenas.filter(r => r.idAutor !== user.id).map((r, ri) => (
                      <div key={ri} style={{ display: 'flex', alignItems: 'flex-start', gap: '0.5rem', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                        <Star size={14} style={{ flexShrink: 0, marginTop: '0.15rem' }} fill={r.calificacion >= 3 ? 'var(--color-yellow-500)' : 'var(--text-tertiary)'} color={r.calificacion >= 3 ? 'var(--color-yellow-500)' : 'var(--text-tertiary)'} />
                        <div>
                          <span style={{ fontWeight: '600' }}>{r.calificacion}/5</span> &mdash; {r.comentario}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Wallet;
