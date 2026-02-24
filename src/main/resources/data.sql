-- Seed data for Payment Simulator

-- Simulation Profiles
INSERT INTO simulation_profile (name, description, default_scenario, latency_ms, amount_limit, active) VALUES
('FAST_ACCEPT', 'Fast acceptance profile – approves all transactions with no latency', 'ACCEPT', 0, 999999999, true);
INSERT INTO simulation_profile (name, description, default_scenario, latency_ms, amount_limit, active) VALUES
('SLOW_RANDOM', 'Slow profile with random-like behaviour – uses rules engine with 2s latency', 'ACCEPT', 2000, 50000, true);

-- Terminals
INSERT INTO terminal (terminal_id, merchant_id, active) VALUES ('TERM0001', 'MERCH001', true);
INSERT INTO terminal (terminal_id, merchant_id, active) VALUES ('TERM0002', 'MERCH001', true);
INSERT INTO terminal (terminal_id, merchant_id, active) VALUES ('TERM0003', 'MERCH002', true);

-- Simulation Rules (ordered by priority – lower = higher)
INSERT INTO simulation_rule (name, priority, enabled, condition_type, condition_value, outcome_scenario, response_code39) VALUES
('Expired Card', 1, true, 'EXPIRED', '', 'EXPIRED_CARD', '54');
INSERT INTO simulation_rule (name, priority, enabled, condition_type, condition_value, outcome_scenario, response_code39) VALUES
('Insufficient Funds', 2, true, 'AMOUNT_GREATER_THAN', '50000', 'INSUFFICIENT_FUNDS', '51');
INSERT INTO simulation_rule (name, priority, enabled, condition_type, condition_value, outcome_scenario, response_code39) VALUES
('Unknown Terminal', 3, true, 'TERMINAL_UNKNOWN', '', 'REFUSE', '05');
INSERT INTO simulation_rule (name, priority, enabled, condition_type, condition_value, outcome_scenario, response_code39) VALUES
('Visa PAN Prefix', 4, true, 'PAN_PREFIX', '4', 'ACCEPT', '00');
INSERT INTO simulation_rule (name, priority, enabled, condition_type, condition_value, outcome_scenario, response_code39) VALUES
('Default Accept', 10, true, 'ALWAYS', '', 'ACCEPT', '00');
