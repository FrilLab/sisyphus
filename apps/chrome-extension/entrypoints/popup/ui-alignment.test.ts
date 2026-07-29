// @vitest-environment node
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { describe, expect, it } from 'vitest';

const repositoryRoot = fileURLToPath(new URL('../../../..', import.meta.url));
const webCssPath = join(repositoryRoot, 'apps/web/src/styles/index.css');
const extensionCssPath = join(
  repositoryRoot,
  'apps/chrome-extension/entrypoints/popup/styles/app.css',
);

const SHARED_SEMANTIC_TOKENS = [
  '--sis: #1186ce',
  '--sisy: #ffcd49',
  '--action-primary: color-mix(in srgb, var(--sis) 84%, black)',
  '--focus-ring: var(--sis)',
  '--success: #187a57',
  '--danger: #b42318',
  '--color-action-primary: var(--action-primary)',
  '--color-highlight: var(--brand-accent)',
];

describe('chrome extension UI alignment', () => {
  it('shares semantic token roles with the web foundation', () => {
    const webCss = readFileSync(webCssPath, 'utf8');
    const extensionCss = readFileSync(extensionCssPath, 'utf8');

    for (const token of SHARED_SEMANTIC_TOKENS) {
      expect(webCss).toContain(token);
      expect(extensionCss).toContain(token);
    }

    expect(extensionCss).toMatch(/prefers-reduced-motion:\s*reduce/);
  });

  it('avoids decorative popup styling that conflicts with Focused Workspace', () => {
    const extensionCss = readFileSync(extensionCssPath, 'utf8');

    expect(extensionCss).not.toMatch(/radial-gradient|linear-gradient|box-shadow:\s*var\(--shadow/);
    expect(extensionCss).toContain('border: 1px solid var(--border)');
    expect(extensionCss).toContain('background: var(--action-primary)');
  });

  it('keeps extension source free of raw brand hex values outside token definitions', () => {
    const css = readFileSync(extensionCssPath, 'utf8');
    const tokenSection = css.slice(0, css.indexOf('body {'));

    expect(tokenSection).toContain('#1186ce');
    expect(tokenSection).toContain('#ffcd49');
    expect(css.slice(css.indexOf('body {')).match(/#1186ce|#ffcd49/gi)).toBeNull();
  });
});
