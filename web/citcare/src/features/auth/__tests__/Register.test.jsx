import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import Register from '../Register';

describe('Register', () => {
  it('submits registration data and resets the form after success', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ success: true }),
    });

    render(<Register />, { wrapper: MemoryRouter });

    fireEvent.change(screen.getByLabelText(/full name/i), { target: { value: 'Student User' } });
    fireEvent.change(screen.getByLabelText(/email address/i), { target: { value: 'student@cit.edu' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'secret' } });
    fireEvent.click(screen.getByRole('button', { name: /register/i }));

    expect(await screen.findByText(/registration successful/i)).toBeInTheDocument();
    expect(globalThis.fetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/v1/auth/register',
      expect.objectContaining({ method: 'POST' }),
    );
    expect(screen.getByLabelText(/full name/i)).toHaveValue('');
  });
});
