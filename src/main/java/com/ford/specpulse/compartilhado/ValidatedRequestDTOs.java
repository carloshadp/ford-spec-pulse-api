package com.ford.specpulse.compartilhado;

import jakarta.validation.constraints.*;

import java.util.List;

/**
 * DTOs de entrada com validacao completa.
 *
 * Substitui os DTOs existentes de FichaTecnica e Comparacao.
 * Adiciona: @Size, @Pattern, @NotEmpty para prevenir
 * payload flooding, injeção e entradas malformadas.
 */
public class ValidatedRequestDTOs {

    /**
     * Request para consulta de ficha tecnica.
     * Substitui o DTO existente em ficha/FichaTecnicaRequest (ou similar).
     */
    public record ConsultarFichaTecnicaRequest(

            @NotBlank(message = "Marca e obrigatoria.")
            @Size(min = 1, max = 80, message = "Marca deve ter entre 1 e 80 caracteres.")
            @Pattern(regexp = "^[\\p{L}\\d\\s\\-\\.]+$",
                    message = "Marca contem caracteres invalidos.")
            String marca,

            @NotBlank(message = "Modelo e obrigatorio.")
            @Size(min = 1, max = 100, message = "Modelo deve ter entre 1 e 100 caracteres.")
            @Pattern(regexp = "^[\\p{L}\\d\\s\\-\\.]+$",
                    message = "Modelo contem caracteres invalidos.")
            String modelo,

            @NotBlank(message = "Versao e obrigatoria.")
            @Size(min = 1, max = 200, message = "Versao deve ter entre 1 e 200 caracteres.")
            String versao,

            @NotEmpty(message = "Informe pelo menos 1 atributo.")
            @Size(max = 20, message = "Maximo de 20 atributos por consulta.")
            List<
                    @NotBlank
                    @Size(max = 60, message = "Nome de atributo deve ter no maximo 60 caracteres.")
                            String
                    > atributos

    ) {}

    /**
     * Request para criacao de comparacao.
     * Substitui o DTO existente de ComparacaoRequest.
     */
    public record CriarComparacaoRequest(

            @NotBlank(message = "ID da versao de referencia e obrigatorio.")
            @Size(max = 200, message = "ID da versao excede o tamanho maximo.")
            String referenceVersionId,

            @NotEmpty(message = "Informe pelo menos 1 concorrente.")
            @Size(max = 5, message = "Maximo de 5 concorrentes por comparacao.")
            List<
                    @NotBlank
                    @Size(max = 200)
                            String
                    > competitorVersionIds,

            @Size(max = 50, message = "Perfil de cliente deve ter no maximo 50 caracteres.")
            String customerProfileId,

            @NotBlank(message = "Titulo e obrigatorio.")
            @Size(min = 3, max = 200, message = "Titulo deve ter entre 3 e 200 caracteres.")
            String title

    ) {}

    /**
     * Request de autenticacao — limita tamanho para prevenir flooding.
     * Substitui o DTO existente de LoginRequest.
     */
    public record LoginRequest(

            @NotBlank(message = "Email e obrigatorio.")
            @Email(message = "Formato de email invalido.")
            @Size(max = 255, message = "Email excede o tamanho maximo.")
            String email,

            @NotBlank(message = "Senha e obrigatoria.")
            @Size(min = 6, max = 128, message = "Senha deve ter entre 6 e 128 caracteres.")
            String senha

    ) {}

    /**
     * Request de registro — com restricoes de formato.
     * Substitui o DTO existente de RegistroRequest.
     */
    public record RegistroRequest(

            @NotBlank(message = "Email e obrigatorio.")
            @Email(message = "Formato de email invalido.")
            @Size(max = 255, message = "Email excede o tamanho maximo.")
            String email,

            @NotBlank(message = "Senha e obrigatoria.")
            @Size(min = 8, max = 128, message = "Senha deve ter entre 8 e 128 caracteres.")
            @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&\\-_]).{8,}$",
                    message = "Senha deve conter maiuscula, minuscula, numero e caractere especial."
            )
            String senha,

            @Size(max = 30, message = "Perfil excede o tamanho maximo.")
            String perfil

    ) {}

    /**
     * Request de busca — limita tamanho do termo de pesquisa.
     */
    public record BuscaRequest(

            @NotBlank(message = "Termo de busca e obrigatorio.")
            @Size(min = 2, max = 100, message = "Termo deve ter entre 2 e 100 caracteres.")
            @Pattern(regexp = "^[\\p{L}\\d\\s\\-\\.\\,]+$",
                    message = "Termo de busca contem caracteres invalidos.")
            String q

    ) {}
}