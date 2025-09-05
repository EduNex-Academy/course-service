#!/bin/bash

# =================================================================
#  API-BASED POPULATION SCRIPT FOR EDUNEX COURSE SERVICE
#  This script first DELETES all existing data via the API,
#  then populates it with a comprehensive set of fresh sample data.
#  It calls the course-service directly, bypassing the API gateway.
# =================================================================

# --- Configuration ---
# The base URL for your Course Service
BASE_URL="http://localhost:8082/api"

# Sample UUIDs (as if they came from Keycloak)
INSTRUCTOR_JOHN_DOE="123e4567-e89b-12d3-a456-426614174000"
INSTRUCTOR_JANE_SMITH="987e6543-e21b-12d3-a456-426614174001"
USER_ALICE="f47ac10b-58cc-4372-a567-0e02b2c3d479"
USER_BOB="a1b2c3d4-e5f6-7890-1234-567890abcdef"

# --- Helper Functions ---
function api_get {
    curl -s -X GET "${BASE_URL}${1}"
}

function api_post {
    curl -s -X POST "${BASE_URL}${1}" -H "Content-Type: application/json" -d "${2}"
}

function api_delete {
    # Send DELETE request and suppress output
    curl -s -o /dev/null -w "%{http_code}" -X DELETE "${BASE_URL}${1}"
}

# =============================================
#  1. CLEAR ALL EXISTING DATA
# =============================================
echo "🧹 Clearing all existing data via API..."

# The deletion order is important to avoid foreign key constraint errors.
# We delete child objects before parent objects.

echo "   - Deleting Quiz Results..."
for id in $(api_get "/quiz-results" | jq -r '.[].id'); do api_delete "/quiz-results/${id}"; done

echo "   - Deleting Progress Records..."
for id in $(api_get "/progress" | jq -r '.[].id'); do api_delete "/progress/${id}"; done

echo "   - Deleting Enrollments..."
for id in $(api_get "/enrollments" | jq -r '.[].id'); do api_delete "/enrollments/${id}"; done

# Per the API, creating a quiz also creates its questions/answers.
# Deleting the parent quiz should handle the children.
echo "   - Deleting Quizzes..."
for id in $(api_get "/quizzes" | jq -r '.[].id'); do api_delete "/quizzes/${id}"; done

echo "   - Deleting Modules..."
for id in $(api_get "/modules" | jq -r '.[].id'); do api_delete "/modules/${id}"; done

echo "   - Deleting Courses..."
for id in $(api_get "/courses" | jq -r '.[].id'); do api_delete "/courses/${id}"; done

echo "✅ All existing data has been cleared."

