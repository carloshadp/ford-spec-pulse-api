package com.ford.specpulse.autenticacao.api.dto;

import com.ford.specpulse.seguranca.Perfil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Schema(description = "Requisicao para registrar um novo usuario na API.")
public record RegistroRequisicao(

        @Schema(description = "Nome completo do usuario.", example = "Ana Estrategista")
        @NotBlank(message = "nome e obrigatorio")
        @Size(min = 2, max = 120, message = "nome deve ter entre 2 e 120 caracteres")
        @Pattern(regexp = "^[A-Za-zÀ-ÿ '\\-]+$",
                message = "nome aceita apenas letras, espacos, apostrofo e hifen")
        String nome,

        @Schema(description = "Email institucional unico.", example = "ana@ford.internal")
        @NotBlank(message = "email e obrigatorio")
        @Email(message = "email com formato invalido")
        @Size(max = 160, message = "email deve ter no maximo 160 caracteres")
        String email,

        @Schema(description = "Senha em texto puro. Sera convertida em BCrypt antes de persistir.",
                example = "Senha@Forte123")
        @NotBlank(message = "senha e obrigatoria")
        @Size(min = 8, max = 80, message = "senha deve ter entre 8 e 80 caracteres")
        String senha,

        @Schema(description = "Perfil opcional. Quando ausente, recebe SOMENTE_LEITURA. "
                + "Apenas ADMINISTRADOR pode registrar com perfil diferente do padrao "
                + "(restricao a aplicar no controller).",
                example = "ANALISTA")
        Perfil perfil
) {
}
