import { BackgroundBlobs } from '../components/layout/BackgroundBlobs';
import styles from './App.module.css';

/**
 * Tela provisória da etapa 1: mostra as receitas de vidro e as fontes para
 * conferir o design system. É substituída pelo router na etapa 3.
 */
export function App() {
  return (
    <>
      <BackgroundBlobs variant="login" />
      <main className={styles.page}>
        <header className={styles.header}>
          <p className={styles.logo}>vicino</p>
          <p className={styles.tagline}>Seu condomínio, mais perto de você.</p>
        </header>

        <section className={`glass-strong ${styles.card}`} aria-labelledby="titulo-setup">
          <h1 id="titulo-setup" className={styles.title}>
            Setup pronto
          </h1>
          <p className={styles.text}>
            Tokens, fontes e receitas de vidro carregados. As telas chegam nas próximas etapas.
          </p>
          <label className={styles.label} htmlFor="exemplo">
            Campo de exemplo
          </label>
          <div className={`glass-input ${styles.field}`}>
            <input id="exemplo" className={styles.input} placeholder="voce@email.com" />
          </div>
        </section>

        <div className={styles.samples}>
          <div className={`glass ${styles.sample}`}>glass</div>
          <div className={`glass-dark ${styles.sample}`}>glass-dark</div>
          <div className={`glass-nav ${styles.sample}`}>glass-nav</div>
        </div>
      </main>
    </>
  );
}
