# CIT-Care Submission Checklist

## 1. GitHub Repository Link

- Repository: https://github.com/Rafiki128/IT342-Abella-CIT-Care
- Current local branch checked during preparation: `main`
- Existing refactor branch in history: `refactor-vertical-slice`
- Recommended submission branch for these final testing/report artifacts: `refactor-vertical-slice-testing`

Before submitting, push the final branch:

```powershell
git checkout -b refactor-vertical-slice-testing
git add .
git commit -m "Add regression test plan and automated evidence"
git push -u origin refactor-vertical-slice-testing
```

## 2. Full Regression Test Report PDF

Submit this file:

- `docs/FullRegressionReport_CIT-Care.pdf`

Supporting editable source files:

- `docs/REGRESSION_TEST_REPORT.md`
- `docs/TEST_PLAN.md`

## 3. Automated Test Evidence

Submit or attach the files in:

- `docs/evidence/`

Key evidence files:

- `docs/evidence/backend-mvn-test.log`
- `docs/evidence/frontend-vitest.log`
- `docs/evidence/frontend-eslint.log`
- `docs/evidence/frontend-vite-build.log`
- `docs/evidence/mobile-gradle-test.log`
- `docs/evidence/TEST-edu.cit.abella.citcare.*.xml`
- `docs/evidence/edu.cit.abella.citcare.*.txt`

## Latest Recorded Results

- Backend Maven tests: 13 passed, 0 failed
- Frontend Vitest tests: 7 passed, 0 failed
- Frontend ESLint: 0 errors
- Frontend Vite build: passed
- Mobile Gradle test: blocked by Gradle wrapper path issue, documented in report and log

