alter type template_variable_type add value 'NUMBER' before 'STRING'
;

commit;

update template_variable
set type = 'NUMBER'
where domain = 'SENTENCE'
  and code in ('sentLenYears', 'sentLenMonths', 'sentLenDays')
;

update template_variable
set type = 'DATE'
where domain = 'SENTENCE'
  and code in ('sentArdCrd', 'sentCrd', 'sentPed', 'sentSled')
;