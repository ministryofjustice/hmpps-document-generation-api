alter table document_template
    add column if not exists instruction_text text
;

alter table document_template_audit
    add column if not exists instruction_text text
;