-- Path: src/main/resources/db/migration/V1__initial_schema.sql

-- This is an initial migration that will establish Flyway's control over the schema
-- We're using IF NOT EXISTS to avoid errors if tables already exist

-- Event Categories table
CREATE TABLE IF NOT EXISTS event_categories (
                                                id BIGSERIAL PRIMARY KEY,
                                                name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    color VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Events table
CREATE TABLE IF NOT EXISTS events (
                                      id BIGSERIAL PRIMARY KEY,
                                      title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category_id BIGINT NOT NULL REFERENCES event_categories(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    location VARCHAR(255) NOT NULL,
    venue_id BIGINT NULL,
    max_participants INT NOT NULL,
    organizer VARCHAR(255) NOT NULL,
    organizer_id BIGINT NOT NULL,
    image_url VARCHAR(255) NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Event Tags table (many-to-many relationship with events)
CREATE TABLE IF NOT EXISTS event_tags (
                                          event_id BIGINT NOT NULL REFERENCES events(id),
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (event_id, tag)
    );

-- Event Registrations table
CREATE TABLE IF NOT EXISTS event_registrations (
                                                   id BIGSERIAL PRIMARY KEY,
                                                   event_id BIGINT NOT NULL REFERENCES events(id),
    user_id BIGINT NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'REGISTERED',
    check_in_time TIMESTAMP NULL,
    feedback TEXT NULL,
    rating INT NULL CHECK (rating IS NULL OR (rating >= 1 AND rating <= 5)),
    registration_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_events_start_time ON events(start_time);
CREATE INDEX IF NOT EXISTS idx_events_category_id ON events(category_id);
CREATE INDEX IF NOT EXISTS idx_events_status ON events(status);
CREATE INDEX IF NOT EXISTS idx_events_organizer_id ON events(organizer_id);
CREATE INDEX IF NOT EXISTS idx_events_venue_id ON events(venue_id);
CREATE INDEX IF NOT EXISTS idx_event_registrations_event_id ON event_registrations(event_id);
CREATE INDEX IF NOT EXISTS idx_event_registrations_user_id ON event_registrations(user_id);
CREATE INDEX IF NOT EXISTS idx_event_registrations_status ON event_registrations(status);

-- Initial event categories
INSERT INTO event_categories (name, description, color)
VALUES
    ('Academic', 'Lectures, seminars, and academic workshops', '#3498db'),
    ('Social', 'Social gatherings and networking events', '#e74c3c'),
    ('Cultural', 'Cultural performances, exhibitions, and celebrations', '#9b59b6'),
    ('Sports', 'Sports competitions and fitness activities', '#2ecc71'),
    ('Workshop', 'Hands-on learning and skill-building sessions', '#f39c12')
    ON CONFLICT (name) DO NOTHING;