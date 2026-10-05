package br.com.vicino.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.vicino.api.enums.PerfilEnum;
import br.com.vicino.api.model.Usuario;
import br.com.vicino.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Component 
@RequiredArgsConstructor 
public class AdminInicial implements ApplicationRunner {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${vicino.admin.email:}")
    private String email;

    @Value("${vicino.admin.senha}")
    private String senha;

    @Override 
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByPerfil(PerfilEnum.ADMINISTRADOR)) {
            return;
        }

        if (email.isBlank() || senha.length() < 8) {
            log.warn("Nenhum ADMINISTRADOR cadastrado. Defina ADMIN_EMAIL e ADMIN_SENHA (mínimo 8 caracteres) para criar o administrador inicial.");
            return;
        }

        String emailNormalizado = email.trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            log.warn("ADMIN_EMAIL {} já pertence a outro usuario, administrador inicial não foi criado.", emailNormalizado);
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome("Administrador");
        admin.setEmail(emailNormalizado);
        admin.setSenhaHash(passwordEncoder.encode(senha));
        admin.setPerfil(PerfilEnum.ADMINISTRADOR);
        usuarioRepository.save(admin);

        log.info("Administrador inicial criado: {}", emailNormalizado);
    }
}
