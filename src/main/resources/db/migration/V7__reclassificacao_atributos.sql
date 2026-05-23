-- Reclassifica atributos para categorias mais precisas introduzidas na versao 7.
-- PAINEL_DIGITAL_POLEGADAS pertence a PAINEL_DIGITAL (digital_cockpit), nao CONECTIVIDADE.
UPDATE atributos_definicao
SET categoria = 'PAINEL_DIGITAL'
WHERE id = '55555555-5555-5555-5555-000000000021';
