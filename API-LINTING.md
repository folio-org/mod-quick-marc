# API Linting Guide

This guide explains how to lint the OpenAPI specifications locally using Spectral.

## Prerequisites

You need to have Spectral CLI installed. If you don't have it:

### Install Spectral

```bash
# Using npm (recommended)
npm install -g @stoplight/spectral-cli

# Using yarn
yarn global add @stoplight/spectral-cli
```

Verify installation:
```bash
spectral --version
```

## Running the Linter

### Option 1: Using the Lint Script (Recommended)

Run the provided shell script to lint all API specifications:

```bash
./lint-api.sh
```

This will automatically:
- Check if Spectral is installed
- Lint both `records-editor.yaml` and `marc-specifications.yaml`
- Show a summary of results

### Option 2: Manual Commands

Lint individual files:

```bash
# Lint records-editor API
spectral lint src/main/resources/swagger.api/records-editor.yaml

# Lint marc-specifications API
spectral lint src/main/resources/swagger.api/marc-specifications.yaml

# Lint both files at once
spectral lint src/main/resources/swagger.api/*.yaml
```

### Option 3: Lint with Custom Options

```bash
# Quiet mode (only show errors)
spectral lint --quiet src/main/resources/swagger.api/records-editor.yaml

# JSON output
spectral lint --format json src/main/resources/swagger.api/records-editor.yaml

# Show full error details
spectral lint --verbose src/main/resources/swagger.api/records-editor.yaml

# Lint and ignore specific rules
spectral lint --ignore-unknown-format src/main/resources/swagger.api/records-editor.yaml
```

## Linting Rules

The project uses `.spectral.yaml` configuration file which:

1. **Extends the standard OpenAPI ruleset**: `spectral:oas`
2. **Custom rules**:
   - `docs-descriptions`: Ensures descriptions are provided and properly formatted
   - `docs-info-contact`: Requires contact information in the API spec
   - `docs-summary`: Requires summaries for all operations
   - `docs-parameters-examples-or-schema`: Parameters must have examples or schemas
   - `docs-media-types-examples-or-schema`: Media types must have examples or schemas
   - `docs-tags-alphabetical`: Tags should be in alphabetical order
   - `docs-operation-tags`: Operations must have at least one tag

### Severity Levels

- **error**: Must be fixed (breaks build in CI)
- **warn**: Should be fixed (reported but doesn't break build)
- **info**: Nice to have
- **hint**: Suggestions

## Common Issues and Fixes

### Issue: "Server URL must not have trailing slash"
```yaml
# ❌ Wrong
servers:
  - url: /records-editor/

# ✅ Correct
servers:
  - url: /records-editor
```

### Issue: "Operation tags must be defined in global tags"
```yaml
# Add global tags section
tags:
  - name: records-editor
    description: Operations for managing MARC records

paths:
  /records:
    get:
      tags:
        - records-editor  # Now this is defined
```

### Issue: "version property must be string"
```yaml
# ❌ Wrong
info:
  version: 5.5

# ✅ Correct
info:
  version: "5.5"
```

### Issue: "example property type must be string"
```yaml
# ❌ Wrong
properties:
  tag:
    type: string
    example: 100  # Number

# ✅ Correct
properties:
  tag:
    type: string
    example: "100"  # String
```

## Integration with CI/CD

The GitHub Actions workflow `.github/workflows/api-lint.yml` automatically runs linting on:
- Every push to branches with API changes
- Every pull request with API changes

You can also trigger it manually via GitHub Actions UI.

## VS Code Integration

For real-time linting in VS Code:

1. Install the Spectral extension:
   - Extension ID: `stoplight.spectral`
   
2. The extension will automatically use `.spectral.yaml` configuration

3. You'll see linting errors/warnings inline as you edit OpenAPI files

## IntelliJ IDEA Integration

For IntelliJ IDEA / WebStorm:

1. Install the OpenAPI plugin (usually pre-installed)
2. Configure Spectral as an external tool:
   - Go to Settings → Tools → External Tools
   - Add a new tool:
     - Name: `Spectral Lint`
     - Program: `spectral`
     - Arguments: `lint $FilePath$`
     - Working directory: `$ProjectFileDir$`

## Pre-commit Hook (Optional)

To automatically lint before commits, add to `.git/hooks/pre-commit`:

```bash
#!/bin/sh
echo "Running API lint..."
./lint-api.sh
```

Make it executable:
```bash
chmod +x .git/hooks/pre-commit
```

## Useful Spectral Commands

```bash
# Check Spectral version
spectral --version

# Get help
spectral lint --help

# Validate the .spectral.yaml ruleset itself
spectral lint --ruleset .spectral.yaml --test

# Generate HTML report
spectral lint --format html src/main/resources/swagger.api/records-editor.yaml > api-lint-report.html
```

## Resources

- [Spectral Documentation](https://meta.stoplight.io/docs/spectral)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Spectral Rulesets](https://github.com/stoplightio/spectral-rulesets)
- [Custom Spectral Rules](https://meta.stoplight.io/docs/spectral/docs/guides/4-custom-rulesets.md)
