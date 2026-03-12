insert into template_variable(domain, code, description, sequence_number, type)
values ('SENTENCE', 'sentDate', 'Date of sentence', 335, 'DATE')
;

update template_variable
set type = 'DATE'
where code = 'sentEarliestCrtApp'
;