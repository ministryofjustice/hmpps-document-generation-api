-- update of existing variables

update template_variable
set code = 'prsnCode'
where code = 'PRISON__CODE'
;

update template_variable
set code = 'prsnName'
where code = 'PRISON__NAME'
;

update template_variable
set code = 'prsnAddress'
where code = 'PRISON__ADDRESS'
;

update template_variable
set code = 'prsnPhone'
where code = 'PRISON__PHONE'
;

update template_variable
set code = 'perName'
where code = 'PERSON__NAME'
;

update template_variable
set code = 'perImage'
where code = 'PERSON__IMAGE'
;

update template_variable
set code = 'perPrsnNo'
where code = 'PERSON__PRISON_NUMBER'
;

update template_variable
set code = 'perCro'
where code = 'PERSON__COURT_REFERENCE_NUMBER'
;

update template_variable
set code = 'perPnc'
where code = 'PERSON__POLICE_NATIONAL_COMPUTER_NUMBER'
;

update template_variable
set code = 'perBookNo'
where code = 'PERSON__BOOKING_NUMBER'
;

update template_variable
set code = 'perDob'
where code = 'PERSON__DATE_OF_BIRTH'
;

update template_variable
set code = 'tapStartDate', sequence_number = 410
where code = 'TEMPORARY_ABSENCE__START_DATE'
;

update template_variable
set code = 'tapStartTime', sequence_number = 411
where code = 'TEMPORARY_ABSENCE__START_TIME'
;

update template_variable
set code = 'tapEndDate', sequence_number = 420
where code = 'TEMPORARY_ABSENCE__END_DATE'
;

update template_variable
set code = 'tapEndTime', sequence_number = 421
where code = 'TEMPORARY_ABSENCE__END_TIME'
;

update template_variable
set code = 'tapCat', sequence_number = 430
where code = 'TEMPORARY_ABSENCE__CATEGORISATION'
;

update template_variable
set code = 'omApptDate', sequence_number = 510
where code = 'OFFENDER_MANAGER__INITIAL_APPOINTMENT_DATE'
;

update template_variable
set code = 'omApptTime', sequence_number = 511
where code = 'OFFENDER_MANAGER__INITIAL_APPOINTMENT_TIME'
;

update template_variable
set code = 'omApptAddr', sequence_number = 520
where code = 'OFFENDER_MANAGER__INITIAL_APPOINTMENT_ADDRESS'
;

update template_variable_domain
set sequence_number = 150
where code = 'OFFENDER_MANAGER'
;

update template_variable_domain
set sequence_number = 140
where code = 'TEMPORARY_ABSENCE'
;

-- remove unused variables

delete
from document_template_variable
where id in (select id
             from template_variable
             where code in ('OFFENDER_MANAGER__NAME', 'OFFENDER_MANAGER__ROLE', 'OFFENDER_MANAGER__PHONE'))
;

delete
from template_variable
where code in ('OFFENDER_MANAGER__NAME', 'OFFENDER_MANAGER__ROLE', 'OFFENDER_MANAGER__PHONE')
;


-- insert new variables

insert into template_variable (domain, code, description, sequence_number, type)
values ('PERSON', 'perFirstName', 'First name', 211, 'STRING'),
       ('PERSON', 'perMiddleNames', 'Middle names', 212, 'STRING'),
       ('PERSON', 'perLastName', 'Last name', 213, 'STRING'),
       ('PERSON', 'perSecCat', 'Security category', 280, 'STRING')
on conflict do nothing
;

insert into template_variable_domain(code, description, sequence_number)
values ('SENTENCE', 'Sentence details', 130)
on conflict do nothing
;

insert into template_variable (domain, code, description, sequence_number, type)
values ('SENTENCE', 'sentMainOff', 'Main offence', 310, 'STRING'),
       ('SENTENCE', 'sentCurOff', 'Current offence', 320, 'STRING'),
       ('SENTENCE', 'sentEarliestCrtApp', 'Earliest court appearance', 330, 'STRING'),
       ('SENTENCE', 'sentLenYears', 'Sentence length - years', 340, 'STRING'),
       ('SENTENCE', 'sentLenMonths', 'Sentence length - months', 341, 'STRING'),
       ('SENTENCE', 'sentLenDays', 'Sentence length - days', 342, 'STRING'),
       ('SENTENCE', 'sentArdCrd', 'ARD/CRD', 350, 'STRING'),
       ('SENTENCE', 'sentCrd', 'CRD', 360, 'STRING'),
       ('SENTENCE', 'sentPed', 'PED or review date', 370, 'STRING'),
       ('SENTENCE', 'sentSled', 'SLED', 380, 'STRING')
on conflict do nothing
;

