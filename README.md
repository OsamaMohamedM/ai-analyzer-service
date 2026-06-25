# AI Analyzer Service

`ai-analyzer-service` is a passive, read-only Spring Boot 3 service that ranks developers for tasks and bug reports. It is independent of the VTBA research application and contains no TSV, filesystem, experiment-runner, ingestion, migration, authentication, or dataset-generation code.

## Runtime

- Java 17+
- Maven 3.9+
- SQL Server

Required environment variables:

```text
AI_DB_URL
AI_DB_USERNAME
AI_DB_PASSWORD
AI_PROJECT_ID
```

The database user should have `SELECT` permission only.

## API

```http
POST /internal/v1/ai/recommendations/tasks
Content-Type: application/json

{
  "title": "Fix JSON serialization",
  "description": "Resolve the Spring API response regression"
}
```

```http
POST /internal/v1/ai/recommendations/bug-reports
POST /internal/v1/ai/recommendations/bugs
Content-Type: application/json

{
  "bugReportText": "JSON serialization fails for nested objects"
}
```

The developer-key endpoint ranks only the submitted identities while aggregating each identity's
bug-assignment and commit-code history across every project:

```http
POST /api/ai/recommend-developers-for-task
Content-Type: application/json

{
  "taskTitle": "Fix login token expiration bug",
  "taskDescription": "Refresh token expiration is not handled correctly.",
  "developers": [
    { "developerKey": "pra85" },
    { "developerKey": "drnic" }
  ]
}
```

Developer keys are matched using `LOWER(TRIM(developers.name))`. Duplicate keys are collapsed,
unknown keys are skipped, and an entirely unknown list produces an empty successful response.
This endpoint does not use `AI_PROJECT_ID`; the existing endpoints remain project scoped.

## Database contract

The service assumes an already-populated schema. It never creates or modifies these tables.

| Table | Required columns |
|---|---|
| `developers` | `developer_id`, `project_id`, `name`, `email`, `is_active` |
| `bug_reports` | `bug_report_id`, `project_id`, `external_bug_number`, `reported_at`, `unique_bug_sequence`, `original_word_count` |
| `bug_assignments` | `assignment_id`, `bug_report_id`, `developer_id`, `assigned_at`, `assignment_sequence` |
| `bug_terms` | `bug_report_id`, `term`, `frequency` |
| `code_commits` | `commit_id`, `project_id`, `commit_sha`, `developer_id`, `committed_at`, `commit_sequence`, `code_embedding` |
| `term_statistics` | `term`, `global_weight`, `embedding` |
| `developer_term_statistics` | `developer_id`, `term`, `bug_frequency`, `code_frequency`, `last_observed_at` |
| `project_term_statistics` | `project_id`, `term`, `weight`, `developer_count` |
| `tag_expansions` | `source_term`, `target_term`, `weight` |

`code_embedding` and `embedding` are SQL Server `varbinary(max)` values containing exactly 300 little-endian IEEE-754 float32 values. Commit embeddings are expected to be normalized during population. Term embeddings represent the supervised `TAG_` vocabulary rows used by the existing W2V model.

`unique_bug_sequence`, `assignment_sequence`, and `commit_sequence` must be monotonically increasing within a project. Project term weights must be normalized to the same range as global term weights. Tag expansion weights must be between zero and one.

The configured `AI_PROJECT_ID` scopes every recommendation. This preserves project-specific candidates and statistics without changing the required .NET request contract.

## Ranking behavior

The default configuration preserves the requested production variant:

- monthly commit recency
- 80% normalized bug-history score and 20% normalized commit-code W2V score
- unique-bug-distance history recency
- database-backed Stack Overflow tag expansion with six neighbors and weight 0.25
- project-only term weighting through a project blend weight of 1.0

All values are typed under `ai.ranking` in `application.yml`.

## Package structure

```text
com.company.aianalyzer
  api
  application
  application.dto
  config
  domain.entity
  domain.repository
  exception
  ranking
```
