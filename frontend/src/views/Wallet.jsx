import React, { useContext } from 'react';
import { AppContext } from '../App';
import { Wallet as WalletIcon, ArrowUpRight, ArrowDownRight, Clock } from 'lucide-react';

const Wallet = () => {
  const { balance } = useContext(AppContext);

  const transactions = [
    { id: 1, type: 'ingreso', concept: 'Capital Semilla Inicial', amount: 100, date: '17/04/2026', status: 'Completado' },
    { id: 2, type: 'egreso', concept: 'Reserva para Paseo de Perros', amount: 15, date: 'Hoy', status: 'Retenido' },
  ];

  const retainedBalance = transactions
    .filter(t => t.status === 'Retenido' && t.type === 'egreso')
    .reduce((acc, t) => acc + t.amount, 0);

  const availableBalance = balance - retainedBalance;

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2>Mi Billetera</h2>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem', marginBottom: '2.5rem' }}>
        {/* Saldo Disponible */}
        <div className="card" style={{ backgroundColor: 'var(--accent-primary)', color: '#fff', border: 'none' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', opacity: 0.9 }}>
            <WalletIcon size={20} />
            <span style={{ fontSize: '1.125rem', fontWeight: '500' }}>Créditos Disponibles</span>
          </div>
          <div style={{ fontSize: '3rem', fontWeight: 'bold' }}>
            {availableBalance} <span style={{ fontSize: '1.5rem', fontWeight: 'normal', opacity: 0.8 }}>cr</span>
          </div>
        </div>

        {/* Saldo Retenido */}
        <div className="card" style={{ backgroundColor: 'var(--color-yellow-100)', color: 'var(--color-orange-600)', border: 'none' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', opacity: 0.9 }}>
            <Clock size={20} />
            <span style={{ fontSize: '1.125rem', fontWeight: '500' }}>Créditos Retenidos</span>
          </div>
          <div style={{ fontSize: '3rem', fontWeight: 'bold' }}>
            {retainedBalance} <span style={{ fontSize: '1.5rem', fontWeight: 'normal', opacity: 0.8 }}>cr</span>
          </div>
          <p style={{ fontSize: '0.75rem', marginTop: '0.5rem', opacity: 0.8 }}>
            Garantía para transacciones en curso (Épica 5).
          </p>
        </div>
      </div>

      <h3 style={{ marginBottom: '1rem', fontSize: '1.25rem' }}>Historial de Transacciones</h3>
      <div className="card" style={{ padding: '0' }}>
        {transactions.map((tx, idx) => (
          <div key={tx.id} style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center', 
            padding: '1rem 1.5rem',
            borderBottom: idx === transactions.length - 1 ? 'none' : '1px solid var(--border-color)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <div style={{ 
                width: '40px', 
                height: '40px', 
                borderRadius: '50%', 
                backgroundColor: tx.type === 'ingreso' ? 'var(--color-green-100)' : 'var(--color-yellow-100)',
                color: tx.type === 'ingreso' ? 'var(--color-green-700)' : 'var(--color-orange-600)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                {tx.type === 'ingreso' ? <ArrowDownRight size={20} /> : <ArrowUpRight size={20} />}
              </div>
              <div>
                <div style={{ fontWeight: '600' }}>{tx.concept}</div>
                <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)' }}>{tx.date}</div>
              </div>
            </div>

            <div style={{ textAlign: 'right' }}>
              <div style={{ 
                fontWeight: 'bold', 
                fontSize: '1.125rem',
                color: tx.type === 'ingreso' ? 'var(--accent-primary)' : 'var(--text-primary)'
              }}>
                {tx.type === 'ingreso' ? '+' : '-'}{tx.amount} cr
              </div>
              <div style={{ 
                fontSize: '0.75rem', 
                color: tx.status === 'Retenido' ? 'var(--color-orange-600)' : 'var(--text-tertiary)',
                fontWeight: tx.status === 'Retenido' ? 'bold' : 'normal'
              }}>
                {tx.status}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Wallet;
