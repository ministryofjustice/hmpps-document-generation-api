# hmpps-document-generation-api

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/hmpps-document-generation-api/badge?style=flat)](https://github-community.service.justice.gov.uk/repository-standards/hmpps-document-generation-api)
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-document-generation-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://document-generation-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)

Database schema: https://ministryofjustice.github.io/hmpps-document-generation-api/schema-spy-report

---

## What this service does

A single-purpose document generation API. It retrieves Word document templates from the [HMPPS Document Management API](https://github.com/ministryofjustice/hmpps-document-management-api), applies variable substitution using bookmark based replacement via `docx4j`, and returns the generated document to the caller as a buffered response. Generation events are audited.

The service is designed as a shared DPS capability. It has no knowledge of any specific HMPPS domain, and any DPS service that needs template-based document generation can use it. It is currently used exclusively by the **Receptions and External Movements** product set.

The reference UI implementation is [hmpps-document-generation-ui](https://github.com/ministryofjustice/hmpps-document-generation-ui).

### Key design constraints

- No document storage - documents are generated on demand and returned to the caller
- No NOMIS dependency - template variables are supplied by the calling service
- No domain logic - the API does not know what the documents are for

---

## Template variables

Variables are substituted into Word document bookmarks at generation time. The caller supplies all values; pre-population from DPS sources (Prisoner Search, Prison Register, etc.) is the responsibility of the calling UI.

Two variables are injected automatically by the API at generation time and cannot be overridden by the caller:

| Code | Description |
|---|---|
| `dateNow` | Date of generation (`dd/MM/yyyy`) |
| `dateTimeNow` | Date and time of generation (`dd/MM/yyyy hh:mm:ss`) |

### PERSON — Prisoner details

Pre-populated from [Prisoner Search API](https://prisoner-search-dev.prison.service.justice.gov.uk/swagger-ui/index.html) `GET /prisoner/{id}` by the calling UI.

| Code | Description | Type |
|---|---|---|
| `perName` | Full name | String |
| `perFirstName` | First name | String |
| `perMiddleNames` | Middle names | String |
| `perLastName` | Last name | String |
| `perImage` | Prisoner photo | Binary |
| `perPrsnNo` | Prison number | String |
| `perCro` | CRO number | String |
| `perPnc` | PNC number | String |
| `perBookNo` | Booking number | String |
| `perDob` | Date of birth | Date |
| `perSecCat` | Security category | String |
| `perLocation` | Location | String |

**`perImage` is handled differently from all other variables.** Rather than being passed as a string in the `variables` map, the prisoner image must be fetched by the calling service from the Prison API and sent as a named multipart file part (`perImage`) alongside the JSON request body. The API does not fetch the image itself.

The calling UI fetches the image and constructs the multipart request like this (from [hmpps-document-generation-ui](https://github.com/ministryofjustice/hmpps-document-generation-ui)):

```typescript
// Fetch the image as a buffer from the Prison API
const buffer = await this.services.prisonApiService.getPrisonerImageAsBuffer({ res }, req.body['perImage'])
const image = { buffer, originalname: `${req.body['perImage']}.png` }

// Pass as a named file alongside the variables
await this.services.documentGenerationService.generateDocument(
  { res },
  templateId,
  filename,
  variables,        // all other variable values — perImage is excluded from this map
  image,            // sent as multipart file part named 'perImage'
)
```

Which produces the following `POST /templates/{id}/document` request:

```typescript
// From documentGenerationService.ts
this.apiClient.withContext(context).post<Buffer>({
  path: `/templates/${id}/document`,
  responseType: 'application/msword',
  multipartData: { data: { filename, variables } },
  ...(prisonerImage ? { files: { perImage: prisonerImage } } : {}),
})
```

The `variables` map contains all other variable values keyed by their variable code, for example:

```json
{
  "filename": "ROTL_LIC1_A1234BC_user_2026-04-21_14-30-00.docx",
  "variables": {
    "perName": "John Smith",
    "perPrsnNo": "A1234BC",
    "perDob": "1980-06-15",
    "prsnName": "HMP Brixton",
    "prsnAddress": "Jebb Avenue\nBrixton\nLondon\nSW2 5XF",
    "tapStartDate": "2026-05-01",
    "tapEndDate": "2026-05-03",
    "tapCat": "ROTL - Resettlement day release"
  }
}
```

### PRISON — Prison details

Pre-populated from [Prison Register API](https://prison-register-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html) `GET /prisons/id/{prisonId}` by the calling UI.

| Code | Description | Type |
|---|---|---|
| `prsnCode` | Prison code | String |
| `prsnName` | Prison name | String |
| `prsnAddress` | Prison address | String |
| `prsnPhone` | Prison phone number | String |
| `prsnEmailFax` | Prison email or fax | String |
| `prsnSecCat` | Prison security category | String |

### TEMPORARY_ABSENCE — Absence information

Pre-populated from the External Movements API by the calling UI.

| Code | Description | Type |
|---|---|---|
| `tapStartDate` | Start date | Date |
| `tapStartTime` | Start time | Time |
| `tapEndDate` | Expiry date | Date |
| `tapEndTime` | Expiry time | Time |
| `tapCat` | Reason for absence | String |

### SENTENCE — Sentence details

User-supplied — no DPS source available for pre-population.

| Code | Description | Type |
|---|---|---|
| `sentMainOff` | Main offence | String |
| `sentCurOff` | Current offence | String |
| `sentEarliestCrtApp` | Earliest court appearance | Date |
| `sentDate` | Date of sentence | Date |
| `sentLenYears` | Sentence length — years | Number |
| `sentLenMonths` | Sentence length — months | Number |
| `sentLenDays` | Sentence length — days | Number |
| `sentArdCrd` | ARD/CRD | Date |
| `sentCrd` | CRD | Date |
| `sentPed` | PED or review date | Date |
| `sentSled` | SLED | Date |

### OFFENDER_MANAGER — Manager details

User-supplied — no DPS source available for pre-population.

| Code | Description | Type |
|---|---|---|
| `omApptDate` | Date of initial appointment with offender manager after release on licence | Date |
| `omApptTime` | Start time | Time |
| `omApptAddr` | Address of probation office where initial appointment is scheduled | String |

---

## Template deployment

Templates are not deployed directly to each environment. Instead, each environment pulls templates and their configuration from the environment below it on startup. Dev is the starting point — templates are authored and tested there before propagating upward.

### How it works

On startup, the API reads the `service.include-templates` list from its config. For each template code in that list, it calls its own API in the environment below to retrieve the template file and configuration, then upserts them locally. The propagation chain is:

```
dev  →  (pulled by) preprod  →  (pulled by) prod
```

The source URL and template list are configured per environment:

```yaml
# application-preprod.yml
integration:
  template-configuration:
    url: "https://document-generation-api-dev.hmpps.service.justice.gov.uk"

service:
  allow-pulling-templates: true
  auto-pull-templates: true
  include-templates:
    - TAP_ROTL_LIC1
    - TAP_ROTL_NOTIFY
    # ... other template codes
```

```yaml
# application-prod.yml
integration:
  template-configuration:
    url: "https://document-generation-api-preprod.hmpps.service.justice.gov.uk"

service:
  allow-pulling-templates: true
  auto-pull-templates: true
  include-templates:
    - TAP_ROTL_LIC1
    - TAP_ROTL_NOTIFY
    # ... other template codes
```

In dev, `allow-pulling-templates` and `auto-pull-templates` default to `false` — dev is the origin, not a consumer.

### Adding or updating a template

1. **BA** uses the doc gen UI to upload, configure, and test the template in dev. This includes setting the template code, variables, groups, instruction text, and the `.dotx` file.
2. **Dev** adds the template code to `service.include-templates` in both `application-preprod.yml` and `application-prod.yml`, and raises a PR.
3. **On startup**, preprod pulls the template and its configuration from dev; prod pulls from preprod. No manual file copying or environment-specific template management is required.

---

## Groups

Groups bring together templates for a specific domain or service. The two groups currently defined for External Movements are `EXTERNAL_MOVEMENT` and `TEMPORARY_ABSENCE`.

A new service using the doc gen API should define its own group and assign only its templates to it. That service's UI is then responsible for listing and presenting only the templates from its own group — there is no role-based access control at the group level. This means care is needed when managing groups and templates to avoid exposing templates from one service in another service's UI.
