import { render } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { BackgroundBlobs } from './BackgroundBlobs';

describe('BackgroundBlobs', () => {
  it('fica escondido dos leitores de tela', () => {
    const { container } = render(<BackgroundBlobs />);
    expect(container.firstChild).toHaveAttribute('aria-hidden', 'true');
  });

  it('desenha 4 manchas no login e 3 no app', () => {
    const login = render(<BackgroundBlobs variant="login" />);
    expect(login.container.firstChild?.childNodes).toHaveLength(4);

    const app = render(<BackgroundBlobs variant="app" />);
    expect(app.container.firstChild?.childNodes).toHaveLength(3);
  });
});
