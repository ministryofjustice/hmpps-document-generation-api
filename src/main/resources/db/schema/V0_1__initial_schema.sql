create table if not exists template_group
(
    id          uuid not null default uuidv7(),
    code        text not null,
    name        text not null,
    description text not null,
    constraint pk_template_group primary key (id),
    constraint uq_template_group_code unique (code)
)
;

create table if not exists template_variable_domain
(
    code            text not null,
    description     text not null,
    sequence_number int  not null,
    constraint pk_template_variable_domain primary key (code),
    constraint uq_template_variable_domain_sequence unique (sequence_number)
)
;

do
$$
    begin
        if not exists (select 1 from pg_type where typname = 'template_variable_type') then
            create type template_variable_type as enum ('BINARY', 'DATE', 'STRING', 'TIME');
        end if;
    end;
$$;

create table if not exists template_variable
(
    id              uuid                   not null default uuidv7(),
    domain          text                   not null,
    code            text                   not null,
    description     text                   not null,
    sequence_number int                    not null,
    type            template_variable_type not null,
    constraint pk_template_variable primary key (id),
    constraint uq_template_variable_code unique (code),
    constraint uq_template_variable_sequence unique (domain, sequence_number),
    constraint fk_template_variable_domain foreign key (domain) references template_variable_domain (code)
)
;

create table if not exists document_template
(
    id                 uuid not null default uuidv7(),
    code               text not null,
    name               text not null,
    description        text not null,
    version            int  not null,
    external_reference uuid not null,
    constraint pk_document_template primary key (id),
    constraint uq_document_template_code unique (code),
    constraint uq_document_template_external_reference unique (external_reference)
)
;

create table if not exists document_template_group
(
    id                uuid not null default uuidv7(),
    template_id       uuid not null,
    template_group_id uuid not null,
    constraint pk_document_template_group primary key (id),
    constraint fk_document_template_group_document_template foreign key (template_id) references document_template (id),
    constraint fk_document_template_group_template_group foreign key (template_group_id) references template_group (id),
    constraint uq_template_group unique (template_id, template_group_id)
)
;

create table if not exists document_template_variable
(
    id          uuid    not null default uuidv7(),
    template_id uuid    not null,
    variable_id uuid    not null,
    mandatory   boolean not null,
    constraint pk_document_template_variable primary key (id),
    constraint fk_document_template_variable_document_template foreign key (template_id) references document_template (id),
    constraint fk_document_template_variable_variable_id foreign key (variable_id) references template_variable (id),
    constraint uq_document_template_variable unique (template_id, variable_id)
)
;

create table if not exists audit_revision
(
    id        bigserial   not null,
    timestamp timestamp   not null,
    username  varchar(64) not null,
    constraint pk_audit_revision primary key (id)
)
;

create table if not exists document_template_audit
(
    rev_id             bigint   not null references audit_revision (id),
    rev_type           smallint not null,
    id                 uuid     not null,
    code               text     not null,
    name               text     not null,
    description        text     not null,
    version            int      not null,
    external_reference uuid     not null,
    constraint pk_document_template_audit primary key (id, rev_id),
    constraint uq_document_template_audit_external_reference unique (external_reference, rev_id)
)
;

create table if not exists document_template_variable_audit
(
    rev_id      bigint   not null references audit_revision (id),
    rev_type    smallint not null,
    id          uuid     not null,
    template_id uuid     not null,
    variable_id uuid     not null,
    mandatory   boolean  not null,
    constraint pk_document_template_variable_audit primary key (id, rev_id),
    constraint uq_document_template_variable_audit unique (template_id, variable_id, rev_id)
)
;

create table if not exists document_template_group_audit
(
    rev_id            bigint   not null references audit_revision (id),
    rev_type          smallint not null,
    id                uuid     not null default uuidv7(),
    template_id       uuid     not null,
    template_group_id uuid     not null,
    constraint pk_document_template_group_audit primary key (id, rev_id),
    constraint uq_template_group_audit unique (template_id, template_group_id, rev_id)
)
;