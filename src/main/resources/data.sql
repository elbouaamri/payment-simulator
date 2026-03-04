-- Seed data for Payment Simulator

-- Simulation Profiles
MERGE INTO simulation_profile (name, description, default_scenario, latency_ms, amount_limit, active) KEY (name) VALUES
('FAST_ACCEPT', 'Fast acceptance profile – approves all transactions with no latency', 'ACCEPT', 0, 999999999, true);
MERGE INTO simulation_profile (name, description, default_scenario, latency_ms, amount_limit, active) KEY (name) VALUES
('SLOW_RANDOM', 'Slow profile with random-like behaviour – uses rules engine with 2s latency', 'ACCEPT', 2000, 50000, true);

-- Terminals
MERGE INTO terminal (terminal_id, merchant_id, active) KEY (terminal_id) VALUES ('TERM0001', 'MERCH001', true);
MERGE INTO terminal (terminal_id, merchant_id, active) KEY (terminal_id) VALUES ('TERM0002', 'MERCH001', true);
MERGE INTO terminal (terminal_id, merchant_id, active) KEY (terminal_id) VALUES ('TERM0003', 'MERCH002', true);

-- Simulation Rules (ordered by priority – lower = higher)
MERGE INTO simulation_rule (name, priority, enabled, condition_type, condition_value, outcome_scenario, response_code39) KEY (name) VALUES
('Expired Card', 1, true, 'EXPIRED', '', 'EXPIRED_CARD', '54');
MERGE INTO simulation_rule (name, priority, enabled, condition_type, condition_value, outcome_scenario, response_code39) KEY (name) VALUES
('Insufficient Funds', 2, true, 'AMOUNT_GREATER_THAN', '50000', 'INSUFFICIENT_FUNDS', '51');
MERGE INTO simulation_rule (name, priority, enabled, condition_type, condition_value, outcome_scenario, response_code39) KEY (name) VALUES
('Unknown Terminal', 3, true, 'TERMINAL_UNKNOWN', '', 'REFUSE', '05');
MERGE INTO simulation_rule (name, priority, enabled, condition_type, condition_value, outcome_scenario, response_code39) KEY (name) VALUES
('Visa PAN Prefix', 4, true, 'PAN_PREFIX', '4', 'ACCEPT', '00');
MERGE INTO simulation_rule (name, priority, enabled, condition_type, condition_value, outcome_scenario, response_code39) KEY (name) VALUES
('Default Accept', 10, true, 'ALWAYS', '', 'ACCEPT', '00');
