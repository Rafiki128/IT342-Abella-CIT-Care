import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import BookingModal from '../BookingModal';

const defaultProps = {
  isOpen: true,
  onClose: vi.fn(),
  services: [{ id: 1, name: 'Medical Clinic' }],
  formData: {
    serviceId: '',
    appointmentDate: '',
    appointmentTime: '',
    reason: '',
    office: '',
  },
  handleInputChange: vi.fn(),
  handleBooking: vi.fn((event) => event.preventDefault()),
};

describe('BookingModal', () => {
  it('renders available services and submits booking form', () => {
    const { container } = render(<BookingModal {...defaultProps} />);

    expect(screen.getByRole('heading', { name: /schedule appointment/i })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: /medical clinic/i })).toBeInTheDocument();

    fireEvent.submit(container.querySelector('form'));

    expect(defaultProps.handleBooking).toHaveBeenCalled();
  });

  it('does not render when closed', () => {
    const { container } = render(<BookingModal {...defaultProps} isOpen={false} />);

    expect(container).toBeEmptyDOMElement();
  });
});
