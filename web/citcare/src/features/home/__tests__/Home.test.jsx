import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import Home from '../Home';

describe('Home', () => {
  it('renders guest landing content when no user is stored', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      json: async () => [],
    });

    render(<Home />, { wrapper: MemoryRouter });

    expect(screen.getByRole('heading', { name: /compassionate care/i })).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: /book now|book appointment now/i }).length).toBeGreaterThan(0);
  });

  it('loads dashboard appointments for an authenticated user', async () => {
    localStorage.setItem('user', JSON.stringify({ id: 1, fullName: 'Student User' }));
    globalThis.fetch = vi.fn((url) => {
      if (url.includes('/api/appointments/student/1')) {
        return Promise.resolve({
          ok: true,
          json: async () => [
            {
              id: 10,
              service: { name: 'Medical Clinic' },
              appointmentDate: '2026-05-15',
              appointmentTime: '09:30:00',
              office: 'Clinic Room 1',
              status: 'PENDING',
            },
          ],
        });
      }

      return Promise.resolve({ json: async () => [] });
    });

    render(<Home />, { wrapper: MemoryRouter });

    expect(screen.getByText(/welcome back/i)).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText(/medical clinic/i)).toBeInTheDocument());
    expect(screen.getByText(/pending/i)).toBeInTheDocument();
  });
});
