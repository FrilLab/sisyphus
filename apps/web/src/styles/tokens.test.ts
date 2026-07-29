// @vitest-environment node
import { readdirSync, readFileSync } from 'node:fs';
import { extname, join, relative } from 'node:path';
import { fileURLToPath } from 'node:url';
import { describe, expect, it } from 'vitest';

const stylesDirectory = fileURLToPath(new URL('.', import.meta.url));
const sourceDirectory = fileURLToPath(new URL('..', import.meta.url));
const repositoryRoot = fileURLToPath(new URL('../../../..', import.meta.url));

const BRAND_PRIMARY = '#1186ce';
const BRAND_ACCENT = '#ffcd49';

describe('design tokens', () => {
  it('retains canonical brand sources and semantic aliases', () => {
    const css = readFileSync(join(stylesDirectory, 'index.css'), 'utf8');

    expect(css).toContain(`--sis: ${BRAND_PRIMARY}`);
    expect(css).toContain(`--sisy: ${BRAND_ACCENT}`);
    expect(css).toContain('--color-action-primary: var(--action-primary)');
    expect(css).toContain('--color-focus-ring: var(--focus-ring)');
    expect(css).toContain('@media (prefers-reduced-motion: reduce)');
  });

  it('aligns chrome extension popup brand sources with web canonical tokens', () => {
    const extensionCss = readFileSync(
      join(
        repositoryRoot,
        'apps/chrome-extension/entrypoints/popup/styles/app.css',
      ),
      'utf8',
    );

    expect(extensionCss).toContain(`--sis: ${BRAND_PRIMARY}`);
    expect(extensionCss).toContain(`--sisy: ${BRAND_ACCENT}`);
    expect(extensionCss).toContain('--action-primary: color-mix(in srgb, var(--sis) 84%, black)');
    expect(extensionCss).toContain('--color-action-primary: var(--action-primary)');
    expect(extensionCss).toContain('--focus-ring: var(--sis)');
    expect(extensionCss).toContain('--success: #187a57');
    expect(extensionCss).toContain('--danger: #b42318');
    expect(extensionCss).toMatch(/prefers-reduced-motion:\s*reduce/);
  });

  it('keeps brand focus, action, and accent combinations at accessible contrast', () => {
    const actionPrimary = mix(BRAND_PRIMARY, '#000000', 0.84);

    expect(contrast(BRAND_PRIMARY, '#ffffff')).toBeGreaterThanOrEqual(3);
    expect(contrast(actionPrimary, '#ffffff')).toBeGreaterThanOrEqual(4.5);
    expect(contrast(BRAND_ACCENT, '#111827')).toBeGreaterThanOrEqual(4.5);
  });

  it('keeps feature styling free of raw brand and competing blue values', () => {
    const violations = sourceFiles(sourceDirectory)
      .filter((path) => !path.endsWith('styles/index.css'))
      .filter((path) => !path.endsWith('styles/tokens.ts'))
      .filter((path) => !path.endsWith('styles/tokens.test.ts'))
      .flatMap((path) => {
        const content = readFileSync(join(sourceDirectory, path), 'utf8');
        return [
          ...content.matchAll(
            /#1186ce|#ffcd49|#747bff|(?:text|bg|border|ring)-blue-\d+/gi,
          ),
        ].map((match) => `${path}: ${match[0]}`);
      });

    expect(violations).toEqual([]);
  });
});

function sourceFiles(directory: string): string[] {
  return readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const absolutePath = join(directory, entry.name);
    if (entry.isDirectory()) return sourceFiles(absolutePath);
    if (!['.css', '.ts', '.tsx'].includes(extname(entry.name))) return [];
    return [relative(sourceDirectory, absolutePath)];
  });
}

function mix(first: string, second: string, firstWeight: number): string {
  const firstRgb = rgb(first);
  const secondRgb = rgb(second);
  const channels = firstRgb.map((channel, index) =>
    Math.round(channel * firstWeight + secondRgb[index] * (1 - firstWeight)),
  );
  return `#${channels.map((channel) => channel.toString(16).padStart(2, '0')).join('')}`;
}

function contrast(first: string, second: string): number {
  const firstLuminance = luminance(first);
  const secondLuminance = luminance(second);
  return (
    (Math.max(firstLuminance, secondLuminance) + 0.05) /
    (Math.min(firstLuminance, secondLuminance) + 0.05)
  );
}

function luminance(color: string): number {
  const [red, green, blue] = rgb(color).map((channel) => {
    const value = channel / 255;
    return value <= 0.04045
      ? value / 12.92
      : Math.pow((value + 0.055) / 1.055, 2.4);
  });
  return 0.2126 * red + 0.7152 * green + 0.0722 * blue;
}

function rgb(color: string): number[] {
  return [1, 3, 5].map((index) => Number.parseInt(color.slice(index, index + 2), 16));
}
