create table if not exists document_generation_request
(
    id          uuid  not null,
    template_id uuid  not null,
    request     jsonb not null,
    constraint pk_document_generation_request primary key (id),
    constraint fk_document_generation_request_document_template foreign key (template_id) references document_template (id)
)
;

create table if not exists document_generation_request_audit
(
    rev_id      bigint   not null references audit_revision (id),
    rev_type    smallint not null,
    id          uuid     not null,
    template_id uuid     not null,
    request     jsonb    not null,
    constraint pk_document_generation_request_audit primary key (id, rev_id)
)
;