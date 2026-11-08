import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '../auth/AuthProvider.tsx';
import { LoginPage } from './LoginPage.jsx';

vi.mock('../api/client.js', async (importOriginal) => ({ ...(await importOriginal()), authApi: { login: vi.fn().mockResolvedValue({ accessToken:'test', refreshToken:'refresh' }), me: vi.fn().mockResolvedValue({ email:'admin@example.test', roles:['ADMIN'] }) } }));

describe('LoginPage', () => {
  it('validates email before calling the authentication API', async () => {
    render(<MemoryRouter><AuthProvider><LoginPage /></AuthProvider></MemoryRouter>);
    fireEvent.change(screen.getByLabelText('Work email'), { target: { value:'invalid' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value:'secret' } });
    fireEvent.click(screen.getByRole('button', { name:/sign in/i }));
    await waitFor(() => expect(screen.getByText('Enter a valid email address')).toBeInTheDocument());
  });
});
