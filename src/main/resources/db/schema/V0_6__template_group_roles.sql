alter table template_group
    add column if not exists roles text[] not null default '{}'
;

update template_group
set roles = '{"EXTERNAL_MOVEMENTS_TAP_RO", "EXTERNAL_MOVEMENTS_TAP_RW"}'
where code in ('EXTERNAL_MOVEMENT', 'TEMPORARY_ABSENCE')
;

alter table template_group
    alter column roles drop default
;