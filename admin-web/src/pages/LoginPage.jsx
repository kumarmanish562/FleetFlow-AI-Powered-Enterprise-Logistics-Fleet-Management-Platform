import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider.tsx';
import { apiMessage } from '../api/client.js';
import { ArrowRight, Truck } from 'lucide-react';

const schema = z.object({ email: z.string().email('Enter a valid email address'), password: z.string().min(1, 'Password is required') });

export function LoginPage() {
  const { user, login } = useAuth();
  const location = useLocation();
  const [serverError, setServerError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { register, handleSubmit, formState: { errors } } = useForm({ resolver: zodResolver(schema), defaultValues: { email: '', password: '' } });
  if (user) {
    const roles=user.roles||[];
    const destination=location.state?.from||(
      roles.some((role)=>['ADMIN','SUPER_ADMIN'].includes(role))?'/':
      roles.includes('FINANCE_MANAGER')?'/analytics':
      roles.includes('CUSTOMER')?'/billing':
      roles.includes('DISPATCHER')?'/shipments':
      roles.includes('FLEET_MANAGER')?'/fleet':
      roles.includes('DRIVER')?'/fleet':'/notifications'
    );
    return <Navigate to={destination} replace />;
  }
  const submit = async (values) => {
    setServerError(''); setSubmitting(true);
    try { await login(values); }
    catch (error) { setServerError(apiMessage(error)); }
    finally { setSubmitting(false); }
  };
  return <div className="login-screen"><div className="login-aside"><div className="login-brand"><span className="brand-mark"><Truck size={19}/></span>fleetflow<span className="brand-dot">.</span></div><div className="login-pitch"><span className="eyebrow">FLEET OPERATIONS, IN SYNC</span><h1>Clarity across every mile.</h1><p>Bring your fleet, shipments, and people together in one operating view.</p><div className="login-rule"/><span className="login-aside-caption">FLEETFLOW OPERATIONS PLATFORM</span></div></div><main className="login-main"><div className="login-card"><div className="eyebrow">WELCOME BACK</div><h2>Sign in to FleetFlow</h2><p className="muted">Use your FleetFlow account to continue.</p><form onSubmit={handleSubmit(submit)} noValidate><label htmlFor="email">Work email</label><input id="email" type="email" autoComplete="username" placeholder="you@company.com" {...register('email')}/>{errors.email && <small className="field-error">{errors.email.message}</small>}<label htmlFor="password">Password</label><input id="password" type="password" autoComplete="current-password" placeholder="Enter your password" {...register('password')}/>{errors.password && <small className="field-error">{errors.password.message}</small>}{serverError && <div role="alert" className="form-error">{serverError}</div>}<button className="button button-primary login-submit" disabled={submitting}>{submitting ? 'Signing in…' : 'Sign in'} {!submitting && <ArrowRight size={17}/>}</button></form><div className="login-footnote">Secure access managed by your FleetFlow account</div></div><span className="login-copyright">© {new Date().getFullYear()} FleetFlow</span></main></div>;
}
