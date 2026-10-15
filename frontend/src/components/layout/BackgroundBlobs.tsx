import type { CSSProperties } from 'react';
import styles from './BackgroundBlobs.module.css';

type Variant = 'login' | 'app';

interface Blob {
  color: string;
  size: number;
  opacity: number;
  blur: number;
  position: Pick<CSSProperties, 'top' | 'right' | 'bottom' | 'left'>;
}

// Posições, tamanhos e opacidades copiados do protótipo (Docs/prototipo).
const BLOBS: Record<Variant, Blob[]> = {
  login: [
    {
      color: 'var(--color-terracotta)',
      size: 340,
      opacity: 0.55,
      blur: 70,
      position: { top: -110, right: -120 },
    },
    {
      color: 'var(--color-olive-light)',
      size: 300,
      opacity: 0.5,
      blur: 70,
      position: { top: 300, left: -170 },
    },
    {
      color: 'var(--color-peach)',
      size: 280,
      opacity: 0.6,
      blur: 70,
      position: { bottom: -90, right: -80 },
    },
    {
      color: 'var(--color-olive)',
      size: 170,
      opacity: 0.18,
      blur: 60,
      position: { bottom: 120, left: 30 },
    },
  ],
  app: [
    {
      color: 'var(--color-olive-light)',
      size: 320,
      opacity: 0.45,
      blur: 70,
      position: { top: -120, left: -110 },
    },
    {
      color: 'var(--color-terracotta)',
      size: 300,
      opacity: 0.45,
      blur: 70,
      position: { top: 220, right: -150 },
    },
    {
      color: 'var(--color-peach)',
      size: 300,
      opacity: 0.55,
      blur: 70,
      position: { bottom: -80, left: -60 },
    },
  ],
};

interface BackgroundBlobsProps {
  variant?: Variant;
}

/** Manchas de cor desfocadas atrás de todo o conteúdo. Puramente decorativas. */
export function BackgroundBlobs({ variant = 'app' }: BackgroundBlobsProps) {
  return (
    <div className={styles.root} aria-hidden="true">
      {BLOBS[variant].map((blob, index) => (
        <span
          key={index}
          className={styles.blob}
          style={{
            ...blob.position,
            width: blob.size,
            height: blob.size,
            background: blob.color,
            opacity: blob.opacity,
            filter: `blur(${blob.blur}px)`,
            // Cada mancha começa num ponto diferente da animação, para não flutuarem juntas.
            animationDelay: `${index * -7}s`,
          }}
        />
      ))}
    </div>
  );
}
