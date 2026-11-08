import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AppShell } from './AppShell.tsx';

const logout = vi.fn();
const toggleTheme = vi.fn();
vi.mock('../auth/AuthProvider.tsx', () => ({ useAuth: () => ({ user: { email:'ops@example.test', firstName:'Ops', roles:['ADMIN'] }, logout }) }));
vi.mock('../context/ThemeContext.tsx', () => ({ useTheme: () => ({ theme:'light', toggleTheme }) }));
vi.mock('../api/client.js', () => ({ api: { get: vi.fn().mockResolvedValue({ data:{ unreadCount:0 } }) } }));

function LocationText(){return <output data-testid="location">{useLocation().pathname}</output>}
function renderShell(){const client=new QueryClient({defaultOptions:{queries:{retry:false}}});return render(<QueryClientProvider client={client}><MemoryRouter initialEntries={['/']}><Routes><Route path="/" element={<AppShell/>}><Route index element={<LocationText/>}/><Route path="fleet" element={<LocationText/>}/><Route path="settings" element={<LocationText/>}/></Route></Routes></MemoryRouter></QueryClientProvider>)}

describe('AppShell',()=>{
  it('collapses and expands the desktop sidebar',()=>{
    renderShell();
    fireEvent.click(screen.getByRole('button',{name:'Collapse sidebar'}));
    expect(document.querySelector('.sidebar-collapsed')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button',{name:'Expand sidebar'}));
    expect(document.querySelector('.sidebar-collapsed')).not.toBeInTheDocument();
  });

  it('searches accessible pages and navigates to the selected result',()=>{
    renderShell();
    fireEvent.change(screen.getByRole('textbox',{name:'Search pages'}),{target:{value:'fleet'}});
    fireEvent.click(screen.getByRole('option',{name:'Fleet'}));
    expect(screen.getByTestId('location')).toHaveTextContent('/fleet');
  });

  it('opens the profile menu with account settings and theme actions',()=>{
    renderShell();
    fireEvent.click(screen.getByRole('button',{name:/Ops/}));
    expect(screen.getByRole('menuitem',{name:'Account settings'})).toBeInTheDocument();
    fireEvent.click(screen.getByRole('menuitem',{name:'Account settings'}));
    expect(screen.getByTestId('location')).toHaveTextContent('/settings');
    fireEvent.click(screen.getByRole('button',{name:'Switch to dark theme'}));
    expect(toggleTheme).toHaveBeenCalledOnce();
  });
});
