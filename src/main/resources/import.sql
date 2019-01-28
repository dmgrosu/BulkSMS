insert into tb_smsc_account values (1, 1, 0, '192.168.1.91', 1, 'unifun', 34655, 'unifun', 100);
insert into tb_smsc_account values (2, 1, 0, '127.0.0.1', 1, 'un_i3fun', 33555, 'unifun', 200);
insert into tb_account values (1, 0,'emotion', 200, 1);
insert into tb_user values (1,'dmitri.grosu@gmail.com','Dmitri','Grosu','$2a$10$l2jG0pR9uwYEyTkycQmaWefKL2vxgmJU/IDobyZrNF17hCQjNQ1uS','ACTIVE','sysadmin',1);
insert into tb_role values (1,'ADMIN');
insert into tb_role values (2,'USER');
insert into tb_role values (3,'MODERATOR');
insert into tb_user_role values (1,1);
insert into tb_user_role values (1,3);
insert into tb_sms_type values (1, 0, 'simple');
insert into tb_sms_type values (2, 0, 'hidden');
insert into tb_sms_type values (3, 0, 'flash');
insert into tb_sms_priority values (1, 0, 'normal');
insert into tb_sms_priority values (2, 0, 'low');
insert into tb_sms_priority values (3, 0, 'high');
insert into tb_expiration_time values (1, 0, '10 minutes', '000000001000000R', 1);
insert into tb_expiration_time values (2, 0, '30 minutes', '000000003000000R', 1);
insert into tb_expiration_time values (3, 0, '1 hour', '000000010000000R', 1);
insert into tb_expiration_time values (4, 0, '3 hours', '000000030000000R', 1);
insert into tb_sms_prefix_group values (1, 0, 'UMS', 1);
insert into tb_sms_prefix values (1, 0, '998', 1);
insert into tb_smpp_address values (1, 'PREZIDENTUZ', 0, 'UNKNOWN', 5, 1);

