package br.com.fiap3espg.heatsafe.domain.estacao;

import jakarta.persistence.*;
import lombok.*;

/** Estação meteorológica que fornece as observações climáticas (ex.: A701 - São Paulo/Mirante, do INMET). */
@Entity(name = "Estacao")
@Table(name = "estacoes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Estacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private String nome;
    private String cidade;
    private String uf;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private boolean ativa = true;

    public Estacao(DadosCadastroEstacao dados) {
        this.codigo = dados.codigo().toUpperCase();
        this.nome = dados.nome();
        this.cidade = dados.cidade();
        this.uf = dados.uf().toUpperCase();
        this.latitude = dados.latitude();
        this.longitude = dados.longitude();
        this.altitude = dados.altitude();
        this.ativa = true;
    }

    public void atualizarInformacoes(DadosAtualizacaoEstacao dados) {
        if (dados.nome() != null && !dados.nome().isBlank()) {
            this.nome = dados.nome();
        }
        if (dados.cidade() != null && !dados.cidade().isBlank()) {
            this.cidade = dados.cidade();
        }
        if (dados.uf() != null && !dados.uf().isBlank()) {
            this.uf = dados.uf().toUpperCase();
        }
        if (dados.latitude() != null) {
            this.latitude = dados.latitude();
        }
        if (dados.longitude() != null) {
            this.longitude = dados.longitude();
        }
        if (dados.altitude() != null) {
            this.altitude = dados.altitude();
        }
    }

    public void desativar() {
        this.ativa = false;
    }
}
