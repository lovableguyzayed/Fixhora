// Renders FixoraX_UI_UX_Design.html to PDF with the pre-installed Playwright Chromium.
const { chromium } = require('playwright');
const path = require('path');
(async () => {
  const dir = __dirname;
  const b = await chromium.launch();
  const p = await b.newPage();
  await p.goto('file://' + path.join(dir, 'FixoraX_UI_UX_Design.html'), { waitUntil: 'networkidle' });
  await p.pdf({ path: path.join(dir, '..', 'FixoraX_UI_UX_Design.pdf'), format: 'A4', landscape: true, printBackground: true, preferCSSPageSize: true });
  await b.close();
})();
