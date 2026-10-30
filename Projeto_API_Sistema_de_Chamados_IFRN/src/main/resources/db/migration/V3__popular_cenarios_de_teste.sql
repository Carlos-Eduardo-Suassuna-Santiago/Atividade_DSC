-- Usuários adicionais para testar todos os papéis e combinações de filtros.
INSERT INTO usuarios (id, nome, email, matricula, tipo, setor_id) VALUES
    (5, 'Eduardo Solicitante', 'eduardo@ifrn.edu.br', '20260005', 'SOLICITANTE', NULL),
    (6, 'Fernanda Técnica', 'fernanda@func.ifrn.edu.br', 'SERV003', 'TECNICO', 1);

-- Chamados em cada estado do ciclo de vida e com as três prioridades.
INSERT INTO chamados
    (id, titulo, descricao, local, prioridade, status, data_abertura,
     data_fechamento, solucao, solicitante_id, categoria_id, tecnico_id)
VALUES
    (1, 'Computador não liga', 'Equipamento não inicia no laboratório.', 'Laboratório 1',
     'ALTA', 'ABERTO', CURRENT_TIMESTAMP - INTERVAL '1 day', NULL, NULL, 1, 1, NULL),
    (2, 'Wi-Fi instável', 'A conexão cai durante as aulas.', 'Laboratório 2',
     'MEDIA', 'EM_ATENDIMENTO', CURRENT_TIMESTAMP - INTERVAL '2 days', NULL, NULL, 2, 2, 3),
    (3, 'Projetor sem imagem', 'Projetor não apresenta imagem.', 'Sala 10',
     'ALTA', 'RESOLVIDO', CURRENT_TIMESTAMP - INTERVAL '5 days',
     CURRENT_TIMESTAMP - INTERVAL '4 days', 'Cabo HDMI substituído e equipamento testado.', 5, 4, 4),
    (4, 'Tomada danificada', 'Tomada apresenta mau contato.', 'Bloco B',
     'BAIXA', 'CANCELADO', CURRENT_TIMESTAMP - INTERVAL '6 days',
     CURRENT_TIMESTAMP - INTERVAL '5 days', NULL, 1, 3, NULL);

-- Mantém as sequences posteriores aos IDs definidos acima.
SELECT setval(pg_get_serial_sequence('usuarios', 'id'), GREATEST((SELECT MAX(id) FROM usuarios), 1), true);
SELECT setval(pg_get_serial_sequence('chamados', 'id'), GREATEST((SELECT MAX(id) FROM chamados), 1), true);

INSERT INTO interacoes (texto, data_hora, chamado_id, autor_id) VALUES
    ('Chamado aberto e aguardando atendimento.', CURRENT_TIMESTAMP - INTERVAL '1 day', 1, 1),
    ('Vou verificar o roteador e os pontos de acesso.', CURRENT_TIMESTAMP - INTERVAL '1 day', 2, 3),
    ('A conexão melhorou, mas ainda apresenta quedas.', CURRENT_TIMESTAMP - INTERVAL '20 hours', 2, 2),
    ('O problema foi resolvido após a troca do cabo.', CURRENT_TIMESTAMP - INTERVAL '4 days', 3, 4),
    ('Solicitação cancelada pelo solicitante.', CURRENT_TIMESTAMP - INTERVAL '5 days', 4, 1);
