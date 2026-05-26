import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import Home from '../Home';

describe('Home', () => {
  it('renders guest landing content when no user is stored', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      json: async () => [],
    });

    render(<Home />, { wrapper: MemoryRouter });

    expect(screen.getByRole('heading', { name: /compassionate care/i })).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: /book appointment|book medical visit|book guidance session/i }).length).toBeGreaterThan(0);
  });

  it('redirects authenticated users to the universal dashboard', async () => {
    localStorage.setItem('user', JSON.stringify({ id: 1, fullName: 'Student User' }));
    globalThis.fetch = vi.fn().mockResolvedValue({ json: async () => [] });

    render(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/dashboard" element={<div>Universal Dashboard</div>} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => expect(screen.getByText(/universal dashboard/i)).toBeInTheDocument());
  });
});
