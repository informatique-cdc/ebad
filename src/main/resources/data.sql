
INSERT INTO  t_authority (name) VALUES ('ROLE_USER');
INSERT INTO  t_authority (name) VALUES ('ROLE_ADMIN');

INSERT INTO t_user (id, created_by, created_date,  activated, email, first_name, lang_key, last_name, login, password)
VALUES (nextval('hibernate_sequence') , 'SYSTEM', now(), true, 'admin@localhost', 'admin', 'FR_fr', 'admin', 'admin', '$2a$10$QyCZA5xGUzdAkTR.jWA8seE5asafxWqC.FXa7JuQK3BS4dFyoEvM.');

INSERT INTO t_user_authority (authority_name, user_id)
SELECT 'ROLE_ADMIN', id from t_user where login = 'admin';


