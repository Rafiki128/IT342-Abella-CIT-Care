import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import Login from '../Login';

describe('Login', () => {
  it('stores authenticated user data after successful login', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        success: true,
        data: {
          accessToken: 'token',
          user: { id: 1, fullName: 'Student User', email: 'student@cit.edu', role: 'STUDENT' },
        },
      }),
    });

    render(<Login />, { wrapper: MemoryRouter });

    fireEvent.change(screen.getByLabelText(/email address/i), { target: { value: 'student@cit.edu' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'secret' } });
    fireEvent.click(screen.getByRole('button', { name: /^sign in$/i }));

    await waitFor(() => expect(localStorage.getItem('accessToken')).toBe('token'));
    expect(JSON.parse(localStorage.getItem('user')).email).toBe('student@cit.edu');
    expect(screen.getByText(/welcome back, student user/i)).toBeInTheDocument();
  });

  it('shows an error message when credentials are rejected', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      json: async () => ({ success: false, error: { message: 'Invalid credentials' } }),
    });

    render(<Login />, { wrapper: MemoryRouter });

    fireEvent.change(screen.getByLabelText(/email address/i), { target: { value: 'student@cit.edu' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'wrong' } });
    fireEvent.click(screen.getByRole('button', { name: /^sign in$/i }));

    expect(await screen.findByText(/invalid credentials/i)).toBeInTheDocument();
  });
});
