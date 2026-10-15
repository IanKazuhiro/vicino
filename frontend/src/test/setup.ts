import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach } from 'vitest';

// Sem as globais do Vitest, a Testing Library não desmonta os componentes sozinha.
afterEach(() => {
  cleanup();
});
