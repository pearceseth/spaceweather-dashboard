#!/usr/bin/env python3
"""
doc_update_agent.py

Two-phase documentation update agent.

Phase 1 — Triage:
  A fast, cheap call that reads the git diff and decides which
  doc files need updating. Returns a list of file paths.

Phase 2 — Update:
  One focused call per affected doc file. Reads the current doc
  content and the relevant source files, returns updated markdown.

The @doc annotation values in changed Scala files are extracted
first and used as authoritative source-to-doc hints before the
agent does any inference.
"""

import os
import sys
import json
import re
import subprocess
import anthropic

client = anthropic.Anthropic(api_key=os.environ["ANTHROPIC_API_KEY"])
DOCS_DIR = "docs"
MODEL = "claude-sonnet-4-20250514"
MAX_TOKENS_TRIAGE = 512
MAX_TOKENS_UPDATE = 2048

# Fallback heuristic mapping if @doc annotations are absent
SOURCE_TO_DOC_HINTS = {
    "backend/src/main/scala/com/solarion/routes/":    "docs/api/",
    "backend/src/main/scala/com/solarion/services/":  "docs/services/",
    "backend/src/main/scala/com/solarion/domain/":    "docs/domain/",
    "backend/src/main/scala/com/solarion/app/Main.scala": "docs/README.md",
}


# ── Git helpers ───────────────────────────────────────────────────────────────

def get_diff() -> str:
    result = subprocess.run(
        ["git", "diff", "HEAD~1", "HEAD", "--", "*.scala", "--unified=5"],
        capture_output=True, text=True
    )
    return result.stdout


def get_changed_scala_files() -> list[str]:
    result = subprocess.run(
        ["git", "diff", "HEAD~1", "HEAD", "--name-only", "--", "*.scala"],
        capture_output=True, text=True
    )
    return [f.strip() for f in result.stdout.splitlines() if f.strip()]


def get_file_at_head(path: str) -> str:
    result = subprocess.run(
        ["git", "show", f"HEAD:{path}"],
        capture_output=True, text=True
    )
    return result.stdout if result.returncode == 0 else ""


# ── Doc helpers ───────────────────────────────────────────────────────────────

def read_doc(path: str) -> str:
    try:
        with open(path) as f:
            return f.read()
    except FileNotFoundError:
        return ""


def write_doc(path: str, content: str):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(content)
    print(f"  wrote: {path}")


def list_all_docs() -> list[str]:
    docs = []
    for root, _, files in os.walk(DOCS_DIR):
        for f in files:
            if f.endswith(".md"):
                docs.append(os.path.join(root, f))
    return sorted(docs)


# ── @doc annotation extraction ────────────────────────────────────────────────

def extract_doc_annotations(scala_source: str) -> list[str]:
    """
    Extract @doc("path") annotation values from Scala source.
    These are the authoritative source-to-doc mappings.
    """
    return re.findall(r'@doc\("([^"]+)"\)', scala_source)


def collect_annotated_docs(changed_files: list[str]) -> list[str]:
    """
    Read all changed Scala files and collect every @doc path referenced.
    These are high-confidence candidates for update.
    """
    paths = []
    for scala_file in changed_files:
        source = get_file_at_head(scala_file)
        paths.extend(extract_doc_annotations(source))
    return list(set(paths))


# ── Phase 1: Triage ───────────────────────────────────────────────────────────

def triage(
    diff: str,
    changed_files: list[str],
    annotated_docs: list[str]
) -> list[str]:
    """
    Ask Claude which doc files need updating.
    Annotated docs are passed as high-confidence hints.
    Returns a deduplicated list of doc file paths.
    """
    all_docs = list_all_docs()

    prompt = f"""You are maintaining documentation for a Scala/ZIO space weather API.

A developer pushed a commit. The following Scala files changed:
{json.dumps(changed_files, indent=2)}

These doc files are explicitly referenced via @doc annotations in the changed source:
{json.dumps(annotated_docs, indent=2)}

All existing documentation files:
{json.dumps(all_docs, indent=2)}

Source-to-doc mapping hints (for files without @doc annotations):
{json.dumps(SOURCE_TO_DOC_HINTS, indent=2)}

The git diff (truncated to 6000 chars):
<diff>
{diff[:6000]}
</diff>

Decide which documentation files need updating. Rules:
- Always include any file listed in the @doc annotations above
- Add any other docs that are clearly affected by the diff
- Include docs/api/overview.md if any route signatures changed
- Include docs/README.md only for significant structural changes
- Do NOT flag docs for internal refactors with no public-facing impact

Respond with ONLY a JSON array of doc file paths. Example:
["docs/api/kp-history.md", "docs/domain/kp-reading.md"]

If nothing needs updating respond with: []
"""

    response = client.messages.create(
        model=MODEL,
        max_tokens=MAX_TOKENS_TRIAGE,
        messages=[{"role": "user", "content": prompt}]
    )

    text = response.content[0].text.strip()
    try:
        paths = json.loads(text)
        valid = [p for p in paths if p.startswith("docs/") and p.endswith(".md")]
        # Always include annotated docs even if agent missed them
        return list(set(valid + annotated_docs))
    except json.JSONDecodeError:
        print(f"  triage returned non-JSON, falling back to annotations only: {text}")
        return annotated_docs


# ── Phase 2: Update ───────────────────────────────────────────────────────────

def update_doc(
    doc_path: str,
    diff: str,
    changed_files: list[str]
) -> str:
    """
    Generate updated content for a single doc file.
    Includes the current doc content and relevant source files as context.
    """
    current_content = read_doc(doc_path)
    is_new = current_content == ""

    # Gather source file content for context
    source_context_parts = []
    for scala_file in changed_files:
        source = get_file_at_head(scala_file)
        if source:
            source_context_parts.append(
                f"### {scala_file}\n```scala\n{source[:3000]}\n```"
            )
    source_context = "\n\n".join(source_context_parts)

    action = "create" if is_new else "update"

    current_doc_section = (
        "This file does not exist yet. Create it from scratch."
        if is_new else
        f"<current_doc>\n{current_content}\n</current_doc>"
    )

    prompt = f"""You are maintaining documentation for a Scala/ZIO space weather API.

Task: {action} the documentation file at: {doc_path}

{current_doc_section}

Git diff that triggered this update (truncated to 5000 chars):
<diff>
{diff[:5000]}
</diff>

Current state of the changed source files:
<source>
{source_context[:5000]}
</source>

Style guide:
- Concise technical prose, no filler or padding
- Code examples in fenced blocks with language tags (scala, json, http)
- API route docs must include: endpoint, parameters, response shape,
  example request/response, error cases, cache TTL, link to source file
- Service docs must include: downstream URLs, data extracted, error handling,
  rate limit notes, normalization behavior, link to source file
- Domain docs must include: field descriptions, validation rules,
  derivation from upstream raw data, link to source file
- Add a relative markdown link to the relevant source file(s)
- Do not invent information not present in the source or diff

Return ONLY the complete updated markdown for {doc_path}.
No explanation, no preamble, no code fences around the output — just markdown.
"""

    response = client.messages.create(
        model=MODEL,
        max_tokens=MAX_TOKENS_UPDATE,
        messages=[{"role": "user", "content": prompt}]
    )

    return response.content[0].text.strip()


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    print("=== Documentation Update Agent ===\n")

    diff = get_diff()
    changed_files = get_changed_scala_files()

    if not changed_files:
        print("No Scala files changed. Exiting.")
        sys.exit(0)

    print(f"Changed Scala files:\n  " + "\n  ".join(changed_files))

    print("\nExtracting @doc annotations...")
    annotated_docs = collect_annotated_docs(changed_files)
    if annotated_docs:
        print(f"  Found: {annotated_docs}")
    else:
        print("  None found — agent will infer from diff.")

    print("\nPhase 1: Triage...")
    docs_to_update = triage(diff, changed_files, annotated_docs)

    if not docs_to_update:
        print("No documentation updates needed.")
        sys.exit(0)

    print(f"  Will update: {docs_to_update}")

    print("\nPhase 2: Updating docs...")
    for doc_path in docs_to_update:
        print(f"\n  {doc_path}...")
        new_content = update_doc(doc_path, diff, changed_files)
        write_doc(doc_path, new_content)

    print("\n=== Done ===")


if __name__ == "__main__":
    main()
