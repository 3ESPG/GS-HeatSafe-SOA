create table observacoes_climaticas(
    id bigint not null auto_increment,
    estacao_id bigint not null,
    data date not null,
    temperatura_maxima double not null,
    temperatura_minima double not null,
    temperatura_media double,
    umidade_media double,
    umidade_minima double not null,
    precipitacao double,
    indice_calor_maximo double not null,
    nivel_risco varchar(20) not null,
    origem varchar(20) not null,

    primary key(id),
    constraint uk_observacoes_estacao_data unique (estacao_id, data),
    constraint fk_observacoes_estacao_id foreign key (estacao_id) references estacoes(id)
);
