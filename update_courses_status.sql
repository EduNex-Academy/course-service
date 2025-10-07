-- Update all existing courses to have PUBLISHED status
UPDATE courses SET status = 'PUBLISHED' WHERE status IS NULL;