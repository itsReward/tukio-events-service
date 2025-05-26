-- Database Migration: V2__Add_Event_Attendance_Rating.sql
-- Path: src/main/resources/db/migration/V2__Add_Event_Attendance_Rating.sql

-- Event Attendance table
CREATE TABLE IF NOT EXISTS event_attendance (
                                                id BIGSERIAL PRIMARY KEY,
                                                event_id BIGINT NOT NULL,
                                                user_id BIGINT NOT NULL,
                                                attended BOOLEAN NOT NULL,
                                                recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
                                                UNIQUE(event_id, user_id) -- One attendance record per user per event
);

-- Indexes for performance
CREATE INDEX idx_event_attendance_event_id ON event_attendance (event_id);
CREATE INDEX idx_event_attendance_user_id ON event_attendance (user_id);
CREATE INDEX idx_event_attendance_attended ON event_attendance (attended);
CREATE INDEX idx_event_attendance_recorded_at ON event_attendance (recorded_at);

-- Event Ratings table
CREATE TABLE IF NOT EXISTS event_ratings (
                                             id BIGSERIAL PRIMARY KEY,
                                             event_id BIGINT NOT NULL,
                                             user_id BIGINT NOT NULL,
                                             rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
                                             comment TEXT,
                                             rated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
                                             UNIQUE(event_id, user_id) -- One rating per user per event
);

-- Indexes for performance
CREATE INDEX idx_event_ratings_event_id ON event_ratings (event_id);
CREATE INDEX idx_event_ratings_user_id ON event_ratings (user_id);
CREATE INDEX idx_event_ratings_rating ON event_ratings (rating);
CREATE INDEX idx_event_ratings_rated_at ON event_ratings (rated_at);

-- Rating Categories table (for category-specific ratings)
CREATE TABLE IF NOT EXISTS rating_categories (
                                                 rating_id BIGINT NOT NULL,
                                                 category_name VARCHAR(100) NOT NULL,
                                                 category_rating INT NOT NULL CHECK (category_rating >= 1 AND category_rating <= 5),

                                                 PRIMARY KEY (rating_id, category_name),
                                                 FOREIGN KEY (rating_id) REFERENCES event_ratings(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_rating_categories_rating_id ON rating_categories (rating_id);
CREATE INDEX idx_rating_categories_category ON rating_categories (category_name);

-- Add comments to tables for documentation
COMMENT ON TABLE event_attendance IS 'Records user attendance for events - can be marked after event starts';
COMMENT ON TABLE event_ratings IS 'User ratings for events - can only be submitted after event ends and if user attended';
COMMENT ON TABLE rating_categories IS 'Category-specific ratings (e.g., venue, organization, content)';

--- Views for Analytics

-- View: Event Attendance Summary
CREATE OR REPLACE VIEW event_attendance_summary AS
SELECT
    e.id AS event_id,
    e.title AS event_title,
    e.start_time,
    e.end_time,
    COUNT(ea.id) AS total_responses,
    COUNT(CASE WHEN ea.attended = true THEN 1 END) AS attended_count,
    COUNT(CASE WHEN ea.attended = false THEN 1 END) AS did_not_attend_count,
    ROUND(
            (COUNT(CASE WHEN ea.attended = true THEN 1 END) * 100.0) /
            NULLIF(COUNT(ea.id), 0), 2
    ) AS attendance_percentage
FROM events e
         LEFT JOIN event_attendance ea ON e.id = ea.event_id
GROUP BY e.id, e.title, e.start_time, e.end_time;

-- View: Event Rating Summary
CREATE OR REPLACE VIEW event_rating_summary AS
SELECT
    e.id AS event_id,
    e.title AS event_title,
    COUNT(er.id) AS total_ratings,
    ROUND(AVG(er.rating), 2) AS average_rating,
    COUNT(CASE WHEN er.rating = 1 THEN 1 END) AS one_star,
    COUNT(CASE WHEN er.rating = 2 THEN 1 END) AS two_star,
    COUNT(CASE WHEN er.rating = 3 THEN 1 END) AS three_star,
    COUNT(CASE WHEN er.rating = 4 THEN 1 END) AS four_star,
    COUNT(CASE WHEN er.rating = 5 THEN 1 END) AS five_star
FROM events e
         LEFT JOIN event_ratings er ON e.id = er.event_id
GROUP BY e.id, e.title;

-- View: User Event Participation
CREATE OR REPLACE VIEW user_event_participation AS
SELECT
    ea.user_id,
    COUNT(ea.id) AS events_responded_to,
    COUNT(CASE WHEN ea.attended = true THEN 1 END) AS events_attended,
    COUNT(er.id) AS events_rated,
    ROUND(AVG(er.rating), 2) AS average_rating_given
FROM event_attendance ea
         LEFT JOIN event_ratings er ON ea.event_id = er.event_id AND ea.user_id = er.user_id
WHERE ea.attended = true
GROUP BY ea.user_id;

---Stored Procedures

-- Procedure: Get events user can rate
-- PostgreSQL uses functions for this purpose, not procedures with DELIMITER.
CREATE OR REPLACE FUNCTION GetEventsUserCanRate(p_user_id BIGINT)
    RETURNS TABLE (
                      id BIGINT,
                      title VARCHAR,
                      end_time TIMESTAMP,
                      attended BOOLEAN,
                      rating_status TEXT
                  ) AS $$
BEGIN
    RETURN QUERY
        SELECT DISTINCT
            e.id,
            e.title,
            e.end_time,
            ea.attended,
            CASE
                WHEN er.id IS NOT NULL THEN 'ALREADY_RATED'
                WHEN ea.attended = false THEN 'DID_NOT_ATTEND'
                WHEN NOW() < e.end_time THEN 'EVENT_NOT_ENDED'
                ELSE 'CAN_RATE'
                END AS rating_status
        FROM events e
                 LEFT JOIN event_attendance ea ON e.id = ea.event_id AND ea.user_id = p_user_id
                 LEFT JOIN event_ratings er ON e.id = er.event_id AND er.user_id = p_user_id
        WHERE ea.id IS NOT NULL  -- User has some interaction with the event
        ORDER BY e.end_time DESC;
END;
$$ LANGUAGE plpgsql;

-- Procedure: Get events user can mark attendance for
-- PostgreSQL uses functions for this purpose.
CREATE OR REPLACE FUNCTION GetEventsUserCanMarkAttendance(p_user_id BIGINT)
    RETURNS TABLE (
                      id BIGINT,
                      title VARCHAR,
                      start_time TIMESTAMP,
                      end_time TIMESTAMP,
                      attended BOOLEAN,
                      attendance_status TEXT
                  ) AS $$
BEGIN
    RETURN QUERY
        SELECT
            e.id,
            e.title,
            e.start_time,
            e.end_time,
            ea.attended,
            CASE
                WHEN NOW() < e.start_time THEN 'EVENT_NOT_STARTED'
                WHEN er.id IS NOT NULL THEN 'REGISTERED' -- Assuming event_registrations table exists and "er.id IS NOT NULL" means registered
                ELSE 'CAN_MARK_ATTENDANCE'
                END AS attendance_status
        FROM events e
                 INNER JOIN event_registrations er ON e.id = er.event_id AND er.user_id = p_user_id
                 LEFT JOIN event_attendance ea ON e.id = ea.event_id AND ea.user_id = p_user_id
        WHERE NOW() >= e.start_time  -- Event has started
        ORDER BY e.start_time DESC;
END;
$$ LANGUAGE plpgsql;

---Triggers for Data Integrity

-- Trigger: Ensure user attended before rating
CREATE OR REPLACE FUNCTION check_attendance_before_rating_func()
    RETURNS TRIGGER AS $$
DECLARE
    attendance_exists INT DEFAULT 0;
    user_attended BOOLEAN DEFAULT false;
BEGIN
    SELECT COUNT(*), COALESCE(attended, false)
    INTO attendance_exists, user_attended
    FROM event_attendance
    WHERE event_id = NEW.event_id AND user_id = NEW.user_id;

    IF attendance_exists = 0 OR user_attended = false THEN
        RAISE EXCEPTION 'User must attend event before rating';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_attendance_before_rating
    BEFORE INSERT ON event_ratings
    FOR EACH ROW
EXECUTE FUNCTION check_attendance_before_rating_func();

-- Trigger: Check event has ended before rating
CREATE OR REPLACE FUNCTION check_event_ended_before_rating_func()
    RETURNS TRIGGER AS $$
DECLARE
    event_end_time TIMESTAMP;
BEGIN
    SELECT end_time INTO event_end_time
    FROM events
    WHERE id = NEW.event_id;

    IF NOW() < event_end_time THEN
        RAISE EXCEPTION 'Cannot rate event before it ends';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_event_ended_before_rating
    BEFORE INSERT ON event_ratings
    FOR EACH ROW
EXECUTE FUNCTION check_event_ended_before_rating_func();

-- Trigger: Check event has started before marking attendance
CREATE OR REPLACE FUNCTION check_event_started_before_attendance_func()
    RETURNS TRIGGER AS $$
DECLARE
    event_start_time TIMESTAMP;
BEGIN
    SELECT start_time INTO event_start_time
    FROM events
    WHERE id = NEW.event_id;

    IF NOW() < event_start_time THEN
        RAISE EXCEPTION 'Cannot mark attendance before event starts';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_event_started_before_attendance
    BEFORE INSERT ON event_attendance
    FOR EACH ROW
EXECUTE FUNCTION check_event_started_before_attendance_func();

--- Rating Category Types and Performance Indexes

-- Insert some sample category types for ratings
CREATE TABLE IF NOT EXISTS rating_category_types (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     name VARCHAR(100) NOT NULL UNIQUE,
                                                     description TEXT,
                                                     is_active BOOLEAN DEFAULT true,
                                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO rating_category_types (name, description) VALUES
                                                          ('venue', 'Rating for the event venue and facilities'),
                                                          ('organization', 'Rating for event organization and management'),
                                                          ('content', 'Rating for event content and speakers'),
                                                          ('networking', 'Rating for networking opportunities'),
                                                          ('catering', 'Rating for food and beverages provided'),
                                                          ('value', 'Rating for overall value for time invested')
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description;

-- Create performance indexes for common queries
CREATE INDEX idx_events_time_range ON events(start_time, end_time);
CREATE INDEX idx_event_attendance_user_event ON event_attendance(user_id, event_id, attended);
CREATE INDEX idx_event_ratings_user_event ON event_ratings(user_id, event_id, rating);
CREATE INDEX idx_event_ratings_event_rating ON event_ratings(event_id, rating DESC);

--- Constraints for Data Quality

-- Add constraints to ensure data quality
ALTER TABLE event_attendance
    ADD CONSTRAINT fk_attendance_event
        FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE;

ALTER TABLE event_ratings
    ADD CONSTRAINT fk_rating_event
        FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE;

--- Audit Tables for Tracking Changes

-- Create audit tables for tracking changes
CREATE TABLE IF NOT EXISTS event_attendance_audit (
                                                      id BIGSERIAL PRIMARY KEY,
                                                      event_id BIGINT NOT NULL,
                                                      user_id BIGINT NOT NULL,
                                                      old_attended BOOLEAN,
                                                      new_attended BOOLEAN,
                                                      action_type VARCHAR(20) NOT NULL, -- INSERT, UPDATE, DELETE
                                                      changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                      changed_by BIGINT -- Could be user_id or admin_id
);

CREATE TABLE IF NOT EXISTS event_ratings_audit (
                                                   id BIGSERIAL PRIMARY KEY,
                                                   rating_id BIGINT NOT NULL,
                                                   event_id BIGINT NOT NULL,
                                                   user_id BIGINT NOT NULL,
                                                   old_rating INT,
                                                   new_rating INT,
                                                   old_comment TEXT,
                                                   new_comment TEXT,
                                                   action_type VARCHAR(20) NOT NULL,
                                                   changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                   changed_by BIGINT
);

--- Triggers for Audit Logging

-- Triggers for audit logging
CREATE OR REPLACE FUNCTION event_attendance_audit_trigger_func()
    RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO event_attendance_audit (
            event_id, user_id, new_attended, action_type
        ) VALUES (
                     NEW.event_id, NEW.user_id, NEW.attended, 'INSERT'
                 );
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO event_attendance_audit (
            event_id, user_id, old_attended, new_attended, action_type
        ) VALUES (
                     NEW.event_id, NEW.user_id, OLD.attended, NEW.attended, 'UPDATE'
                 );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER event_attendance_audit_trigger
    AFTER INSERT OR UPDATE ON event_attendance
    FOR EACH ROW
EXECUTE FUNCTION event_attendance_audit_trigger_func();

CREATE OR REPLACE FUNCTION event_ratings_audit_trigger_func()
    RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO event_ratings_audit (
            rating_id, event_id, user_id, new_rating, new_comment, action_type
        ) VALUES (
                     NEW.id, NEW.event_id, NEW.user_id, NEW.rating, NEW.comment, 'INSERT'
                 );
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO event_ratings_audit (
            rating_id, event_id, user_id, old_rating, new_rating,
            old_comment, new_comment, action_type
        ) VALUES (
                     NEW.id, NEW.event_id, NEW.user_id, OLD.rating, NEW.rating,
                     OLD.comment, NEW.comment, 'UPDATE'
                 );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER event_ratings_audit_trigger
    AFTER INSERT OR UPDATE ON event_ratings
    FOR EACH ROW
EXECUTE FUNCTION event_ratings_audit_trigger_func();