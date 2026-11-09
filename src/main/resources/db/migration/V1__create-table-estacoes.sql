create table estacoes(
    id bigint not null auto_increment,
    codigo varchar(10) not null unique,
    nome varchar(100) not null,
    cidade varchar(100) not null,
    uf varchar(2) not null,
    latitude double not null,
    longitude double not null,
    altitude double,
    ativa tinyint not null default 1,

    primary key(id)
);
