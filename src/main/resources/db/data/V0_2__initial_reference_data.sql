insert into template_group (code, name, description)
values ('EXTERNAL_MOVEMENT', 'External movement templates',
        'Document templates associated with external movements in general. These require a person to be selected'),
       ('TEMPORARY_ABSENCE', 'Temporary absence templates',
        'Document templates associated with temporary absences. These require a person and a temporary absence to be selected')
on conflict do nothing
;

insert into template_variable_domain (code, description, sequence_number)
values ('PRISON', 'Prison details', 110),
       ('PERSON', 'Prisoner details', 120),
       ('TEMPORARY_ABSENCE', 'Absence information', 130),
       ('OFFENDER_MANAGER', 'Manager details', 140)
on conflict do nothing
;

insert into template_variable (domain, code, description, sequence_number, type)
values ('PRISON', 'PRISON__CODE', 'Prison code', 110, 'STRING'),
       ('PRISON', 'PRISON__NAME', 'Prison name', 120, 'STRING'),
       ('PRISON', 'PRISON__ADDRESS', 'Prison address', 130, 'STRING'),
       ('PRISON', 'PRISON__PHONE', 'Prison phone number', 140, 'STRING'),
       ('PERSON', 'PERSON__NAME', 'Full name', 210, 'STRING'),
       ('PERSON', 'PERSON__IMAGE', 'Prisoner photo', 220, 'BINARY'),
       ('PERSON', 'PERSON__PRISON_NUMBER', 'Prison number', 230, 'STRING'),
       ('PERSON', 'PERSON__COURT_REFERENCE_NUMBER', 'CRO number', 240, 'STRING'),
       ('PERSON', 'PERSON__POLICE_NATIONAL_COMPUTER_NUMBER', 'PNC number', 250, 'STRING'),
       ('PERSON', 'PERSON__BOOKING_NUMBER', 'Booking number', 260, 'STRING'),
       ('PERSON', 'PERSON__DATE_OF_BIRTH', 'Date of birth', 270, 'DATE'),
       ('TEMPORARY_ABSENCE', 'TEMPORARY_ABSENCE__START_DATE', 'Start date', 310, 'DATE'),
       ('TEMPORARY_ABSENCE', 'TEMPORARY_ABSENCE__START_TIME', 'Start time', 311, 'TIME'),
       ('TEMPORARY_ABSENCE', 'TEMPORARY_ABSENCE__END_DATE', 'Expiry date', 320, 'DATE'),
       ('TEMPORARY_ABSENCE', 'TEMPORARY_ABSENCE__END_TIME', 'Expiry time', 321, 'TIME'),
       ('TEMPORARY_ABSENCE', 'TEMPORARY_ABSENCE__CATEGORISATION', 'Reason for absence', 330, 'STRING'),
       ('OFFENDER_MANAGER', 'OFFENDER_MANAGER__NAME', 'Offender manager name', 410, 'STRING'),
       ('OFFENDER_MANAGER', 'OFFENDER_MANAGER__ROLE', 'Offender manager role', 420, 'STRING'),
       ('OFFENDER_MANAGER', 'OFFENDER_MANAGER__PHONE', 'Offender manager phone number', 430, 'STRING'),
       ('OFFENDER_MANAGER', 'OFFENDER_MANAGER__INITIAL_APPOINTMENT_DATE',
        'Date of initial appointment with offender manager after release on licence', 440, 'DATE'),
       ('OFFENDER_MANAGER', 'OFFENDER_MANAGER__INITIAL_APPOINTMENT_TIME', 'Start time', 441, 'TIME'),
       ('OFFENDER_MANAGER', 'OFFENDER_MANAGER__INITIAL_APPOINTMENT_ADDRESS',
        'Address of probation office where initial appointment is scheduled', 450, 'STRING')
on conflict do nothing
;