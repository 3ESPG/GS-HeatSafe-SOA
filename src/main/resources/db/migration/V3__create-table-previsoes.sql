create table previsoes(
    id bigint not null auto_increment,
    estacao_id bigint not null,
    data_base date not null,
    data_prevista date not null,
    indice_calor_previsto double not null,
    probabilidade_perigo double not null,
    perigosa tinyint not null,
    nivel_risco varchar(20) not null,
    metodo varchar(30) not null,
    amostras_historicas int not null,
    criada_em datetime not null,

    primary key(id),
    constraint fk_previsoes_estacao_id foreign key (estacao_id) references estacoes(id)
);

create index idx_previsoes_estacao_data on previsoes (estacao_id, data_prevista);
