-- =====================================================================
-- Geladeira Solidaria Inteligente - V2: dados de teste
-- Datas relativas a CURRENT_DATE: o cenario e o mesmo em qualquer dia.
-- Nomes, telefones e e-mails sao ficticios.
-- =====================================================================

-- ---------------------------------------------------------- GELADEIRAS
INSERT INTO geladeira (nome, endereco, bairro, cidade, latitude, longitude, capacidade_kg, responsavel, status, dt_instalacao) VALUES
('Geladeira Solidaria Paraisopolis',      'Rua Ernest Renan, 1200',          'Paraisopolis',      'Sao Paulo', -23.614500, -46.723000,  80, 'Associacao de Moradores de Paraisopolis', 'ATIVA',      CURRENT_DATE - INTERVAL '400' DAY),
('Geladeira Solidaria Capao Redondo',     'Estrada de Itapecerica, 3400',    'Capao Redondo',     'Sao Paulo', -23.672000, -46.779000,  60, 'Coletivo Capao Solidario',                'ATIVA',      CURRENT_DATE - INTERVAL '350' DAY),
('Geladeira Solidaria Heliopolis',        'Estrada das Lagrimas, 2100',      'Heliopolis',        'Sao Paulo', -23.608000, -46.596000,  70, 'UNAS Heliopolis',                         'ATIVA',      CURRENT_DATE - INTERVAL '300' DAY),
('Geladeira Solidaria Brasilandia',       'Av. Michihisa Murata, 500',       'Brasilandia',       'Sao Paulo', -23.470000, -46.690000,  60, 'Igreja Comunitaria Brasilandia',          'ATIVA',      CURRENT_DATE - INTERVAL '280' DAY),
('Geladeira Solidaria Cidade Tiradentes', 'Av. dos Metalurgicos, 1800',      'Cidade Tiradentes', 'Sao Paulo', -23.582000, -46.409000,  70, 'Centro Cultural Tiradentes',              'ATIVA',      CURRENT_DATE - INTERVAL '250' DAY),
('Geladeira Solidaria Grajau',            'Av. Dona Belmira Marin, 2600',    'Grajau',            'Sao Paulo', -23.760000, -46.697000,  60, 'Coletivo Grajau em Rede',                 'MANUTENCAO', CURRENT_DATE - INTERVAL '220' DAY),
('Geladeira Solidaria Itaquera',          'Av. Jose Pinheiro Borges, 900',   'Itaquera',          'Sao Paulo', -23.536000, -46.456000,  80, 'Associacao Itaquera Viva',                'ATIVA',      CURRENT_DATE - INTERVAL '200' DAY),
('Geladeira Solidaria Jardim Angela',     'Estrada do M''Boi Mirim, 5000',   'Jardim Angela',     'Sao Paulo', -23.712000, -46.770000,  60, 'Sociedade Santos Martires',               'ATIVA',      CURRENT_DATE - INTERVAL '180' DAY),
('Geladeira Solidaria Se',                'Praca da Se, 100',                'Se',                'Sao Paulo', -23.550000, -46.634000, 100, 'Rede Centro Acolhe',                      'ATIVA',      CURRENT_DATE - INTERVAL '150' DAY),
('Geladeira Solidaria Perus',             'Rua Joao Batista de Oliveira, 80','Perus',             'Sao Paulo', -23.405000, -46.753000,  50, 'Associacao Perus Unido',                  'INATIVA',    CURRENT_DATE - INTERVAL '500' DAY);

-- ---------------------------------------------------------- DOADORES
INSERT INTO doador (nome, tipo, telefone, email, dt_cadastro) VALUES
('Padaria Pao Nosso',            'PJ', '(11) 3000-1001', 'contato@paonosso.example.com',      CURRENT_DATE - INTERVAL '300' DAY),
('Mercado Bom Preco',            'PJ', '(11) 3000-1002', 'doacoes@bompreco.example.com',      CURRENT_DATE - INTERVAL '280' DAY),
('Laticinios Serra Azul',        'PJ', '(11) 3000-1003', 'social@serraazul.example.com',      CURRENT_DATE - INTERVAL '260' DAY),
('Maria Aparecida Santos',       'PF', '(11) 90000-1004', 'maria.santos@example.com',         CURRENT_DATE - INTERVAL '240' DAY),
('Atacado Popular Zona Sul',     'PJ', '(11) 3000-1005', 'rh@atacadopopular.example.com',     CURRENT_DATE - INTERVAL '220' DAY),
('Distribuidora Boa Mesa',       'PJ', '(11) 3000-1006', 'contato@boamesa.example.com',       CURRENT_DATE - INTERVAL '200' DAY),
('Joao Pereira Lima',            'PF', '(11) 90000-1007', 'joao.lima@example.com',            CURRENT_DATE - INTERVAL '150' DAY),
('Restaurante Sabor Caseiro',    'PJ', '(11) 3000-1008', 'cozinha@saborcaseiro.example.com',  CURRENT_DATE - INTERVAL '120' DAY),
('Hortifruti Vila Verde',        'PJ', '(11) 3000-1009', 'vendas@vilaverde.example.com',      CURRENT_DATE - INTERVAL '90' DAY),
('Ana Beatriz Costa',            'PF', '(11) 90000-1010', 'ana.costa@example.com',            CURRENT_DATE - INTERVAL '60' DAY);

-- ---------------------------------------------------------- CATEGORIAS
-- temp_max_c = temperatura maxima segura para alimentos refrigerados
INSERT INTO categoria (nome, refrigerado, temp_max_c) VALUES
('Laticinios',           TRUE,  7.0),
('Carnes e frios',       TRUE,  4.0),
('Legumes e verduras',   TRUE, 10.0),
('Paes e padaria',       FALSE, NULL),
('Graos e cereais',      FALSE, NULL),
('Refeicoes prontas',    TRUE,  5.0),
('Enlatados',            FALSE, NULL),
('Bebidas',              FALSE, NULL),
('Frutas',               FALSE, NULL),
('Ovos',                 TRUE, 10.0);

-- ---------------------------------------------------------- LEITURAS DO SENSOR
INSERT INTO leitura_sensor (id_geladeira, dt_leitura, temperatura_c, umidade_pct, porta_aberta) VALUES
(1, CURRENT_TIMESTAMP - INTERVAL '60' MINUTE, 4.2, 55.0, FALSE),
(1, CURRENT_TIMESTAMP - INTERVAL '10' MINUTE, 5.1, 58.0, TRUE),
(2, CURRENT_TIMESTAMP - INTERVAL '60' MINUTE, 3.8, 52.0, FALSE),
(2, CURRENT_TIMESTAMP - INTERVAL '10' MINUTE, 4.0, 53.0, FALSE),
(3, CURRENT_TIMESTAMP - INTERVAL '60' MINUTE, 9.0, 61.0, FALSE),
(3, CURRENT_TIMESTAMP - INTERVAL '10' MINUTE, 9.8, 63.0, FALSE),
(4, CURRENT_TIMESTAMP - INTERVAL '60' MINUTE, 4.5, 50.0, FALSE),
(4, CURRENT_TIMESTAMP - INTERVAL '10' MINUTE, 4.3, 51.0, FALSE),
(5, CURRENT_TIMESTAMP - INTERVAL '60' MINUTE, 3.9, 49.0, FALSE),
(5, CURRENT_TIMESTAMP - INTERVAL '10' MINUTE, 4.1, 50.0, FALSE),
(6, CURRENT_TIMESTAMP - INTERVAL '3' DAY,    12.5, 70.0, FALSE),
(7, CURRENT_TIMESTAMP - INTERVAL '60' MINUTE, 4.8, 54.0, FALSE),
(7, CURRENT_TIMESTAMP - INTERVAL '10' MINUTE, 4.6, 54.0, FALSE),
(8, CURRENT_TIMESTAMP - INTERVAL '60' MINUTE, 5.0, 57.0, FALSE),
(8, CURRENT_TIMESTAMP - INTERVAL '10' MINUTE, 5.2, 57.0, FALSE),
(9, CURRENT_TIMESTAMP - INTERVAL '60' MINUTE, 3.5, 48.0, FALSE),
(9, CURRENT_TIMESTAMP - INTERVAL '10' MINUTE, 3.7, 48.0, FALSE);

-- ---------------------------------------------------------- DOACOES
-- (geladeira, doador, categoria, descricao, kg, data da doacao, validade, status, risco)
INSERT INTO doacao (id_geladeira, id_doador, id_categoria, descricao, quantidade_kg, dt_doacao, dt_validade, status, nivel_risco) VALUES
-- Paraisopolis: 30 de 80 kg
(1, 1, 4, 'Paes franceses',               6, CURRENT_TIMESTAMP - INTERVAL '5' HOUR,  CURRENT_DATE + INTERVAL '1' DAY,   'DISPONIVEL', 'OK'),
(1, 2, 5, 'Arroz (pacotes de 5 kg)',     15, CURRENT_TIMESTAMP - INTERVAL '2' DAY,   CURRENT_DATE + INTERVAL '180' DAY, 'DISPONIVEL', 'OK'),
(1, 3, 1, 'Iogurte natural',              4, CURRENT_TIMESTAMP - INTERVAL '1' DAY,   CURRENT_DATE + INTERVAL '5' DAY,   'DISPONIVEL', 'OK'),
(1, 4, 9, 'Bananas',                      5, CURRENT_TIMESTAMP - INTERVAL '1' DAY,   CURRENT_DATE + INTERVAL '3' DAY,   'DISPONIVEL', 'OK'),
-- Capao Redondo: 55 de 60 kg (quase lotada)
(2, 5, 5, 'Feijao carioca',              20, CURRENT_TIMESTAMP - INTERVAL '3' DAY,   CURRENT_DATE + INTERVAL '200' DAY, 'DISPONIVEL', 'OK'),
(2, 6, 7, 'Enlatados (milho e ervilha)', 12, CURRENT_TIMESTAMP - INTERVAL '3' DAY,   CURRENT_DATE + INTERVAL '300' DAY, 'DISPONIVEL', 'OK'),
(2, 2, 3, 'Legumes variados',            10, CURRENT_TIMESTAMP - INTERVAL '1' DAY,   CURRENT_DATE + INTERVAL '4' DAY,   'DISPONIVEL', 'OK'),
(2, 7, 8, 'Sucos de caixinha',            8, CURRENT_TIMESTAMP - INTERVAL '2' DAY,   CURRENT_DATE + INTERVAL '60' DAY,  'DISPONIVEL', 'OK'),
(2, 1, 4, 'Paes de forma',                5, CURRENT_TIMESTAMP - INTERVAL '6' HOUR,  CURRENT_DATE + INTERVAL '2' DAY,   'DISPONIVEL', 'OK'),
-- Heliopolis: 26 de 70 kg (geladeira com temperatura alta)
(3, 3, 1, 'Leite integral',               8, CURRENT_TIMESTAMP - INTERVAL '1' DAY,   CURRENT_DATE + INTERVAL '4' DAY,   'DISPONIVEL', 'OK'),
(3, 8, 6, 'Marmitas de arroz e feijao',   6, CURRENT_TIMESTAMP - INTERVAL '4' HOUR,  CURRENT_DATE + INTERVAL '1' DAY,   'DISPONIVEL', 'OK'),
(3, 5, 5, 'Macarrao',                    12, CURRENT_TIMESTAMP - INTERVAL '4' DAY,   CURRENT_DATE + INTERVAL '240' DAY, 'DISPONIVEL', 'OK'),
-- Brasilandia: 22 de 60 kg
(4, 9, 9, 'Laranjas',                    10, CURRENT_TIMESTAMP - INTERVAL '1' DAY,   CURRENT_DATE + INTERVAL '6' DAY,   'DISPONIVEL', 'OK'),
(4, 4, 2, 'Frango congelado',             7, CURRENT_TIMESTAMP - INTERVAL '8' HOUR,  CURRENT_DATE + INTERVAL '2' DAY,   'DISPONIVEL', 'OK'),
(4, 10, 10, 'Ovos (5 duzias)',            5, CURRENT_TIMESTAMP - INTERVAL '2' DAY,   CURRENT_DATE + INTERVAL '12' DAY,  'DISPONIVEL', 'OK'),
-- Cidade Tiradentes: 8 de 70 kg (estoque baixo)
(5, 1, 4, 'Paes de forma',                3, CURRENT_TIMESTAMP - INTERVAL '2' DAY,   CURRENT_DATE,                      'DISPONIVEL', 'OK'),
(5, 6, 7, 'Sardinha em lata',             5, CURRENT_TIMESTAMP - INTERVAL '5' DAY,   CURRENT_DATE + INTERVAL '400' DAY, 'DISPONIVEL', 'OK'),
-- Itaquera: 12 de 80 kg (estoque baixo, com um item vencido)
(7, 7, 8, 'Agua mineral',                 6, CURRENT_TIMESTAMP - INTERVAL '6' DAY,   CURRENT_DATE + INTERVAL '365' DAY, 'DISPONIVEL', 'OK'),
(7, 9, 9, 'Macas',                        6, CURRENT_TIMESTAMP - INTERVAL '7' DAY,   CURRENT_DATE - INTERVAL '1' DAY,   'DISPONIVEL', 'OK'),
-- Jardim Angela: 24 de 60 kg (um item vencido)
(8, 8, 6, 'Sopas congeladas',            10, CURRENT_TIMESTAMP - INTERVAL '1' DAY,   CURRENT_DATE + INTERVAL '3' DAY,   'DISPONIVEL', 'OK'),
(8, 10, 1, 'Queijo mucarela',             4, CURRENT_TIMESTAMP - INTERVAL '9' DAY,   CURRENT_DATE - INTERVAL '2' DAY,   'DISPONIVEL', 'OK'),
(8, 2, 5, 'Aveia e fuba',                10, CURRENT_TIMESTAMP - INTERVAL '3' DAY,   CURRENT_DATE + INTERVAL '150' DAY, 'DISPONIVEL', 'OK'),
-- Se: 45 de 100 kg
(9, 5, 5, 'Itens de cesta basica',       25, CURRENT_TIMESTAMP - INTERVAL '2' DAY,   CURRENT_DATE + INTERVAL '120' DAY, 'DISPONIVEL', 'OK'),
(9, 3, 3, 'Verduras folhosas',            6, CURRENT_TIMESTAMP - INTERVAL '5' HOUR,  CURRENT_DATE + INTERVAL '1' DAY,   'DISPONIVEL', 'OK'),
(9, 6, 1, 'Requeijao',                    4, CURRENT_TIMESTAMP - INTERVAL '1' DAY,   CURRENT_DATE + INTERVAL '20' DAY,  'DISPONIVEL', 'OK'),
(9, 4, 9, 'Mamoes',                      10, CURRENT_TIMESTAMP - INTERVAL '1' DAY,   CURRENT_DATE + INTERVAL '2' DAY,   'DISPONIVEL', 'OK'),
-- Historico: doacoes ja retiradas ou descartadas
(1, 2, 4, 'Bolos caseiros',               4, CURRENT_TIMESTAMP - INTERVAL '5' DAY,   CURRENT_DATE - INTERVAL '3' DAY,   'RETIRADA',   'OK'),
(6, 8, 6, 'Marmitas de frango',           5, CURRENT_TIMESTAMP - INTERVAL '6' DAY,   CURRENT_DATE - INTERVAL '4' DAY,   'RETIRADA',   'OK'),
(9, 1, 4, 'Paes doces',                   6, CURRENT_TIMESTAMP - INTERVAL '4' DAY,   CURRENT_DATE - INTERVAL '2' DAY,   'DESCARTADA', 'VENCIDO');

-- ---------------------------------------------------------- ALERTAS
INSERT INTO alerta (id_geladeira, id_doacao, tipo, nivel, mensagem, dt_alerta, resolvido) VALUES
(3, NULL, 'TEMPERATURA', 'CRITICO', 'Temperatura em 9.8 C, acima do limite seguro de 8.0 C.',               CURRENT_TIMESTAMP - INTERVAL '10' MINUTE, FALSE),
(6, NULL, 'TEMPERATURA', 'CRITICO', 'Temperatura em 12.5 C. Geladeira enviada para manutencao.',            CURRENT_TIMESTAMP - INTERVAL '3' DAY,     TRUE),
(9, 29,   'VALIDADE',    'ALTO',    'Paes doces vencidos foram descartados.',                               CURRENT_TIMESTAMP - INTERVAL '2' DAY,     TRUE),
(10, NULL,'ESTOQUE_BAIXO','INFO',   'Geladeira desativada por falta de responsavel local.',                 CURRENT_TIMESTAMP - INTERVAL '30' DAY,    TRUE);

-- ---------------------------------------------------------- USUARIOS DE TESTE
-- Senhas de teste (BCrypt): Admin@123, Voluntario@123, Doador@123, Sensor@123
INSERT INTO usuario (nome, email, senha, papel, id_doador, ativo, dt_cadastro) VALUES
('Administrador da Rede', 'admin@geladeira.test',      '$2a$10$scAVeuFb5KclP9e4wX2aH.ZbEeY8XMzJBS5z1nheUmFEwrvPP8.DC', 'ADMIN',      NULL, TRUE, CURRENT_TIMESTAMP),
('Carla Voluntaria',      'voluntario@geladeira.test', '$2a$10$Rtt6QfXPHqmTnSWlJ.BN0ONwMp75GLw9QdCfe1vE9/LK62gXriH0W', 'VOLUNTARIO', NULL, TRUE, CURRENT_TIMESTAMP),
('Maria Aparecida Santos','doador@geladeira.test',     '$2a$10$o6c8fFtPn0xllPlweJYOAutEFfvNrWxQdwzPm0d5b1hd6EYlmecTy', 'DOADOR',     4,    TRUE, CURRENT_TIMESTAMP),
('Sensor IoT da Rede',    'sensor@geladeira.test',     '$2a$10$jny9dRJBhXdv0wJRAO6eZO4SLGh/phZuKdw1/mZxWqW80QDcpEq5y', 'SENSOR',     NULL, TRUE, CURRENT_TIMESTAMP);
