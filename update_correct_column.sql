-- Create a new column 'correct' with default value false
ALTER TABLE quiz_answers ADD COLUMN correct BOOLEAN DEFAULT FALSE;

-- Copy data from is_correct to correct
UPDATE quiz_answers SET correct = is_correct;

-- Make the correct column not null now that it has values
ALTER TABLE quiz_answers ALTER COLUMN correct SET NOT NULL;

-- Drop the old column
ALTER TABLE quiz_answers DROP COLUMN is_correct;